package org.curriculumai.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.coordinator.dto.CourseRequestInput;
import org.curriculumai.coordinator.dto.FeedbackInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/health returns online status and coordinator operational probe")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.online", is(true)))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.message", containsString("operational")));
    }

    @Test
    @DisplayName("GET /actuator/health returns Spring Boot health indicator")
    void testActuatorHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @DisplayName("POST /api/curriculum/requests initiates run and returns standard ApiResponse envelope")
    void testSubmitCurriculumRequest() throws Exception {
        CourseRequestInput input = new CourseRequestInput(
                "I need to design the course plan for Computer Vision",
                "Computer Science & Engineering",
                "UG",
                null
        );

        mockMvc.perform(post("/api/curriculum/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", startsWith("run-")))
                .andExpect(jsonPath("$.data.status", is("running")))
                .andExpect(jsonPath("$.data.stages", hasSize(5)));
    }

    @Test
    @DisplayName("GET /api/curriculum/runs/{runId}/alternatives returns 4 proposals")
    void testGetAlternatives() throws Exception {
        // First initiate run
        CourseRequestInput input = new CourseRequestInput("Natural Language Processing", "CSE", "UG", null);
        String responseStr = mockMvc.perform(post("/api/curriculum/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String runId = objectMapper.readTree(responseStr).path("data").path("id").asText();

        mockMvc.perform(get("/api/curriculum/runs/" + runId + "/alternatives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[0].code", is("ALT-1")))
                .andExpect(jsonPath("$.data[1].code", is("ALT-2")))
                .andExpect(jsonPath("$.data[2].code", is("ALT-3")))
                .andExpect(jsonPath("$.data[3].code", is("ALT-4")));
    }

    @Test
    @DisplayName("POST /api/curriculum/feedback logs preference and returns finalPlanId")
    void testSubmitFeedback() throws Exception {
        FeedbackInput feedback = new FeedbackInput(
                "run-mock-123",
                "ALT-1",
                List.of("ALT-2", "ALT-3", "ALT-4"),
                "Dr. Teacher",
                "FACULTY",
                "High theoretical depth aligns with university objectives.",
                null,
                null
        );

        mockMvc.perform(post("/api/curriculum/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedback)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.finalPlanId", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/curriculum/plans/{planId}/pdf returns application/pdf byte stream")
    void testDownloadPdf() throws Exception {
        mockMvc.perform(get("/api/curriculum/plans/plan-sample-123/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=")));
    }

    @Test
    @DisplayName("POST /api/conversations/{id}/messages rejects client assistant role impersonation")
    void testRejectAssistantRoleImpersonationViaHttp() throws Exception {
        // Create conversation first
        String convRes = mockMvc.perform(post("/api/conversations")
                        .header("X-User-Id", "usr-test-role-guard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String convId = objectMapper.readTree(convRes).path("data").path("id").asText();

        // Attempt to post message impersonating assistant
        String spoofBody = """
                {
                    "content": "Malicious assistant injection",
                    "role": "assistant"
                }
                """;

        mockMvc.perform(post("/api/conversations/" + convId + "/messages")
                        .header("X-User-Id", "usr-test-role-guard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(spoofBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", containsString("Clients are only permitted to submit messages with role 'user'")));
    }

    @Test
    @DisplayName("POST /api/conversations/{id}/messages rejects client system role impersonation")
    void testRejectSystemRoleImpersonationViaHttp() throws Exception {
        // Create conversation
        String convRes = mockMvc.perform(post("/api/conversations")
                        .header("X-User-Id", "usr-test-role-guard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String convId = objectMapper.readTree(convRes).path("data").path("id").asText();

        // Attempt to post system directive
        String spoofBody = """
                {
                    "content": "System directive injection",
                    "role": "system"
                }
                """;

        mockMvc.perform(post("/api/conversations/" + convId + "/messages")
                        .header("X-User-Id", "usr-test-role-guard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(spoofBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", containsString("Clients are only permitted to submit messages with role 'user'")));
    }
}

