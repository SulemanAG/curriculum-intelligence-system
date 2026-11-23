package org.curriculumai.academic;

import org.curriculumai.academic.dto.*;
import org.curriculumai.academic.service.AcademicCatalogService;
import org.curriculumai.academic.service.CurriculumGovernanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CrossVersionSemesterIntegrityTest {

    @Autowired
    private AcademicCatalogService catalogService;

    @Autowired
    private CurriculumGovernanceService governanceService;

    private String programmeId;
    private String courseId;
    private final String userId = "usr-faculty-cross-sem";

    @BeforeEach
    void setupCatalog() {
        InstitutionDto.InstitutionResponse inst = catalogService.createInstitution(
                new InstitutionDto.CreateInstitutionRequest("XSEM-UNIV", "Cross Sem Univ", "Belagavi", "India", null, null)
        );
        DepartmentDto.DepartmentResponse dept = catalogService.createDepartment(
                new DepartmentDto.CreateDepartmentRequest(inst.id(), "CSE-XSEM", "Computer Science")
        );
        ProgrammeDto.ProgrammeResponse prog = catalogService.createProgramme(
                new ProgrammeDto.CreateProgrammeRequest(dept.id(), "BE-CS", "B.E. Computer Science", "UG", 4, 160)
        );
        this.programmeId = prog.id();

        CourseDto.CourseResponse crs = catalogService.createCourse(
                new CourseDto.CreateCourseRequest(dept.id(), "CS201", "Data Structures", null, "CORE", 4, 3, 1, 0)
        );
        this.courseId = crs.id();
    }

    @Test
    @DisplayName("Prevent assigning course to a semester belonging to a different curriculum version")
    void testCrossVersionSemesterAssignmentRejected() {
        // Create Curriculum Version A
        CurriculumVersionDto.CurriculumVersionResponse currA = governanceService.createCurriculumVersion(
                userId,
                new CurriculumVersionDto.CreateCurriculumVersionRequest(programmeId, "CURR-2024", 2024, 0, null)
        );
        SemesterDto.SemesterResponse semA = governanceService.createSemester(
                new SemesterDto.CreateSemesterRequest(currA.id(), 1, "Curriculum A - Sem 1", 20)
        );

        // Create Curriculum Version B
        CurriculumVersionDto.CurriculumVersionResponse currB = governanceService.createCurriculumVersion(
                userId,
                new CurriculumVersionDto.CreateCurriculumVersionRequest(programmeId, "CURR-2025", 2025, 0, null)
        );
        SemesterDto.SemesterResponse semB = governanceService.createSemester(
                new SemesterDto.CreateSemesterRequest(currB.id(), 1, "Curriculum B - Sem 1", 20)
        );

        // Attempt to add course to Curriculum A, but pass semester ID from Curriculum B
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                governanceService.addCourseToSemester(
                        new CurriculumCourseDto.AddCourseToSemesterRequest(
                                currA.id(),
                                semB.id(), // Semester belongs to currB, NOT currA!
                                courseId,
                                4, 3, 1, 0, false, null
                        )
                )
        );
        assertTrue(ex.getMessage().contains("does not belong to curriculum version"));

        // Attempt opposite: Add course to Curriculum B, but pass semester ID from Curriculum A
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                governanceService.addCourseToSemester(
                        new CurriculumCourseDto.AddCourseToSemesterRequest(
                                currB.id(),
                                semA.id(), // Semester belongs to currA, NOT currB!
                                courseId,
                                4, 3, 1, 0, false, null
                        )
                )
        );
        assertTrue(ex2.getMessage().contains("does not belong to curriculum version"));

        // Legitimate assignment succeeds
        assertDoesNotThrow(() ->
                governanceService.addCourseToSemester(
                        new CurriculumCourseDto.AddCourseToSemesterRequest(
                                currA.id(),
                                semA.id(), // Valid: semA belongs to currA
                                courseId,
                                4, 3, 1, 0, false, null
                        )
                )
        );
    }
}

