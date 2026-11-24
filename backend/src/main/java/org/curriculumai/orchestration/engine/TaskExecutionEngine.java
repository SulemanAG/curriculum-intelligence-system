package org.curriculumai.orchestration.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.orchestration.agent.AgentRegistry;
import org.curriculumai.orchestration.agent.SpecialistAgent;
import org.curriculumai.orchestration.agent.StructuredAgentResult;
import org.curriculumai.orchestration.agent.TaskExecutionContext;
import org.curriculumai.orchestration.domain.*;
import org.curriculumai.orchestration.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskExecutionEngine implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(TaskExecutionEngine.class);

    private final OrchestrationRunRepository runRepository;
    private final WorkflowTaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;
    private final AgentExecutionRecordRepository executionRecordRepository;
    private final WorkflowEventRepository eventRepository;
    private final AgentRegistry agentRegistry;
    private final ObjectMapper objectMapper;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final Set<String> currentlyExecutingTasks = ConcurrentHashMap.newKeySet();

    public TaskExecutionEngine(
            OrchestrationRunRepository runRepository,
            WorkflowTaskRepository taskRepository,
            TaskDependencyRepository dependencyRepository,
            AgentExecutionRecordRepository executionRecordRepository,
            WorkflowEventRepository eventRepository,
            AgentRegistry agentRegistry,
            ObjectMapper objectMapper
    ) {
        this.runRepository = runRepository;
        this.taskRepository = taskRepository;
        this.dependencyRepository = dependencyRepository;
        this.executionRecordRepository = executionRecordRepository;
        this.eventRepository = eventRepository;
        this.agentRegistry = agentRegistry;
        this.objectMapper = objectMapper;

        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(4);
        this.taskExecutor.setMaxPoolSize(8);
        this.taskExecutor.setQueueCapacity(100);
        this.taskExecutor.setThreadNamePrefix("aura-orchestrator-");
        this.taskExecutor.initialize();
    }

    @Override
    public void destroy() {
        if (taskExecutor != null) {
            taskExecutor.shutdown();
        }
    }

    /**
     * Initiates run execution and begins scheduling ready tasks.
     */
    @Transactional
    public void startRun(String runId) {
        OrchestrationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Run not found: " + runId));

        if (run.getStatus() == RunStatus.CANCELLED) {
            log.warn("Cannot start cancelled run: {}", runId);
            return;
        }

        run.setStatus(RunStatus.RUNNING);
        run.setStartTime(Instant.now());
        runRepository.save(run);

        recordEvent(runId, null, null, WorkflowEventType.RUN_STARTED,
                "Workflow run started for subject: " + run.getSubject(), null);

        evaluateAndDispatch(runId);
    }

    /**
     * Evaluates all tasks for a run, advances state transitions, and submits ready tasks for execution.
     */
    @Transactional
    public synchronized void evaluateAndDispatch(String runId) {
        OrchestrationRun run = runRepository.findById(runId).orElse(null);
        if (run == null || run.getStatus() == RunStatus.CANCELLED) {
            return;
        }

        List<WorkflowTask> tasks = taskRepository.findByRunIdOrderBySequenceOrderAsc(runId);
        if (tasks.isEmpty()) {
            return;
        }

        // Fixpoint propagation loop: repeatedly evaluate dependencies until task statuses converge
        boolean stateChanged = true;
        while (stateChanged) {
            stateChanged = false;
            Map<String, TaskStatus> taskStatusMap = new HashMap<>();
            for (WorkflowTask t : tasks) {
                taskStatusMap.put(t.getId(), t.getStatus());
            }

            for (WorkflowTask t : tasks) {
                if (t.getStatus() == TaskStatus.PENDING) {
                    List<TaskDependency> deps = dependencyRepository.findByTaskId(t.getId());
                    boolean allDependenciesSatisfied = true;
                    boolean anyDependencyFailedOrBlocked = false;

                    for (TaskDependency dep : deps) {
                        TaskStatus depStatus = taskStatusMap.get(dep.getDependsOnTaskId());
                        if (depStatus != TaskStatus.COMPLETED) {
                            allDependenciesSatisfied = false;
                        }
                        if (depStatus == TaskStatus.FAILED || depStatus == TaskStatus.BLOCKED || depStatus == TaskStatus.CANCELLED) {
                            anyDependencyFailedOrBlocked = true;
                        }
                    }

                    if (anyDependencyFailedOrBlocked) {
                        t.setStatus(TaskStatus.BLOCKED);
                        t.setErrorMessage("Upstream dependency failed or blocked.");
                        t.setErrorType("DEPENDENCY_BLOCKED");
                        t.setCompletedAt(Instant.now());
                        taskRepository.save(t);
                        taskStatusMap.put(t.getId(), TaskStatus.BLOCKED);
                        recordEvent(runId, t.getId(), t.getAssignedAgentId(), WorkflowEventType.TASK_BLOCKED,
                                "Task " + t.getTaskKey() + " blocked because upstream dependency failed or blocked.", null);
                        stateChanged = true;
                    } else if (allDependenciesSatisfied) {
                        t.setStatus(TaskStatus.READY);
                        taskRepository.save(t);
                        taskStatusMap.put(t.getId(), TaskStatus.READY);
                        recordEvent(runId, t.getId(), t.getAssignedAgentId(), WorkflowEventType.TASK_SCHEDULED,
                                "Task " + t.getTaskKey() + " dependencies met; marked READY for execution.", null);
                        stateChanged = true;
                    }
                }
            }
        }

        // Count current states after dependency convergence
        int completed = 0;
        int failed = 0;
        int blocked = 0;
        int running = 0;
        int ready = 0;
        int pending = 0;

        for (WorkflowTask t : tasks) {
            switch (t.getStatus()) {
                case COMPLETED -> completed++;
                case FAILED -> failed++;
                case BLOCKED -> blocked++;
                case RUNNING -> running++;
                case READY -> ready++;
                case PENDING -> pending++;
                default -> {}
            }
        }

        run.setCompletedTasks(completed);
        run.setFailedTasks(failed);

        // Check if workflow has reached a terminal or paused state
        boolean allTerminal = (completed + failed + blocked) == tasks.size();

        if (allTerminal) {
            run.setEndTime(Instant.now());
            if (failed > 0) {
                run.setStatus(RunStatus.FAILED);
                run.setErrorMessage("Workflow terminated with " + failed + " failed task(s).");
                runRepository.save(run);
                recordEvent(runId, null, null, WorkflowEventType.RUN_FAILED,
                        "Workflow run failed: " + run.getErrorMessage(), null);
                return;
            } else if (blocked > 0) {
                run.setStatus(RunStatus.WAITING_FOR_INPUT);
                run.setErrorMessage("Workflow paused: " + blocked + " task(s) are blocked by un-implemented specialist agent(s) in Phase 3.1.");
                runRepository.save(run);
                recordEvent(runId, null, null, WorkflowEventType.TASK_BLOCKED,
                        "Workflow paused awaiting agent implementation or user input: " + blocked + " task(s) blocked.", null);
                return;
            } else {
                run.setStatus(RunStatus.COMPLETED);
                runRepository.save(run);
                recordEvent(runId, null, null, WorkflowEventType.RUN_COMPLETED,
                        "Workflow run completed successfully. All " + completed + " task(s) executed.", null);
                return;
            }
        }

        runRepository.save(run);

        // Submit ready tasks that are not already running or queued
        for (WorkflowTask readyTask : tasks) {
            if (readyTask.getStatus() == TaskStatus.READY && !currentlyExecutingTasks.contains(readyTask.getId())) {
                if (currentlyExecutingTasks.add(readyTask.getId())) {
                    String taskId = readyTask.getId();
                    taskExecutor.submit(() -> executeSingleTask(taskId));
                }
            }
        }
    }

    /**
     * Executes a single task in a worker thread.
     */
    public void executeSingleTask(String taskId) {
        try {
            WorkflowTask task = null;
            for (int retry = 0; retry < 10; retry++) {
                task = taskRepository.findById(taskId).orElse(null);
                if (task != null) break;
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {}
            }

            if (task == null) {
                log.warn("Task {} not found after waiting for transaction commit.", taskId);
                return;
            }

            // Check run status
            OrchestrationRun run = runRepository.findById(task.getRunId()).orElse(null);
            if (run == null || run.getStatus() == RunStatus.CANCELLED) {
                task.setStatus(TaskStatus.CANCELLED);
                task.setCompletedAt(Instant.now());
                taskRepository.save(task);
                return;
            }

            if (task.getStatus() != TaskStatus.READY) {
                return;
            }

            task.setStatus(TaskStatus.RUNNING);
            task.setAttemptCount(task.getAttemptCount() + 1);
            task.setStartedAt(Instant.now());
            taskRepository.save(task);

            recordEvent(task.getRunId(), task.getId(), task.getAssignedAgentId(), WorkflowEventType.TASK_STARTED,
                    "Task " + task.getTaskKey() + " execution started by worker thread.", null);

            long startMs = System.currentTimeMillis();

            // 1. Check Specialist Agent implementation status
            var agentDefOpt = agentRegistry.getAgent(task.getAssignedAgentId());
            if (agentDefOpt.isEmpty() || agentDefOpt.get().status() == AgentStatus.REGISTERED_NOT_IMPLEMENTED) {
                // Honest handling of Phase 3.1 unimplemented specialist agents
                long duration = System.currentTimeMillis() - startMs;
                String reason = "Specialist agent '" + task.getAssignedAgentId()
                        + "' is registered in catalog but marked REGISTERED_NOT_IMPLEMENTED in Phase 3.1 foundation. Task cannot proceed to simulated execution without real implementation.";

                task.setStatus(TaskStatus.BLOCKED);
                task.setErrorMessage(reason);
                task.setErrorType("AGENT_NOT_IMPLEMENTED");
                task.setCompletedAt(Instant.now());
                taskRepository.save(task);

                AgentExecutionRecord record = new AgentExecutionRecord(
                        UUID.randomUUID().toString(),
                        task.getRunId(),
                        task.getId(),
                        task.getAssignedAgentId(),
                        task.getAttemptCount(),
                        "BLOCKED",
                        null,
                        null,
                        null,
                        reason,
                        "{\"phase\":\"3.1\",\"status\":\"REGISTERED_NOT_IMPLEMENTED\"}",
                        duration
                );
                executionRecordRepository.save(record);

                recordEvent(task.getRunId(), task.getId(), task.getAssignedAgentId(), WorkflowEventType.TASK_BLOCKED,
                        "Task " + task.getTaskKey() + " blocked: " + reason, null);

                evaluateAndDispatch(task.getRunId());
                return;
            }

            // 2. Active agent execution
            try {
                SpecialistAgent agentInstance = agentRegistry.getAgentInstance(task.getAssignedAgentId());
                if (agentInstance == null) {
                    throw new IllegalStateException("Agent instance not found for ID: " + task.getAssignedAgentId());
                }

                Map<String, Object> academicScope = Collections.emptyMap();
                if (run.getSharedContextJson() != null && !run.getSharedContextJson().isBlank()) {
                    try {
                        academicScope = objectMapper.readValue(run.getSharedContextJson(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    } catch (Exception e) {
                        log.warn("Could not deserialize sharedContextJson for run {}", run.getId());
                    }
                }

                // Collect prior completed task outputs for this run
                Map<String, StructuredAgentResult> priorTaskOutputs = new LinkedHashMap<>();
                List<WorkflowTask> allTasks = taskRepository.findByRunIdOrderBySequenceOrderAsc(run.getId());
                for (WorkflowTask otherTask : allTasks) {
                    if (otherTask.getStatus() == TaskStatus.COMPLETED && otherTask.getOutputPayloadJson() != null) {
                        try {
                            Map<String, Object> findings = objectMapper.readValue(
                                    otherTask.getOutputPayloadJson(),
                                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                            );

                            List<String> evidenceRefs = Collections.emptyList();
                            List<String> limitations = Collections.emptyList();
                            List<AgentExecutionRecord> execRecs = executionRecordRepository.findByTaskId(otherTask.getId());
                            if (!execRecs.isEmpty()) {
                                AgentExecutionRecord lastRec = execRecs.get(execRecs.size() - 1);
                                if (lastRec.getEvidenceReferencesJson() != null) {
                                    try {
                                        evidenceRefs = objectMapper.readValue(lastRec.getEvidenceReferencesJson(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                                    } catch (Exception ignored) {}
                                }
                                if (lastRec.getLimitationsJson() != null) {
                                    try {
                                        limitations = objectMapper.readValue(lastRec.getLimitationsJson(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                                    } catch (Exception ignored) {}
                                }
                            }

                            StructuredAgentResult res = new StructuredAgentResult(
                                    run.getId(),
                                    otherTask.getId(),
                                    otherTask.getAssignedAgentId(),
                                    "COMPLETED",
                                    findings,
                                    evidenceRefs,
                                    limitations,
                                    null,
                                    Map.of("taskKey", otherTask.getTaskKey())
                            );
                            priorTaskOutputs.put(otherTask.getTaskKey(), res);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize outputPayloadJson for task {}: {}", otherTask.getTaskKey(), e.getMessage());
                        }
                    }
                }

                TaskExecutionContext ctx = new TaskExecutionContext(
                        run.getId(),
                        task.getId(),
                        task.getTaskKey(),
                        run.getSubject(),
                        run.getRawPrompt(),
                        academicScope,
                        Collections.emptyMap(),
                        priorTaskOutputs,
                        Collections.emptyList(),
                        run.getUserId()
                );

                StructuredAgentResult result = agentInstance.execute(ctx);
                long duration = System.currentTimeMillis() - startMs;

                String findingsJson = result.findings() != null ? objectMapper.writeValueAsString(result.findings()) : null;
                String evidenceJson = result.evidenceReferences() != null ? objectMapper.writeValueAsString(result.evidenceReferences()) : null;
                String limitationsJson = result.limitations() != null ? objectMapper.writeValueAsString(result.limitations()) : null;
                String metadataJson = result.executionMetadata() != null ? objectMapper.writeValueAsString(result.executionMetadata()) : null;

                if ("BLOCKED".equalsIgnoreCase(result.executionStatus())) {
                    task.setStatus(TaskStatus.BLOCKED);
                    task.setErrorType("AGENT_BLOCKED");
                    task.setErrorMessage(result.errorDetails() != null ? result.errorDetails() : "Task blocked by agent requirement.");
                    task.setCompletedAt(Instant.now());
                    task.setOutputPayloadJson(findingsJson);
                    taskRepository.save(task);

                    AgentExecutionRecord record = new AgentExecutionRecord(
                            UUID.randomUUID().toString(),
                            task.getRunId(),
                            task.getId(),
                            task.getAssignedAgentId(),
                            task.getAttemptCount(),
                            "BLOCKED",
                            findingsJson,
                            evidenceJson,
                            limitationsJson,
                            result.errorDetails(),
                            metadataJson,
                            duration
                    );
                    executionRecordRepository.save(record);

                    recordEvent(task.getRunId(), task.getId(), task.getAssignedAgentId(), WorkflowEventType.TASK_BLOCKED,
                            "Task " + task.getTaskKey() + " blocked: " + task.getErrorMessage(), null);
                } else if ("FAILED".equalsIgnoreCase(result.executionStatus())) {
                    task.setStatus(TaskStatus.FAILED);
                    task.setErrorType("AGENT_FAILED");
                    task.setErrorMessage(result.errorDetails() != null ? result.errorDetails() : "Task execution failed.");
                    task.setCompletedAt(Instant.now());
                    task.setOutputPayloadJson(findingsJson);
                    taskRepository.save(task);

                    AgentExecutionRecord record = new AgentExecutionRecord(
                            UUID.randomUUID().toString(),
                            task.getRunId(),
                            task.getId(),
                            task.getAssignedAgentId(),
                            task.getAttemptCount(),
                            "FAILED",
                            findingsJson,
                            evidenceJson,
                            limitationsJson,
                            result.errorDetails(),
                            metadataJson,
                            duration
                    );
                    executionRecordRepository.save(record);

                    recordEvent(task.getRunId(), task.getId(), task.getAssignedAgentId(), WorkflowEventType.TASK_FAILED,
                            "Task " + task.getTaskKey() + " failed: " + task.getErrorMessage(), null);
                } else {
                    task.setStatus(TaskStatus.COMPLETED);
                    task.setCompletedAt(Instant.now());
                    task.setOutputPayloadJson(findingsJson);
                    taskRepository.save(task);

                    AgentExecutionRecord record = new AgentExecutionRecord(
                            UUID.randomUUID().toString(),
                            task.getRunId(),
                            task.getId(),
                            task.getAssignedAgentId(),
                            task.getAttemptCount(),
                            result.executionStatus(),
                            findingsJson,
                            evidenceJson,
                            limitationsJson,
                            result.errorDetails(),
                            metadataJson,
                            duration
                    );
                    executionRecordRepository.save(record);

                    recordEvent(task.getRunId(), task.getId(), task.getAssignedAgentId(), WorkflowEventType.TASK_COMPLETED,
                            "Task " + task.getTaskKey() + " completed successfully in " + duration + "ms.", null);
                }

            } catch (Exception ex) {
                long duration = System.currentTimeMillis() - startMs;
                log.error("Task {} execution failed: {}", task.getTaskKey(), ex.getMessage(), ex);

                if (task.getAttemptCount() <= task.getMaxRetries()) {
                    task.setStatus(TaskStatus.READY);
                    task.setErrorMessage("Attempt " + task.getAttemptCount() + " failed: " + ex.getMessage());
                    taskRepository.save(task);

                    recordEvent(task.getRunId(), task.getId(), task.getAssignedAgentId(), WorkflowEventType.TASK_RETRYING,
                            "Task " + task.getTaskKey() + " failed; queued for retry (" + task.getAttemptCount() + "/" + task.getMaxRetries() + "). Error: " + ex.getMessage(), null);
                } else {
                    task.setStatus(TaskStatus.FAILED);
                    task.setCompletedAt(Instant.now());
                    task.setErrorMessage(ex.getMessage());
                    task.setErrorType(ex.getClass().getSimpleName());
                    taskRepository.save(task);

                    AgentExecutionRecord record = new AgentExecutionRecord(
                            UUID.randomUUID().toString(),
                            task.getRunId(),
                            task.getId(),
                            task.getAssignedAgentId(),
                            task.getAttemptCount(),
                            "FAILED",
                            null,
                            null,
                            null,
                            ex.getMessage(),
                            null,
                            duration
                    );
                    executionRecordRepository.save(record);

                    recordEvent(task.getRunId(), task.getId(), task.getAssignedAgentId(), WorkflowEventType.TASK_FAILED,
                            "Task " + task.getTaskKey() + " failed permanently after " + task.getAttemptCount() + " attempts: " + ex.getMessage(), null);
                }
            }

            evaluateAndDispatch(task.getRunId());
        } finally {
            currentlyExecutingTasks.remove(taskId);
        }
    }

    /**
     * Cancels an active or pending run and all its non-terminal tasks.
     */
    @Transactional
    public void cancelRun(String runId) {
        OrchestrationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Run not found: " + runId));

        run.setStatus(RunStatus.CANCELLED);
        run.setEndTime(Instant.now());
        runRepository.save(run);

        List<WorkflowTask> tasks = taskRepository.findByRunIdOrderBySequenceOrderAsc(runId);
        for (WorkflowTask t : tasks) {
            if (t.getStatus() == TaskStatus.PENDING || t.getStatus() == TaskStatus.READY || t.getStatus() == TaskStatus.RUNNING) {
                t.setStatus(TaskStatus.CANCELLED);
                t.setCompletedAt(Instant.now());
                taskRepository.save(t);
            }
        }

        recordEvent(runId, null, null, WorkflowEventType.RUN_CANCELLED,
                "Workflow run was cancelled by user.", null);
    }

    /**
     * Resumes an interrupted, paused, or failed run from the last valid checkpoint.
     */
    @Transactional
    public void resumeRun(String runId) {
        OrchestrationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Run not found: " + runId));

        run.setStatus(RunStatus.RUNNING);
        run.setErrorMessage(null);
        run.setEndTime(null);
        runRepository.save(run);

        List<WorkflowTask> tasks = taskRepository.findByRunIdOrderBySequenceOrderAsc(runId);
        for (WorkflowTask t : tasks) {
            if (t.getStatus() == TaskStatus.FAILED || t.getStatus() == TaskStatus.BLOCKED || t.getStatus() == TaskStatus.CANCELLED) {
                t.setStatus(TaskStatus.PENDING);
                t.setErrorMessage(null);
                t.setErrorType(null);
                t.setCompletedAt(null);
                taskRepository.save(t);
            }
        }

        recordEvent(runId, null, null, WorkflowEventType.RUN_RESUMED,
                "Workflow run resumed from checkpoint.", null);

        evaluateAndDispatch(runId);
    }

    private void recordEvent(String runId, String taskId, String agentId, WorkflowEventType type, String summary, String metadataJson) {
        WorkflowEvent event = new WorkflowEvent(
                UUID.randomUUID().toString(),
                runId,
                taskId,
                agentId,
                type,
                summary,
                metadataJson
        );
        eventRepository.save(event);
    }
}
