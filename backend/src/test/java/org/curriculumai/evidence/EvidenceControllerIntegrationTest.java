package org.curriculumai.evidence;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.evidence.domain.SourceType;
import org.curriculumai.evidence.dto.RegisterSourceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EvidenceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/evidence/sources registers source with X-User-Id header")
    void testRegisterSourceEndpoint() throws Exception {
        RegisterSourceRequest request = new RegisterSourceRequest(
                "https://apni-india.org/standards/curriculum-2025",
                SourceType.ACADEMIC_STANDARD,
                "APNI",
                "National AI Engineering Curriculum Standard",
                "Academic Standard",
                null, null, null
        );

        mockMvc.perform(post("/api/v1/evidence/sources")
                        .header("X-User-Id", "usr-academic-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.canonicalUrl").value("https://apni-india.org/standards/curriculum-2025"))
                .andExpect(jsonPath("$.data.publisher").value("APNI"));
    }

    @Test
    @DisplayName("POST /api/v1/evidence/sources without X-User-Id is rejected with 403 Forbidden")
    void testRegisterSourceWithoutIdentityIsForbidden() throws Exception {
        RegisterSourceRequest request = new RegisterSourceRequest(
                "https://example.org/spec",
                SourceType.OTHER,
                "Example Org",
                "Spec",
                null, null, null, null
        );

        mockMvc.perform(post("/api/v1/evidence/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("POST /api/v1/evidence/sources/{id}/upload ingests document successfully")
    void testUploadDocumentEndpoint() throws Exception {
        // Register source first
        RegisterSourceRequest reg = new RegisterSourceRequest(
                "https://worldbank.org/reports/skills-gap-2025",
                SourceType.INDUSTRY_REPORT,
                "World Bank",
                "Global Skills Gap Analysis",
                null, null, null, null
        );

        String regResponse = mockMvc.perform(post("/api/v1/evidence/sources")
                        .header("X-User-Id", "usr-academic-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String sourceId = objectMapper.readTree(regResponse).path("data").path("id").asText();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "worldbank_report.md",
                "text/markdown",
                "# Global Skills Gap\n\nDemand for AI infrastructure and distributed databases.".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/evidence/sources/{id}/upload", sourceId)
                        .file(file)
                        .param("publicationDate", "2025-03-01")
                        .header("X-User-Id", "usr-academic-admin"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.versionIdentifier").value("v1"));
    }

    @Test
    @DisplayName("GET /api/v1/evidence/chunks/search returns search results")
    void testSearchChunksEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/evidence/chunks/search")
                        .param("query", "curriculum")
                        .header("X-User-Id", "usr-academic-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

