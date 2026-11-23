package org.curriculumai.academic;

import org.curriculumai.academic.dto.*;
import org.curriculumai.academic.service.AcademicCatalogService;
import org.curriculumai.academic.service.CurriculumGovernanceService;
import org.curriculumai.users.User;
import org.curriculumai.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ApprovalAuthorizationTest {

    @Autowired
    private AcademicCatalogService catalogService;

    @Autowired
    private CurriculumGovernanceService governanceService;

    @Autowired
    private UserRepository userRepository;

    private String programmeId;
    private final String facultyId = "usr-auth-faculty";
    private final String hodId = "usr-auth-hod";
    private final String deanId = "usr-auth-dean";
    private final String adminId = "usr-auth-admin";
    private final String unknownUserId = "usr-non-existent-user";

    @BeforeEach
    void setupUsersAndCurriculum() {
        if (!userRepository.existsById(facultyId)) {
            userRepository.save(new User(facultyId, "auth_faculty", "fac@auth.edu", "Dr. Faculty", "FACULTY"));
        }
        if (!userRepository.existsById(hodId)) {
            userRepository.save(new User(hodId, "auth_hod", "hod@auth.edu", "Dr. HoD", "HOD"));
        }
        if (!userRepository.existsById(deanId)) {
            userRepository.save(new User(deanId, "auth_dean", "dean@auth.edu", "Dr. Dean", "DEAN"));
        }
        if (!userRepository.existsById(adminId)) {
            userRepository.save(new User(adminId, "auth_admin", "admin@auth.edu", "Sys Admin", "ADMIN"));
        }

        InstitutionDto.InstitutionResponse inst = catalogService.createInstitution(
                new InstitutionDto.CreateInstitutionRequest("AUTH-UNIV", "Auth Test Univ", "Belagavi", "India", null, null)
        );
        DepartmentDto.DepartmentResponse dept = catalogService.createDepartment(
                new DepartmentDto.CreateDepartmentRequest(inst.id(), "CSE-AUTH", "Computer Science")
        );
        ProgrammeDto.ProgrammeResponse prog = catalogService.createProgramme(
                new ProgrammeDto.CreateProgrammeRequest(dept.id(), "BE-AUTH", "B.E. Computer Science", "UG", 4, 160)
        );
        this.programmeId = prog.id();
    }

    private String createCurriculumInUnderReview(String versionCode) {
        CurriculumVersionDto.CurriculumVersionResponse v = governanceService.createCurriculumVersion(
                facultyId,
                new CurriculumVersionDto.CreateCurriculumVersionRequest(programmeId, versionCode, 2024, 0, null)
        );
        governanceService.transitionStatus(
                facultyId, v.id(), new CurriculumVersionDto.TransitionStatusRequest("UNDER_REVIEW", null, "Ready for approval")
        );
        return v.id();
    }

    @Test
    @DisplayName("Reject approval transition when user role is FACULTY")
    void testFacultyCannotApproveCurriculum() {
        String versionId = createCurriculumInUnderReview("AUTH-V1");

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                governanceService.transitionStatus(
                        facultyId,
                        versionId,
                        new CurriculumVersionDto.TransitionStatusRequest("APPROVED", "RES-01", "Attempted faculty approval")
                )
        );
        assertTrue(ex.getMessage().contains("is not authorized to approve curriculum versions"));
        assertTrue(ex.getMessage().contains("FACULTY"));
    }

    @Test
    @DisplayName("Reject approval transition when user does not exist in the database")
    void testUnknownUserCannotApproveCurriculum() {
        String versionId = createCurriculumInUnderReview("AUTH-V2");

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                governanceService.transitionStatus(
                        unknownUserId,
                        versionId,
                        new CurriculumVersionDto.TransitionStatusRequest("APPROVED", "RES-02", "Unknown user attempt")
                )
        );
        assertTrue(ex.getMessage().contains("is not authorized to approve curriculum versions"));
    }

    @Test
    @DisplayName("Allow approval transition when user role is HOD")
    void testHodCanApproveCurriculum() {
        String versionId = createCurriculumInUnderReview("AUTH-V3");

        CurriculumVersionDto.CurriculumVersionResponse approved = governanceService.transitionStatus(
                hodId,
                versionId,
                new CurriculumVersionDto.TransitionStatusRequest("APPROVED", "HOD-RES-03", "Approved by Head of Department")
        );
        assertEquals("APPROVED", approved.lifecycleStatus());
        assertTrue(approved.isImmutable());
        assertNotNull(approved.approvedAt());
    }

    @Test
    @DisplayName("Allow approval transition when user role is DEAN")
    void testDeanCanApproveCurriculum() {
        String versionId = createCurriculumInUnderReview("AUTH-V4");

        CurriculumVersionDto.CurriculumVersionResponse approved = governanceService.transitionStatus(
                deanId,
                versionId,
                new CurriculumVersionDto.TransitionStatusRequest("APPROVED", "DEAN-RES-04", "Approved by Dean")
        );
        assertEquals("APPROVED", approved.lifecycleStatus());
        assertTrue(approved.isImmutable());
    }

    @Test
    @DisplayName("Allow approval transition when user role is ADMIN")
    void testAdminCanApproveCurriculum() {
        String versionId = createCurriculumInUnderReview("AUTH-V5");

        CurriculumVersionDto.CurriculumVersionResponse approved = governanceService.transitionStatus(
                adminId,
                versionId,
                new CurriculumVersionDto.TransitionStatusRequest("APPROVED", "ADMIN-RES-05", "Approved by Academic Admin")
        );
        assertEquals("APPROVED", approved.lifecycleStatus());
        assertTrue(approved.isImmutable());
    }
}

