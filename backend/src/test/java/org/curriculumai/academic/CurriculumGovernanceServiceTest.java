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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CurriculumGovernanceServiceTest {

    @Autowired
    private CurriculumGovernanceService governanceService;

    @Autowired
    private AcademicCatalogService catalogService;

    @Autowired
    private org.curriculumai.users.UserRepository userRepository;

    private String programmeId;
    private String courseId1;
    private String courseId2;
    private final String userId = "usr-default-faculty";
    private final String approverId = "usr-test-dean";

    @BeforeEach
    void setupCatalog() {
        if (!userRepository.existsById("usr-default-faculty")) {
            userRepository.save(new org.curriculumai.users.User(
                    "usr-default-faculty", "faculty_member", "faculty@test.edu", "Dr. Faculty", "FACULTY"
            ));
        }
        if (!userRepository.existsById("usr-test-dean")) {
            userRepository.save(new org.curriculumai.users.User(
                    "usr-test-dean", "test_dean", "dean@test.edu", "Dr. Test Dean", "DEAN"
            ));
        }

        InstitutionDto.InstitutionResponse inst = catalogService.createInstitution(
                new InstitutionDto.CreateInstitutionRequest("GOV-UNIV", "Gov University", "Belagavi", "India", null, null)
        );
        DepartmentDto.DepartmentResponse dept = catalogService.createDepartment(
                new DepartmentDto.CreateDepartmentRequest(inst.id(), "AI-DEPT", "Artificial Intelligence")
        );
        ProgrammeDto.ProgrammeResponse prog = catalogService.createProgramme(
                new ProgrammeDto.CreateProgrammeRequest(dept.id(), "BE-AI", "B.E. Artificial Intelligence", "UG", 4, 160)
        );
        this.programmeId = prog.id();

        CourseDto.CourseResponse c1 = catalogService.createCourse(
                new CourseDto.CreateCourseRequest(dept.id(), "AI101", "Intro to AI", null, "CORE", 4, 3, 1, 0)
        );
        CourseDto.CourseResponse c2 = catalogService.createCourse(
                new CourseDto.CreateCourseRequest(dept.id(), "AI201", "Machine Learning", null, "CORE", 4, 3, 0, 2)
        );
        this.courseId1 = c1.id();
        this.courseId2 = c2.id();
    }

    @Test
    @DisplayName("Curriculum versioning, semester allocation, and lifecycle transitions")
    void testCurriculumLifecycleAndCourseAllocation() {
        // 1. Create version in DRAFT
        CurriculumVersionDto.CurriculumVersionResponse v1 = governanceService.createCurriculumVersion(
                userId,
                new CurriculumVersionDto.CreateCurriculumVersionRequest(programmeId, "2024-REG", 2024, 0, "ACADEMIC_SENATE")
        );
        assertNotNull(v1.id());
        assertEquals("DRAFT", v1.lifecycleStatus());
        assertFalse(v1.isImmutable());

        // 2. Add Semesters
        SemesterDto.SemesterResponse sem1 = governanceService.createSemester(
                new SemesterDto.CreateSemesterRequest(v1.id(), 1, "Semester 1", 20)
        );
        SemesterDto.SemesterResponse sem2 = governanceService.createSemester(
                new SemesterDto.CreateSemesterRequest(v1.id(), 2, "Semester 2", 20)
        );
        assertEquals(1, sem1.semesterNumber());
        assertEquals(2, sem2.semesterNumber());

        // 3. Add Course Offering to Semester 1
        CurriculumCourseDto.CurriculumCourseResponse cc1 = governanceService.addCourseToSemester(
                new CurriculumCourseDto.AddCourseToSemesterRequest(v1.id(), sem1.id(), courseId1, 4, 3, 1, 0, false, null)
        );
        assertEquals("AI101", cc1.courseCode());
        assertEquals(4, cc1.credits());

        // 4. Verify illegal transition (cannot jump DRAFT -> APPROVED directly)
        assertThrows(IllegalStateException.class, () ->
                governanceService.transitionStatus(userId, v1.id(), new CurriculumVersionDto.TransitionStatusRequest("APPROVED", "RES-01", null))
        );

        // 5. Valid transition: DRAFT -> UNDER_REVIEW
        CurriculumVersionDto.CurriculumVersionResponse underReview = governanceService.transitionStatus(
                userId, v1.id(), new CurriculumVersionDto.TransitionStatusRequest("UNDER_REVIEW", null, "Submitted for BoS")
        );
        assertEquals("UNDER_REVIEW", underReview.lifecycleStatus());

        // 6. Verify role authorization gate: standard FACULTY cannot approve
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                governanceService.transitionStatus(
                        userId, v1.id(), new CurriculumVersionDto.TransitionStatusRequest("APPROVED", "AC-2024-RES-42", "Approved by Faculty")
                )
        );

        // 7. Valid transition: UNDER_REVIEW -> APPROVED with authorized DEAN role
        CurriculumVersionDto.CurriculumVersionResponse approved = governanceService.transitionStatus(
                approverId, v1.id(), new CurriculumVersionDto.TransitionStatusRequest("APPROVED", "AC-2024-RES-42", "Approved by Academic Council")
        );
        assertEquals("APPROVED", approved.lifecycleStatus());
        assertTrue(approved.isImmutable());
        assertNotNull(approved.approvedAt());

        // 8. Immutability Gate: verify that adding a course offering to APPROVED version is rejected
        assertThrows(IllegalStateException.class, () ->
                governanceService.addCourseToSemester(
                        new CurriculumCourseDto.AddCourseToSemesterRequest(v1.id(), sem2.id(), courseId2, 4, 3, 0, 2, false, null)
                )
        );

        // 9. Immutability Gate: verify that adding a semester to APPROVED version is rejected
        assertThrows(IllegalStateException.class, () ->
                governanceService.createSemester(new SemesterDto.CreateSemesterRequest(v1.id(), 3, "Semester 3", 20))
        );
    }

    @Test
    @DisplayName("Clone approved curriculum into new DRAFT preserving historical approved version")
    void testCloneApprovedCurriculum() {
        // Setup approved version with 1 semester and 1 course
        CurriculumVersionDto.CurriculumVersionResponse v1 = governanceService.createCurriculumVersion(
                userId,
                new CurriculumVersionDto.CreateCurriculumVersionRequest(programmeId, "2024-V1", 2024, 0, "SENATE")
        );
        SemesterDto.SemesterResponse sem1 = governanceService.createSemester(
                new SemesterDto.CreateSemesterRequest(v1.id(), 1, "Semester 1", 20)
        );
        governanceService.addCourseToSemester(
                new CurriculumCourseDto.AddCourseToSemesterRequest(v1.id(), sem1.id(), courseId1, 4, 3, 1, 0, false, null)
        );

        // Approve v1 using authorized approverId (DEAN)
        governanceService.transitionStatus(userId, v1.id(), new CurriculumVersionDto.TransitionStatusRequest("UNDER_REVIEW", null, null));
        CurriculumVersionDto.CurriculumVersionResponse approvedV1 = governanceService.transitionStatus(
                approverId, v1.id(), new CurriculumVersionDto.TransitionStatusRequest("APPROVED", "AC-100", "Approved")
        );
        assertTrue(approvedV1.isImmutable());

        // Clone as new DRAFT
        CurriculumVersionDto.CurriculumVersionResponse v2Draft = governanceService.cloneAsNewDraft(
                userId,
                v1.id(),
                new CurriculumVersionDto.CloneDraftRequest("2025-V2", 2025)
        );
        assertNotNull(v2Draft.id());
        assertEquals("2025-V2", v2Draft.versionCode());
        assertEquals("DRAFT", v2Draft.lifecycleStatus());
        assertFalse(v2Draft.isImmutable());

        // Historical approved version V1 remains APPROVED and immutable
        CurriculumVersionDto.CurriculumVersionResponse historicV1 = governanceService.getCurriculumVersion(v1.id());
        assertEquals("APPROVED", historicV1.lifecycleStatus());
        assertTrue(historicV1.isImmutable());

        // Cloned version has semesters and courses copied
        List<SemesterDto.SemesterResponse> clonedSemesters = governanceService.listSemesters(v2Draft.id());
        assertEquals(1, clonedSemesters.size());
        List<CurriculumCourseDto.CurriculumCourseResponse> clonedCourses = governanceService.listCoursesByCurriculumVersion(v2Draft.id());
        assertEquals(1, clonedCourses.size());
        assertEquals("AI101", clonedCourses.get(0).courseCode());

        // Cloned draft is mutable: can add courseId2
        CurriculumCourseDto.CurriculumCourseResponse addedCourse = governanceService.addCourseToSemester(
                new CurriculumCourseDto.AddCourseToSemesterRequest(v2Draft.id(), clonedSemesters.get(0).id(), courseId2, 4, 3, 0, 2, true, "PE-1")
        );
        assertNotNull(addedCourse.id());
        assertEquals(2, governanceService.listCoursesByCurriculumVersion(v2Draft.id()).size());
    }
}

