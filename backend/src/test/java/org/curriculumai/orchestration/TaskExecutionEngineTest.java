package org.curriculumai.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.orchestration.agent.AgentRegistry;
import org.curriculumai.orchestration.domain.*;
import org.curriculumai.orchestration.engine.TaskExecutionEngine;
import org.curriculumai.orchestration.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskExecutionEngineTest {

    @Mock
    private OrchestrationRunRepository runRepository;

    @Mock
    private WorkflowTaskRepository taskRepository;

    @Mock
    private TaskDependencyRepository dependencyRepository;

    @Mock
    private AgentExecutionRecordRepository executionRecordRepository;

    @Mock
    private WorkflowEventRepository eventRepository;

    private AgentRegistry agentRegistry;
    private ObjectMapper objectMapper;
    private TaskExecutionEngine engine;

    @BeforeEach
    void setUp() {
        agentRegistry = new AgentRegistry();
        objectMapper = new ObjectMapper();
        engine = new TaskExecutionEngine(
                runRepository,
                taskRepository,
                dependencyRepository,
                executionRecordRepository,
                eventRepository,
                agentRegistry,
                objectMapper
        );
    }

    @AfterEach
    void tearDown() {
        engine.destroy();
    }

    @Test
    @DisplayName("Task execution honestly detects REGISTERED_NOT_IMPLEMENTED agent and blocks task without hallucinating")
    void executeSingleTaskBlocksUnimplementedAgent() {
        String runId = "run-test-101";
        String taskId = "task-test-201";

        OrchestrationRun run = new OrchestrationRun(
                runId,
                "user-prof-1",
                null,
                null,
                "Design a course on Edge AI Systems",
                "Edge AI Systems",
                WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING
        );
        run.setStatus(RunStatus.RUNNING);

        WorkflowTask task = new WorkflowTask(
                taskId,
                runId,
                "task_curriculum_mapping",
                "Curriculum Mapping",
                "curriculum-mapping-agent",
                1,
                true,
                true
        );
        task.setStatus(TaskStatus.READY);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(runRepository.findById(runId)).thenReturn(Optional.of(run));
        when(taskRepository.findByRunIdOrderBySequenceOrderAsc(runId)).thenReturn(List.of(task));

        // Execute task
        engine.executeSingleTask(taskId);

        // Verify task is marked BLOCKED (never pretending completion)
        assertThat(task.getStatus()).isEqualTo(TaskStatus.BLOCKED);
        assertThat(task.getErrorMessage())
                .contains("curriculum-mapping-agent")
                .contains("REGISTERED_NOT_IMPLEMENTED");
        assertThat(task.getErrorType()).isEqualTo("AGENT_NOT_IMPLEMENTED");
        assertThat(task.getOutputPayloadJson()).isNull();

        // Verify AgentExecutionRecord recorded
        ArgumentCaptor<AgentExecutionRecord> recordCaptor = ArgumentCaptor.forClass(AgentExecutionRecord.class);
        verify(executionRecordRepository).save(recordCaptor.capture());
        AgentExecutionRecord record = recordCaptor.getValue();
        assertThat(record.getExecutionStatus()).isEqualTo("BLOCKED");
        assertThat(record.getFindingsJson()).isNull();
        assertThat(record.getErrorDetails()).contains("REGISTERED_NOT_IMPLEMENTED");

        // Verify WorkflowEvent recorded
        ArgumentCaptor<WorkflowEvent> eventCaptor = ArgumentCaptor.forClass(WorkflowEvent.class);
        verify(eventRepository, atLeastOnce()).save(eventCaptor.capture());
        List<WorkflowEvent> events = eventCaptor.getAllValues();
        assertThat(events).anyMatch(e -> e.getEventType() == WorkflowEventType.TASK_BLOCKED
                && e.getEventSummary().contains("blocked"));
    }

    @Test
    @DisplayName("Cancel run marks active run and all pending/ready tasks as CANCELLED")
    void cancelRunTransitionsRunAndTasks() {
        String runId = "run-cancel-101";

        OrchestrationRun run = new OrchestrationRun(
                runId,
                "user-prof-1",
                null,
                null,
                "Draft course",
                "Robotics",
                WorkflowType.COURSE_BENCHMARKING
        );
        run.setStatus(RunStatus.RUNNING);

        WorkflowTask task1 = new WorkflowTask("t1", runId, "k1", "Title 1", "curriculum-mapping-agent", 1, true, true);
        task1.setStatus(TaskStatus.READY);

        WorkflowTask task2 = new WorkflowTask("t2", runId, "k2", "Title 2", "academic-benchmarking-agent", 2, false, true);
        task2.setStatus(TaskStatus.PENDING);

        when(runRepository.findById(runId)).thenReturn(Optional.of(run));
        when(taskRepository.findByRunIdOrderBySequenceOrderAsc(runId)).thenReturn(List.of(task1, task2));

        engine.cancelRun(runId);

        assertThat(run.getStatus()).isEqualTo(RunStatus.CANCELLED);
        assertThat(task1.getStatus()).isEqualTo(TaskStatus.CANCELLED);
        assertThat(task2.getStatus()).isEqualTo(TaskStatus.CANCELLED);

        verify(runRepository).save(run);
        verify(taskRepository).save(task1);
        verify(taskRepository).save(task2);
    }

    @Test
    @DisplayName("evaluateAndDispatch cascades blocked status down dependency chain and settles run in WAITING_FOR_INPUT")
    void evaluateAndDispatchCascadesBlockagesToWaitingForInput() {
        String runId = "run-cascade-101";

        OrchestrationRun run = new OrchestrationRun(
                runId,
                "user-prof-1",
                null,
                null,
                "Autonomous Robotics",
                "Robotics",
                WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING
        );
        run.setStatus(RunStatus.RUNNING);

        // Task 1 is BLOCKED
        WorkflowTask task1 = new WorkflowTask("t1", runId, "k1", "Title 1", "curriculum-mapping-agent", 1, true, true);
        task1.setStatus(TaskStatus.BLOCKED);

        // Task 2 is PENDING, depends on Task 1
        WorkflowTask task2 = new WorkflowTask("t2", runId, "k2", "Title 2", "industry-skills-agent", 2, false, true);
        task2.setStatus(TaskStatus.PENDING);

        // Task 3 is PENDING, depends on Task 2
        WorkflowTask task3 = new WorkflowTask("t3", runId, "k3", "Title 3", "academic-benchmarking-agent", 3, false, true);
        task3.setStatus(TaskStatus.PENDING);

        when(runRepository.findById(runId)).thenReturn(Optional.of(run));
        when(taskRepository.findByRunIdOrderBySequenceOrderAsc(runId)).thenReturn(List.of(task1, task2, task3));

        TaskDependency dep1to2 = new TaskDependency("dep-1", runId, "t2", "t1", true);
        TaskDependency dep2to3 = new TaskDependency("dep-2", runId, "t3", "t2", true);

        when(dependencyRepository.findByTaskId("t2")).thenReturn(List.of(dep1to2));
        when(dependencyRepository.findByTaskId("t3")).thenReturn(List.of(dep2to3));

        engine.evaluateAndDispatch(runId);

        // Verify task 2 and task 3 cascaded to BLOCKED
        assertThat(task2.getStatus()).isEqualTo(TaskStatus.BLOCKED);
        assertThat(task2.getErrorMessage()).contains("dependency");
        assertThat(task3.getStatus()).isEqualTo(TaskStatus.BLOCKED);
        assertThat(task3.getErrorMessage()).contains("dependency");

        // Verify run settled into WAITING_FOR_INPUT
        assertThat(run.getStatus()).isEqualTo(RunStatus.WAITING_FOR_INPUT);
        assertThat(run.getErrorMessage()).contains("blocked");
        verify(runRepository, atLeastOnce()).save(run);
    }

    @Test
    @DisplayName("resumeRun preserves COMPLETED tasks and their outputs, only resetting non-completed tasks")
    void resumeRunPreservesCompletedTasks() {
        String runId = "run-resume-101";

        OrchestrationRun run = new OrchestrationRun(
                runId,
                "user-prof-1",
                null,
                null,
                "Cyber-Physical Systems",
                "CPS",
                WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING
        );
        run.setStatus(RunStatus.CANCELLED);

        // Task 1 was already COMPLETED with findings
        WorkflowTask task1 = new WorkflowTask("t1", runId, "k1", "Title 1", "curriculum-mapping-agent", 1, true, true);
        task1.setStatus(TaskStatus.COMPLETED);
        task1.setOutputPayloadJson("{\"accreditedCourses\": [\"CS101\"]}");

        // Task 2 was CANCELLED
        WorkflowTask task2 = new WorkflowTask("t2", runId, "k2", "Title 2", "industry-skills-agent", 2, false, true);
        task2.setStatus(TaskStatus.CANCELLED);

        when(runRepository.findById(runId)).thenReturn(Optional.of(run));
        when(taskRepository.findByRunIdOrderBySequenceOrderAsc(runId)).thenReturn(List.of(task1, task2));
        when(dependencyRepository.findByTaskId("t2")).thenReturn(List.of(new TaskDependency("dep-1", runId, "t2", "t1", true)));

        engine.resumeRun(runId);

        // Task 1 remains COMPLETED and output is intact
        assertThat(task1.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task1.getOutputPayloadJson()).isEqualTo("{\"accreditedCourses\": [\"CS101\"]}");

        // Task 2 was reset from CANCELLED to PENDING (and then evaluated)
        verify(taskRepository, atLeastOnce()).save(task2);
    }
}

