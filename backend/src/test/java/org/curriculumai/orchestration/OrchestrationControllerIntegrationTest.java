package org.curriculumai.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.orchestration.domain.RunStatus;
import org.curriculumai.orchestration.domain.WorkflowType;
import org.curriculumai.orchestration.dto.CreateRunRequest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrchestrationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.curriculumai.academic.repository.InstitutionRepository institutionRepository;

    @Autowired
    private org.curriculumai.academic.repository.DepartmentRepository departmentRepository;

    @Autowired
    private org.curriculumai.academic.repository.CourseRepository courseRepository;

    @Autowired
    private org.curriculumai.orchestration.engine.TaskExecutionEngine taskExecutionEngine;

    @Test
    @DisplayName("GET /api/v1/orchestration/agents lists all five registered specialist agents")
    void listAgents() throws Exception {
        mockMvc.perform(get("/api/v1/orchestration/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(jsonPath("$.data[0].agentId").value("curriculum-mapping-agent"))
                .andExpect(jsonPath("$.data[0].status").value("IMPLEMENTED"))
                .andExpect(jsonPath("$.data[1].status").value("IMPLEMENTED"))
                .andExpect(jsonPath("$.data[2].status").value("IMPLEMENTED"))
                .andExpect(jsonPath("$.data[3].status").value("IMPLEMENTED"))
                .andExpect(jsonPath("$.data[4].status").value("IMPLEMENTED"));
    }

    @Test
    @DisplayName("POST /api/v1/orchestration/runs creates run, persists DAG, and verifies user access")
    void createAndManageRunLifecycle() throws Exception {
        CreateRunRequest request = new CreateRunRequest(
                "Design a modern semester course for Edge AI and TinyML Systems",
                "Edge AI and TinyML",
                WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING,
                null, null, null, null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/orchestration/runs")
                        .header("X-User-Id", "usr-faculty-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.subject").value("Edge AI and TinyML"))
                .andExpect(jsonPath("$.data.totalTasks").value(5))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        String runId = objectMapper.readTree(responseBody).path("data").path("id").asText();
        assertThat(runId).isNotBlank();

        // Retrieve run overview with owner
        mockMvc.perform(get("/api/v1/orchestration/runs/" + runId)
                        .header("X-User-Id", "usr-faculty-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(runId))
                .andExpect(jsonPath("$.data.subject").value("Edge AI and TinyML"));

        // Retrieve workflow plan & task graph
        mockMvc.perform(get("/api/v1/orchestration/runs/" + runId + "/tasks")
                        .header("X-User-Id", "usr-faculty-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tasks", hasSize(5)))
                .andExpect(jsonPath("$.data.dependencies").isNotEmpty());

        // Retrieve workflow events audit log
        mockMvc.perform(get("/api/v1/orchestration/runs/" + runId + "/events")
                        .header("X-User-Id", "usr-faculty-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty());

        // Cancel the run
        mockMvc.perform(post("/api/v1/orchestration/runs/" + runId + "/cancel")
                        .header("X-User-Id", "usr-faculty-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // Resume the run
        mockMvc.perform(post("/api/v1/orchestration/runs/" + runId + "/resume")
                        .header("X-User-Id", "usr-faculty-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RUNNING"));

        // Access forbidden with another user
        mockMvc.perform(get("/api/v1/orchestration/runs/" + runId)
                        .header("X-User-Id", "usr-unauthorized-other"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/orchestration/runs rejects blank prompt or subject with 400 Bad Request")
    void rejectInvalidRunSubmission() throws Exception {
        CreateRunRequest invalid = new CreateRunRequest(
                "",
                "",
                WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING,
                null, null, null, null
        );

        mockMvc.perform(post("/api/v1/orchestration/runs")
                        .header("X-User-Id", "usr-faculty-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/orchestration/runs without X-User-Id is rejected with 403 Forbidden")
    void missingUserIdHeaderReturnsForbidden() throws Exception {
        CreateRunRequest request = new CreateRunRequest(
                "Design a course",
                "Edge AI",
                WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING,
                null, null, null, null
        );

        mockMvc.perform(post("/api/v1/orchestration/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/orchestration/runs with blank X-User-Id is rejected with 403 Forbidden")
    void blankUserIdHeaderReturnsForbidden() throws Exception {
        CreateRunRequest request = new CreateRunRequest(
                "Design a course",
                "Edge AI",
                WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING,
                null, null, null, null
        );

        mockMvc.perform(post("/api/v1/orchestration/runs")
                        .header("X-User-Id", "   ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/orchestration/runs/{id} without X-User-Id is rejected with 403 Forbidden")
    void missingUserIdOnGetRunReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/orchestration/runs/run-any-id"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Workflow executes Curriculum Mapping Task successfully and preserves output while other agents stay honestly blocked")
    void workflowExecutesCurriculumMappingTaskSuccessfully() throws Exception {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        org.curriculumai.academic.domain.Institution inst = new org.curriculumai.academic.domain.Institution(
                "inst-h2-" + uid, "INST-H2-" + uid, "University H2", "Hubli", "India"
        );
        institutionRepository.save(inst);

        org.curriculumai.academic.domain.Department dept = new org.curriculumai.academic.domain.Department(
                "dept-h2-" + uid, inst, "CSE-H2-" + uid, "CSE Dept H2"
        );
        departmentRepository.save(dept);

        org.curriculumai.academic.domain.Course crs = new org.curriculumai.academic.domain.Course(
                "crs-h2-" + uid, dept, "CS-EDGE-" + uid, "Edge Computing & TinyML", 4, 3, 0, 2
        );
        courseRepository.save(crs);

        CreateRunRequest request = new CreateRunRequest(
                "Map and benchmark Edge Computing & TinyML course",
                "CS-EDGE-" + uid,
                WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING,
                null, null, null, null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/orchestration/runs")
                        .header("X-User-Id", "usr-faculty-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        String runId = objectMapper.readTree(responseBody).path("data").path("id").asText();

        MvcResult tasksResult = mockMvc.perform(get("/api/v1/orchestration/runs/" + runId + "/tasks")
                        .header("X-User-Id", "usr-faculty-test"))
                .andExpect(status().isOk())
                .andReturn();

        String tasksJson = tasksResult.getResponse().getContentAsString();
        var tasksNode = objectMapper.readTree(tasksJson).path("data").path("tasks");
        var task1 = tasksNode.get(0);

        // Execute task 1 directly in test transaction context
        taskExecutionEngine.executeSingleTask(task1.path("id").asText());

        // Tasks 2 and 3 become READY. Execute them directly to verify honest BLOCKED handling
        var task2Init = tasksNode.get(1);
        var task3Init = tasksNode.get(2);
        taskExecutionEngine.executeSingleTask(task2Init.path("id").asText());
        taskExecutionEngine.executeSingleTask(task3Init.path("id").asText());

        // Re-query tasks after execution
        tasksResult = mockMvc.perform(get("/api/v1/orchestration/runs/" + runId + "/tasks")
                        .header("X-User-Id", "usr-faculty-test"))
                .andExpect(status().isOk())
                .andReturn();

        tasksJson = tasksResult.getResponse().getContentAsString();
        tasksNode = objectMapper.readTree(tasksJson).path("data").path("tasks");

        // Task 1: curriculum-mapping-agent should be COMPLETED with valid output
        task1 = tasksNode.get(0);
        assertThat(task1.path("taskKey").asText()).isEqualTo("task_curriculum_mapping");
        assertThat(task1.path("status").asText()).isEqualTo("COMPLETED");
        assertThat(task1.path("outputPayloadJson").asText()).contains("Edge Computing & TinyML");

        // Tasks 2 & 3: industry-skills-agent & academic-benchmarking-agent should be BLOCKED (no external API key in test environment)
        var task2 = tasksNode.get(1);
        assertThat(task2.path("taskKey").asText()).isEqualTo("task_industry_skills");
        assertThat(task2.path("status").asText()).isEqualTo("BLOCKED");
        assertThat(task2.path("errorType").asText()).isEqualTo("AGENT_BLOCKED");

        var task3 = tasksNode.get(2);
        assertThat(task3.path("taskKey").asText()).isEqualTo("task_academic_benchmarking");
        assertThat(task3.path("status").asText()).isEqualTo("BLOCKED");
        assertThat(task3.path("errorType").asText()).isEqualTo("AGENT_BLOCKED");

        // Tasks 4 & 5: gap-analysis-agent & recommendation-agent should be BLOCKED due to dependencies
        var task4 = tasksNode.get(3);
        assertThat(task4.path("taskKey").asText()).isEqualTo("task_gap_analysis");
        assertThat(task4.path("status").asText()).isEqualTo("BLOCKED");
        assertThat(task4.path("errorType").asText()).isEqualTo("DEPENDENCY_BLOCKED");

        var task5 = tasksNode.get(4);
        assertThat(task5.path("taskKey").asText()).isEqualTo("task_recommendations");
        assertThat(task5.path("status").asText()).isEqualTo("BLOCKED");
        assertThat(task5.path("errorType").asText()).isEqualTo("DEPENDENCY_BLOCKED");

        // Run should settle in WAITING_FOR_INPUT
        mockMvc.perform(get("/api/v1/orchestration/runs/" + runId)
                        .header("X-User-Id", "usr-faculty-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("WAITING_FOR_INPUT"))
                .andExpect(jsonPath("$.data.completedTasks").value(1));
    }
}

