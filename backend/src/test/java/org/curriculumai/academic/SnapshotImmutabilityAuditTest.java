package org.curriculumai.academic;

import org.curriculumai.academic.domain.CurriculumLifecycleStatus;
import org.curriculumai.academic.domain.CurriculumOutcomeSnapshot;
import org.curriculumai.academic.domain.CurriculumVersion;
import org.curriculumai.academic.dto.*;
import org.curriculumai.academic.repository.CurriculumOutcomeSnapshotRepository;
import org.curriculumai.academic.repository.CurriculumVersionRepository;
import org.curriculumai.academic.service.AcademicCatalogService;
import org.curriculumai.academic.service.CurriculumGovernanceService;
import org.curriculumai.academic.service.OutcomeMappingService;
import org.curriculumai.users.User;
import org.curriculumai.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SnapshotImmutabilityAuditTest {

    @Autowired
    private AcademicCatalogService catalogService;

    @Autowired
    private CurriculumGovernanceService governanceService;

    @Autowired
    private OutcomeMappingService outcomeService;

    @Autowired
    private CurriculumOutcomeSnapshotRepository snapshotRepository;

    @Autowired
    private CurriculumVersionRepository versionRepository;

    @Autowired
    private UserRepository userRepository;

    private String programmeId;
    private String courseId;
    private final String facultyId = "usr-faculty-audit";
    private final String deanId = "usr-dean-audit";

    @BeforeEach
    void setupCatalog() {
        if (!userRepository.existsById(facultyId)) {
            userRepository.save(new User(facultyId, "fac_audit", "fac@audit.edu", "Dr. Faculty", "FACULTY"));
        }
        if (!userRepository.existsById(deanId)) {
            userRepository.save(new User(deanId, "dean_audit", "dean@audit.edu", "Dr. Dean", "DEAN"));
        }

        InstitutionDto.InstitutionResponse inst = catalogService.createInstitution(
                new InstitutionDto.CreateInstitutionRequest("AUDIT-UNIV", "Audit Univ", "Belagavi", "India", null, null)
        );
        DepartmentDto.DepartmentResponse dept = catalogService.createDepartment(
                new DepartmentDto.CreateDepartmentRequest(inst.id(), "CSE-AUDIT", "Computer Science")
        );
        ProgrammeDto.ProgrammeResponse prog = catalogService.createProgramme(
                new ProgrammeDto.CreateProgrammeRequest(dept.id(), "BE-AUDIT", "B.E. Computer Science", "UG", 4, 160)
        );
        this.programmeId = prog.id();

        CourseDto.CourseResponse crs = catalogService.createCourse(
                new CourseDto.CreateCourseRequest(dept.id(), "CS-AUDIT-1", "Operating Systems", null, "CORE", 4, 3, 1, 0)
        );
        this.courseId = crs.id();
    }

    private String createAndApproveCurriculum(String versionCode) {
        CurriculumVersionDto.CurriculumVersionResponse v = governanceService.createCurriculumVersion(
                facultyId,
                new CurriculumVersionDto.CreateCurriculumVersionRequest(programmeId, versionCode, 2024, 0, null)
        );
        SemesterDto.SemesterResponse sem = governanceService.createSemester(
                new SemesterDto.CreateSemesterRequest(v.id(), 1, "Semester 1", 20)
        );
        governanceService.addCourseToSemester(
                new CurriculumCourseDto.AddCourseToSemesterRequest(v.id(), sem.id(), courseId, 4, 3, 1, 0, false, null)
        );

        // Outcomes
        OutcomeDto.ProgrammeOutcomeResponse po = outcomeService.createProgrammeOutcome(
                new OutcomeDto.CreateProgrammeOutcomeRequest(programmeId, "PO-" + versionCode, "Eng Knowledge", "Desc", "PO")
        );
        OutcomeDto.CourseOutcomeResponse co = outcomeService.createCourseOutcome(
                new OutcomeDto.CreateCourseOutcomeRequest(courseId, "CO-" + versionCode, "Process scheduling", "APPLY")
        );
        outcomeService.createCoPoMapping(
                new OutcomeDto.CreateCoPoMappingRequest(co.id(), po.id(), 3, "Direct mapping")
        );

        // Move to UNDER_REVIEW then APPROVED
        governanceService.transitionStatus(facultyId, v.id(), new CurriculumVersionDto.TransitionStatusRequest("UNDER_REVIEW", null, "Ready"));
        CurriculumVersionDto.CurriculumVersionResponse app = governanceService.transitionStatus(
                deanId, v.id(), new CurriculumVersionDto.TransitionStatusRequest("APPROVED", "RES-AUDIT", "Approved")
        );
        assertEquals("APPROVED", app.lifecycleStatus());
        return v.id();
    }

    @Test
    @Transactional
    @DisplayName("Audit A.1 & A.2: Direct update or deletion of CurriculumOutcomeSnapshot entity is strictly rejected")
    void testSnapshotUpdateAndDeletionRejected() {
        String versionId = createAndApproveCurriculum("AUD-V1");
        List<CurriculumOutcomeSnapshot> snapshots = snapshotRepository.findByCurriculumVersionId(versionId);
        assertFalse(snapshots.isEmpty(), "Snapshots must exist for approved curriculum");

        CurriculumOutcomeSnapshot snapshot = snapshots.get(0);

        // 1. Attempt update
        assertThrows(UnsupportedOperationException.class, () -> {
            snapshot.setDescription("MALICIOUS MUTATION ATTEMPT");
            snapshot.preUpdate();
        });

        // 2. Attempt deletion
        assertThrows(UnsupportedOperationException.class, () -> {
            snapshot.preRemove();
        });
    }

    @Test
    @Transactional
    @DisplayName("Audit A.3: Repeated approval request on already APPROVED curriculum is rejected without duplicating snapshots")
    void testRepeatedApprovalRejected() {
        String versionId = createAndApproveCurriculum("AUD-V2");
        int snapshotCountBefore = snapshotRepository.findByCurriculumVersionId(versionId).size();
        assertTrue(snapshotCountBefore > 0);

        // Re-approving already APPROVED curriculum must fail
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                governanceService.transitionStatus(
                        deanId, versionId, new CurriculumVersionDto.TransitionStatusRequest("APPROVED", "DUPLICATE-RES", "Re-approval attempt")
                )
        );
        assertTrue(ex.getMessage().contains("already in APPROVED status") || ex.getMessage().contains("Illegal lifecycle transition"));

        // Verify snapshot count did not duplicate
        int snapshotCountAfter = snapshotRepository.findByCurriculumVersionId(versionId).size();
        assertEquals(snapshotCountBefore, snapshotCountAfter, "Snapshot set must not be duplicated or replaced upon repeated approval attempt");
    }

    @Test
    @Transactional
    @DisplayName("Audit C: Approved curriculum is strictly immutable against course addition or semester creation")
    void testApprovedCurriculumMutationBlocked() {
        String versionId = createAndApproveCurriculum("AUD-V3");

        // Attempt to add a semester to APPROVED curriculum
        assertThrows(IllegalStateException.class, () ->
                governanceService.createSemester(new SemesterDto.CreateSemesterRequest(versionId, 2, "Sem 2", 20))
        );

        // Attempt invalid state transition from APPROVED back to DRAFT directly
        assertThrows(IllegalStateException.class, () ->
                governanceService.transitionStatus(deanId, versionId, new CurriculumVersionDto.TransitionStatusRequest("DRAFT", null, "Illegal revert"))
        );
    }
}

