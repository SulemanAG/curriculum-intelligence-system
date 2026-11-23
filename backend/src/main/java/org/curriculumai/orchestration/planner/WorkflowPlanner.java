package org.curriculumai.orchestration.planner;

import org.curriculumai.orchestration.agent.AgentDefinition;
import org.curriculumai.orchestration.agent.AgentRegistry;
import org.curriculumai.orchestration.domain.AgentStatus;
import org.curriculumai.orchestration.domain.WorkflowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WorkflowPlanner {

    private static final Logger log = LoggerFactory.getLogger(WorkflowPlanner.class);

    private final AgentRegistry agentRegistry;
    private final org.curriculumai.orchestration.repository.WorkflowTaskRepository taskRepository;
    private final org.curriculumai.orchestration.repository.TaskDependencyRepository dependencyRepository;

    public record PlannedTask(
            String taskKey,
            String title,
            String assignedAgentId,
            int sequenceOrder,
            boolean isParallelEligible,
            boolean isRequired,
            List<String> dependsOnTaskKeys
    ) {}

    public record WorkflowPlan(
            WorkflowType workflowType,
            String subject,
            List<PlannedTask> tasks
    ) {}

    public WorkflowPlanner(
            AgentRegistry agentRegistry,
            org.curriculumai.orchestration.repository.WorkflowTaskRepository taskRepository,
            org.curriculumai.orchestration.repository.TaskDependencyRepository dependencyRepository
    ) {
        this.agentRegistry = agentRegistry;
        this.taskRepository = taskRepository;
        this.dependencyRepository = dependencyRepository;
    }

    /**
     * Generates and persists the deterministic task DAG for the given run.
     */
    @org.springframework.transaction.annotation.Transactional
    public WorkflowPlan generatePlan(org.curriculumai.orchestration.domain.OrchestrationRun run, WorkflowType workflowType) {
        WorkflowPlan plan = createPlan(workflowType, run.getSubject());

        Map<String, org.curriculumai.orchestration.domain.WorkflowTask> taskMap = new HashMap<>();
        for (PlannedTask pt : plan.tasks()) {
            org.curriculumai.orchestration.domain.WorkflowTask task = new org.curriculumai.orchestration.domain.WorkflowTask(
                    UUID.randomUUID().toString(),
                    run.getId(),
                    pt.taskKey(),
                    pt.title(),
                    pt.assignedAgentId(),
                    pt.sequenceOrder(),
                    pt.isParallelEligible(),
                    pt.isRequired()
            );
            task.setStatus(org.curriculumai.orchestration.domain.TaskStatus.PENDING);
            task = taskRepository.save(task);
            taskMap.put(pt.taskKey(), task);
        }

        for (PlannedTask pt : plan.tasks()) {
            org.curriculumai.orchestration.domain.WorkflowTask currentTask = taskMap.get(pt.taskKey());
            for (String depKey : pt.dependsOnTaskKeys()) {
                org.curriculumai.orchestration.domain.WorkflowTask depTask = taskMap.get(depKey);
                if (depTask != null) {
                    org.curriculumai.orchestration.domain.TaskDependency dep = new org.curriculumai.orchestration.domain.TaskDependency(
                            UUID.randomUUID().toString(),
                            run.getId(),
                            currentTask.getId(),
                            depTask.getId(),
                            true
                    );
                    dependencyRepository.save(dep);
                }
            }
        }

        return plan;
    }

    /**
     * Constructs and validates a deterministic execution graph for the requested workflow.
     */
    public WorkflowPlan createPlan(WorkflowType workflowType, String subject) {
        if (subject == null || subject.isBlank()) {
            throw new InvalidWorkflowPlanException("Subject must not be blank for workflow plan");
        }

        List<PlannedTask> plannedTasks;
        switch (workflowType) {
            case CURRICULUM_RESEARCH_AND_PLANNING -> plannedTasks = buildCurriculumResearchAndPlanningPlan(subject);
            case CURRICULUM_GAP_ANALYSIS -> plannedTasks = buildCurriculumGapAnalysisPlan(subject);
            case COURSE_BENCHMARKING -> plannedTasks = buildCourseBenchmarkingPlan(subject);
            default -> throw new InvalidWorkflowPlanException("Unsupported workflow type: " + workflowType);
        }

        validatePlan(plannedTasks);
        log.info("Constructed and validated workflow plan for type '{}', subject '{}' with {} tasks",
                workflowType, subject, plannedTasks.size());

        return new WorkflowPlan(workflowType, subject, plannedTasks);
    }

    private List<PlannedTask> buildCurriculumResearchAndPlanningPlan(String subject) {
        return List.of(
                // Phase 1: Parallel Research Tasks
                new PlannedTask(
                        "task_curriculum_mapping",
                        "Institutional Curriculum Mapping: " + subject,
                        "curriculum-mapping-agent",
                        1,
                        true,
                        true,
                        Collections.emptyList()
                ),
                new PlannedTask(
                        "task_industry_skills",
                        "Industry Competency & Skill Matrix Extraction: " + subject,
                        "industry-skills-agent",
                        2,
                        true,
                        true,
                        Collections.emptyList()
                ),
                new PlannedTask(
                        "task_academic_benchmarking",
                        "Peer Academic Benchmarking & Standards Survey: " + subject,
                        "academic-benchmarking-agent",
                        3,
                        true,
                        true,
                        Collections.emptyList()
                ),
                // Phase 2: Synthesis (requires all 3 research tasks)
                new PlannedTask(
                        "task_gap_analysis",
                        "Multi-Perspective Curricular Gap Analysis: " + subject,
                        "gap-analysis-agent",
                        4,
                        false,
                        true,
                        List.of("task_curriculum_mapping", "task_industry_skills", "task_academic_benchmarking")
                ),
                // Phase 3: Recommendation (requires gap analysis)
                new PlannedTask(
                        "task_recommendations",
                        "Curriculum Revision & Course Proposal Formulation: " + subject,
                        "recommendation-agent",
                        5,
                        false,
                        true,
                        List.of("task_gap_analysis")
                )
        );
    }

    private List<PlannedTask> buildCurriculumGapAnalysisPlan(String subject) {
        return List.of(
                new PlannedTask(
                        "task_curriculum_mapping",
                        "Institutional Curriculum Mapping: " + subject,
                        "curriculum-mapping-agent",
                        1,
                        true,
                        true,
                        Collections.emptyList()
                ),
                new PlannedTask(
                        "task_industry_skills",
                        "Industry Competency Analysis: " + subject,
                        "industry-skills-agent",
                        2,
                        true,
                        true,
                        Collections.emptyList()
                ),
                new PlannedTask(
                        "task_gap_analysis",
                        "Curricular Gap Analysis: " + subject,
                        "gap-analysis-agent",
                        3,
                        false,
                        true,
                        List.of("task_curriculum_mapping", "task_industry_skills")
                )
        );
    }

    private List<PlannedTask> buildCourseBenchmarkingPlan(String subject) {
        return List.of(
                new PlannedTask(
                        "task_curriculum_mapping",
                        "Institutional Course Inspection: " + subject,
                        "curriculum-mapping-agent",
                        1,
                        true,
                        true,
                        Collections.emptyList()
                ),
                new PlannedTask(
                        "task_academic_benchmarking",
                        "Academic Standards & Peer Benchmarking: " + subject,
                        "academic-benchmarking-agent",
                        2,
                        true,
                        true,
                        Collections.emptyList()
                ),
                new PlannedTask(
                        "task_recommendations",
                        "Benchmarking Summary & Suggestions: " + subject,
                        "recommendation-agent",
                        3,
                        false,
                        true,
                        List.of("task_curriculum_mapping", "task_academic_benchmarking")
                )
        );
    }

    /**
     * Validates that the plan has no cycles, all dependencies exist, and all agents are registered and enabled.
     */
    public void validatePlan(List<PlannedTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            throw new InvalidWorkflowPlanException("Workflow plan must contain at least one task");
        }

        Map<String, PlannedTask> taskMap = new HashMap<>();
        for (PlannedTask t : tasks) {
            if (taskMap.put(t.taskKey(), t) != null) {
                throw new InvalidWorkflowPlanException("Duplicate task key in plan: " + t.taskKey());
            }

            // Verify agent exists and is not disabled
            Optional<AgentDefinition> agentDef = agentRegistry.getAgentDefinition(t.assignedAgentId());
            if (agentDef.isEmpty()) {
                throw new InvalidWorkflowPlanException("Unknown assigned agent: " + t.assignedAgentId());
            }
            if (!agentDef.get().enabled() || agentDef.get().status() == AgentStatus.DISABLED) {
                throw new InvalidWorkflowPlanException("Assigned agent is disabled: " + t.assignedAgentId());
            }
        }

        // Verify dependencies exist
        for (PlannedTask t : tasks) {
            for (String depKey : t.dependsOnTaskKeys()) {
                if (!taskMap.containsKey(depKey)) {
                    throw new InvalidWorkflowPlanException(
                            "Task '" + t.taskKey() + "' depends on non-existent task '" + depKey + "'"
                    );
                }
                if (depKey.equals(t.taskKey())) {
                    throw new InvalidWorkflowPlanException("Task '" + t.taskKey() + "' cannot depend on itself");
                }
            }
        }

        // Cycle Detection via Topological Sort (Kahn's Algorithm)
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> adj = new HashMap<>();
        for (PlannedTask t : tasks) {
            inDegree.put(t.taskKey(), t.dependsOnTaskKeys().size());
            adj.put(t.taskKey(), new ArrayList<>());
        }

        for (PlannedTask t : tasks) {
            for (String depKey : t.dependsOnTaskKeys()) {
                adj.get(depKey).add(t.taskKey());
            }
        }

        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        int visitedCount = 0;
        while (!queue.isEmpty()) {
            String u = queue.poll();
            visitedCount++;
            for (String v : adj.get(u)) {
                inDegree.put(v, inDegree.get(v) - 1);
                if (inDegree.get(v) == 0) {
                    queue.add(v);
                }
            }
        }

        if (visitedCount != tasks.size()) {
            throw new InvalidWorkflowPlanException("Cycle detected in task dependency graph! Processed "
                    + visitedCount + " of " + tasks.size() + " tasks.");
        }
    }
}
