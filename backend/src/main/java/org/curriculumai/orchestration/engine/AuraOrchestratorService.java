package org.curriculumai.orchestration.engine;

import org.curriculumai.common.exceptions.ResourceNotFoundException;
import org.curriculumai.orchestration.domain.*;
import org.curriculumai.orchestration.dto.*;
import org.curriculumai.orchestration.planner.WorkflowPlanner;
import org.curriculumai.orchestration.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuraOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(AuraOrchestratorService.class);

    private final OrchestrationRunRepository runRepository;
    private final WorkflowTaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;
    private final WorkflowEventRepository eventRepository;
    private final WorkflowPlanner workflowPlanner;
    private final TaskExecutionEngine taskExecutionEngine;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public AuraOrchestratorService(
            OrchestrationRunRepository runRepository,
            WorkflowTaskRepository taskRepository,
            TaskDependencyRepository dependencyRepository,
            WorkflowEventRepository eventRepository,
            WorkflowPlanner workflowPlanner,
            TaskExecutionEngine taskExecutionEngine,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper
    ) {
        this.runRepository = runRepository;
        this.taskRepository = taskRepository;
        this.dependencyRepository = dependencyRepository;
        this.eventRepository = eventRepository;
        this.workflowPlanner = workflowPlanner;
        this.taskExecutionEngine = taskExecutionEngine;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RunOverviewDto createAndStartRun(CreateRunRequest req, String userId) {
        String runId = UUID.randomUUID().toString();
        log.info("Creating orchestration run {} for user {} on subject '{}'", runId, userId, req.subject());

        OrchestrationRun run = new OrchestrationRun(
                runId,
                userId,
                req.conversationId(),
                req.messageId(),
                req.prompt(),
                req.subject(),
                req.workflowType() != null ? req.workflowType() : WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING
        );

        java.util.Map<String, Object> scope = new java.util.LinkedHashMap<>();
        if (req.institutionId() != null) scope.put("institutionId", req.institutionId());
        if (req.departmentId() != null) scope.put("departmentId", req.departmentId());
        if (req.programmeId() != null) scope.put("programmeId", req.programmeId());
        if (req.curriculumVersionId() != null) scope.put("curriculumVersionId", req.curriculumVersionId());
        if (req.courseCode() != null) scope.put("courseCode", req.courseCode());
        if (!scope.isEmpty()) {
            try {
                run.setSharedContextJson(objectMapper.writeValueAsString(scope));
            } catch (Exception e) {
                log.warn("Failed to serialize shared context JSON: {}", e.getMessage());
            }
        }

        run = runRepository.save(run);

        // Record creation event
        recordEvent(runId, null, null, WorkflowEventType.RUN_CREATED,
                "Orchestration run created for subject: " + req.subject(), null);

        // Generate and persist DAG
        WorkflowPlanner.WorkflowPlan plan = workflowPlanner.generatePlan(run, run.getWorkflowType());
        run.setTotalTasks(plan.tasks().size());
        run.setStatus(RunStatus.PLANNED);
        run = runRepository.save(run);

        recordEvent(runId, null, null, WorkflowEventType.RUN_PLANNED,
                "Workflow DAG successfully synthesized with " + plan.tasks().size() + " tasks.", null);

        // Asynchronously dispatch engine
        taskExecutionEngine.startRun(runId);

        return RunOverviewDto.fromEntity(run);
    }

    @Transactional(readOnly = true)
    public RunOverviewDto getRunOverview(String runId, String userId) {
        OrchestrationRun run = getRunAndVerifyOwner(runId, userId);
        return RunOverviewDto.fromEntity(run);
    }

    @Transactional(readOnly = true)
    public Page<RunOverviewDto> listRuns(String userId, Pageable pageable) {
        return runRepository.findByUserId(userId, pageable)
                .map(RunOverviewDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public WorkflowPlanDto getWorkflowPlan(String runId, String userId) {
        OrchestrationRun run = getRunAndVerifyOwner(runId, userId);
        List<WorkflowTaskDto> tasks = taskRepository.findByRunIdOrderBySequenceOrderAsc(runId).stream()
                .map(WorkflowTaskDto::fromEntity)
                .collect(Collectors.toList());

        List<TaskDependencyDto> deps = dependencyRepository.findByRunId(runId).stream()
                .map(TaskDependencyDto::fromEntity)
                .collect(Collectors.toList());

        return new WorkflowPlanDto(run.getId(), run.getWorkflowType(), tasks, deps);
    }

    @Transactional(readOnly = true)
    public WorkflowTaskDto getTaskDetails(String taskId, String userId) {
        WorkflowTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowTask", taskId));

        // Verify run owner
        getRunAndVerifyOwner(task.getRunId(), userId);
        return WorkflowTaskDto.fromEntity(task);
    }

    @Transactional(readOnly = true)
    public List<WorkflowEventDto> listEvents(String runId, String userId) {
        getRunAndVerifyOwner(runId, userId);
        return eventRepository.findByRunIdOrderByCreatedAtAsc(runId).stream()
                .map(WorkflowEventDto::fromEntity)
                .collect(Collectors.toList());
    }

    public void evaluateAndDispatch(String runId) {
        taskExecutionEngine.evaluateAndDispatch(runId);
    }

    @Transactional
    public RunOverviewDto cancelRun(String runId, String userId) {
        getRunAndVerifyOwner(runId, userId);
        taskExecutionEngine.cancelRun(runId);
        OrchestrationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("OrchestrationRun", runId));
        return RunOverviewDto.fromEntity(run);
    }

    @Transactional
    public RunOverviewDto resumeRun(String runId, String userId) {
        getRunAndVerifyOwner(runId, userId);
        taskExecutionEngine.resumeRun(runId);
        OrchestrationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("OrchestrationRun", runId));
        return RunOverviewDto.fromEntity(run);
    }

    private OrchestrationRun getRunAndVerifyOwner(String runId, String userId) {
        OrchestrationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("OrchestrationRun", runId));
        if (!run.getUserId().equals(userId)) {
            throw new AccessDeniedException("User " + userId + " does not have authorization to access run " + runId);
        }
        return run;
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

