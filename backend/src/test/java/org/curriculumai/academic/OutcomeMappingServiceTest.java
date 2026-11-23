package org.curriculumai.academic;

import org.curriculumai.academic.dto.*;
import org.curriculumai.academic.service.AcademicCatalogService;
import org.curriculumai.academic.service.OutcomeMappingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OutcomeMappingServiceTest {

    @Autowired
    private OutcomeMappingService outcomeService;

    @Autowired
    private AcademicCatalogService catalogService;

    private String programmeId;
    private String courseId;

    @BeforeEach
    void setup() {
        InstitutionDto.InstitutionResponse inst = catalogService.createInstitution(
                new InstitutionDto.CreateInstitutionRequest("OUTCOME-UNIV", "Outcome University", "Dharwad", "India", null, null)
        );
        DepartmentDto.DepartmentResponse dept = catalogService.createDepartment(
                new DepartmentDto.CreateDepartmentRequest(inst.id(), "CSE", "Computer Science")
        );
        ProgrammeDto.ProgrammeResponse prog = catalogService.createProgramme(
                new ProgrammeDto.CreateProgrammeRequest(dept.id(), "BTECH-CSE", "B.Tech Computer Science", "UG", 4, 160)
        );
        this.programmeId = prog.id();

        CourseDto.CourseResponse course = catalogService.createCourse(
                new CourseDto.CreateCourseRequest(dept.id(), "CS301", "Database Systems", null, "CORE", 4, 3, 1, 0)
        );
        this.courseId = course.id();
    }

    @Test
    @DisplayName("PO, CO, and CO-PO mapping creation with strength level validation")
    void testOutcomesAndCoPoMapping() {
        // 1. Programme Outcome (PO1)
        OutcomeDto.ProgrammeOutcomeResponse po1 = outcomeService.createProgrammeOutcome(
                new OutcomeDto.CreateProgrammeOutcomeRequest(programmeId, "PO1", "Engineering Knowledge", "Apply mathematics and science", "PO")
        );
        assertNotNull(po1.id());
        assertEquals("PO1", po1.code());

        // 2. Programme Specific Outcome (PSO1)
        OutcomeDto.ProgrammeOutcomeResponse pso1 = outcomeService.createProgrammeOutcome(
                new OutcomeDto.CreateProgrammeOutcomeRequest(programmeId, "PSO1", "AI Applications", "Design intelligence algorithms", "PSO")
        );
        assertNotNull(pso1.id());

        // 3. Course Outcomes (CO1, CO2)
        OutcomeDto.CourseOutcomeResponse co1 = outcomeService.createCourseOutcome(
                new OutcomeDto.CreateCourseOutcomeRequest(courseId, "CO1", "Model relational schemas using normalization theory", "L3_APPLY")
        );
        OutcomeDto.CourseOutcomeResponse co2 = outcomeService.createCourseOutcome(
                new OutcomeDto.CreateCourseOutcomeRequest(courseId, "CO2", "Implement SQL query optimization strategies", "L4_ANALYZE")
        );
        assertEquals("CO1", co1.code());
        assertEquals("L3_APPLY", co1.bloomLevel());

        // 4. Valid CO-PO Mapping (Level 3 - High)
        OutcomeDto.CoPoMappingResponse map1 = outcomeService.createCoPoMapping(
                new OutcomeDto.CreateCoPoMappingRequest(co1.id(), po1.id(), 3, "Direct application of foundational schema theory")
        );
        assertNotNull(map1.id());
        assertEquals(3, map1.mappingLevel());

        // 5. Invalid Mapping Level (e.g., 0 or 4) must fail
        assertThrows(IllegalArgumentException.class, () ->
                outcomeService.createCoPoMapping(new OutcomeDto.CreateCoPoMappingRequest(co2.id(), po1.id(), 0, "Invalid level"))
        );
        assertThrows(IllegalArgumentException.class, () ->
                outcomeService.createCoPoMapping(new OutcomeDto.CreateCoPoMappingRequest(co2.id(), po1.id(), 4, "Invalid level"))
        );

        // 6. Duplicate Mapping Prevention
        assertThrows(IllegalArgumentException.class, () ->
                outcomeService.createCoPoMapping(new OutcomeDto.CreateCoPoMappingRequest(co1.id(), po1.id(), 2, "Duplicate"))
        );

        List<OutcomeDto.CoPoMappingResponse> mappingsForCo = outcomeService.listMappingsByCourseOutcome(co1.id());
        assertEquals(1, mappingsForCo.size());
    }
}

