package org.curriculumai.academic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.academic.dto.DepartmentDto;
import org.curriculumai.academic.dto.InstitutionDto;
import org.curriculumai.academic.dto.ProgrammeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AcademicControllersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("REST API: Create and list institutions and departments with standard envelope")
    void testAcademicCatalogEndpoints() throws Exception {
        // 1. Create Institution
        InstitutionDto.CreateInstitutionRequest instReq = new InstitutionDto.CreateInstitutionRequest(
                "KLE-BELAGAVI", "KLE Technological University", "Belagavi", "India", "https://kletech.ac.in", null
        );

        String instRes = mockMvc.perform(post("/api/academic/institutions")
                        .header("X-User-Id", "usr-default-faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(instReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.code", is("KLE-BELAGAVI")))
                .andReturn().getResponse().getContentAsString();

        String instId = objectMapper.readTree(instRes).path("data").path("id").asText();

        // 2. Create Department
        DepartmentDto.CreateDepartmentRequest deptReq = new DepartmentDto.CreateDepartmentRequest(
                instId, "CSE-AI", "School of Computer Science & Engineering (AI)"
        );

        String deptRes = mockMvc.perform(post("/api/academic/departments")
                        .header("X-User-Id", "usr-default-faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deptReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.code", is("CSE-AI")))
                .andReturn().getResponse().getContentAsString();

        String deptId = objectMapper.readTree(deptRes).path("data").path("id").asText();

        // 3. Create Programme
        ProgrammeDto.CreateProgrammeRequest progReq = new ProgrammeDto.CreateProgrammeRequest(
                deptId, "BTECH-AI", "B.Tech in Artificial Intelligence", "UG", 4, 160
        );

        mockMvc.perform(post("/api/academic/programmes")
                        .header("X-User-Id", "usr-default-faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.code", is("BTECH-AI")));

        // 4. Query Departments by Institution
        mockMvc.perform(get("/api/academic/departments?institutionId=" + instId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].code", is("CSE-AI")));
    }
}

