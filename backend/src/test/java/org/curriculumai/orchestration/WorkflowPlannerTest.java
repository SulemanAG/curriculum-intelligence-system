package org.curriculumai.orchestration;

import org.curriculumai.orchestration.agent.AgentRegistry;
import org.curriculumai.orchestration.domain.WorkflowType;
import org.curriculumai.orchestration.planner.InvalidWorkflowPlanException;
import org.curriculumai.orchestration.planner.WorkflowPlanner;
import org.curriculumai.orchestration.repository.TaskDependencyRepository;
import org.curriculumai.orchestration.repository.WorkflowTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class WorkflowPlannerTest {

    private AgentRegistry agentRegistry;

    @Mock
    private WorkflowTaskRepository taskRepository;

    @Mock
    private TaskDependencyRepository dependencyRepository;

    private WorkflowPlanner planner;

    @BeforeEach
    void setUp() {
        agentRegistry = new AgentRegistry();
        planner = new WorkflowPlanner(agentRegistry, taskRepository, dependencyRepository);
    }

    @Test
    @DisplayName("Curriculum Research and Planning workflow synthesizes deterministic 5-task DAG")
    void curriculumResearchWorkflowSynthesizesExpectedDag() {
        WorkflowPlanner.WorkflowPlan plan = planner.createPlan(
                WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING,
                "Machine Learning in Healthcare"
        );

        assertThat(plan.tasks()).hasSize(5);

        // Task 1: Curriculum Mapping (Parallel eligible, no upstream deps)
        WorkflowPlanner.PlannedTask mapping = plan.tasks().get(0);
        assertThat(mapping.taskKey()).isEqualTo("task_curriculum_mapping");
        assertThat(mapping.assignedAgentId()).isEqualTo("curriculum-mapping-agent");
        assertThat(mapping.isParallelEligible()).isTrue();
        assertThat(mapping.dependsOnTaskKeys()).isEmpty();

        // Task 2: Industry Skills (Parallel eligible, no upstream deps)
        WorkflowPlanner.PlannedTask skills = plan.tasks().get(1);
        assertThat(skills.taskKey()).isEqualTo("task_industry_skills");
        assertThat(skills.assignedAgentId()).isEqualTo("industry-skills-agent");
        assertThat(skills.isParallelEligible()).isTrue();
        assertThat(skills.dependsOnTaskKeys()).isEmpty();

        // Task 3: Academic Benchmarking (Parallel eligible, no upstream deps)
        WorkflowPlanner.PlannedTask benchmarking = plan.tasks().get(2);
        assertThat(benchmarking.taskKey()).isEqualTo("task_academic_benchmarking");
        assertThat(benchmarking.assignedAgentId()).isEqualTo("academic-benchmarking-agent");
        assertThat(benchmarking.isParallelEligible()).isTrue();
        assertThat(benchmarking.dependsOnTaskKeys()).isEmpty();

        // Task 4: Gap Analysis (Requires all 3 parallel tasks)
        WorkflowPlanner.PlannedTask gapAnalysis = plan.tasks().get(3);
        assertThat(gapAnalysis.taskKey()).isEqualTo("task_gap_analysis");
        assertThat(gapAnalysis.assignedAgentId()).isEqualTo("gap-analysis-agent");
        assertThat(gapAnalysis.isParallelEligible()).isFalse();
        assertThat(gapAnalysis.dependsOnTaskKeys()).containsExactlyInAnyOrder(
                "task_curriculum_mapping",
                "task_industry_skills",
                "task_academic_benchmarking"
        );

        // Task 5: Recommendations (Requires gap analysis)
        WorkflowPlanner.PlannedTask recommendations = plan.tasks().get(4);
        assertThat(recommendations.taskKey()).isEqualTo("task_recommendations");
        assertThat(recommendations.assignedAgentId()).isEqualTo("recommendation-agent");
        assertThat(recommendations.isParallelEligible()).isFalse();
        assertThat(recommendations.dependsOnTaskKeys()).containsExactly("task_gap_analysis");
    }

    @Test
    @DisplayName("Blank or null subject is rejected")
    void blankSubjectRejected() {
        assertThatThrownBy(() -> planner.createPlan(WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING, ""))
                .isInstanceOf(InvalidWorkflowPlanException.class)
                .hasMessageContaining("Subject must not be blank");

        assertThatThrownBy(() -> planner.createPlan(WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING, null))
                .isInstanceOf(InvalidWorkflowPlanException.class)
                .hasMessageContaining("Subject must not be blank");
    }

    @Test
    @DisplayName("Cycle detection rejects circular task dependencies")
    void cycleDetectionRejectsCyclicPlan() {
        List<WorkflowPlanner.PlannedTask> cyclicTasks = List.of(
                new WorkflowPlanner.PlannedTask("task_a", "Task A", "curriculum-mapping-agent", 1, false, true, List.of("task_b")),
                new WorkflowPlanner.PlannedTask("task_b", "Task B", "industry-skills-agent", 2, false, true, List.of("task_a"))
        );

        assertThatThrownBy(() -> planner.validatePlan(cyclicTasks))
                .isInstanceOf(InvalidWorkflowPlanException.class)
                .hasMessageContaining("Cycle detected");
    }

    @Test
    @DisplayName("Plan validation rejects non-existent upstream dependency references")
    void missingDependencyReferenceRejected() {
        List<WorkflowPlanner.PlannedTask> invalidTasks = List.of(
                new WorkflowPlanner.PlannedTask("task_a", "Task A", "curriculum-mapping-agent", 1, false, true, List.of("task_unknown"))
        );

        assertThatThrownBy(() -> planner.validatePlan(invalidTasks))
                .isInstanceOf(InvalidWorkflowPlanException.class)
                .hasMessageContaining("depends on non-existent task");
    }
}

