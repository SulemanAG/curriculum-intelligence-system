package org.curriculumai.academic;

import org.curriculumai.academic.domain.CurriculumOutcomeSnapshot;
import org.curriculumai.academic.dto.*;
import org.curriculumai.academic.repository.CurriculumOutcomeSnapshotRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class HistoricalOutcomePreservationTest {

    @Autowired
    private AcademicCatalogService catalogService;

    @Autowired
    private CurriculumGovernanceService governanceService;

    @Autowired
    private OutcomeMappingService outcomeService;

    @Autowired
    private CurriculumOutcomeSnapshotRepository snapshotRepository;

    @Autowired
    private UserRepository userRepository;

    private String programmeId;
    private String courseId;
    private final String facultyId = "usr-faculty-history";
    private final String deanId = "usr-dean-history";

    @BeforeEach
    void setupTestData() {
        if (!userRepository.existsById(facultyId)) {
            userRepository.save(new User(facultyId, "prof_smith", "smith@test.edu", "Prof. Smith", "FACULTY"));
        }
        if (!userRepository.existsById(deanId)) {
            userRepository.save(new User(deanId, "dean_jones", "jones@test.edu", "Dean Jones", "DEAN"));
        }

        InstitutionDto.InstitutionResponse inst = catalogService.createInstitution(
                new InstitutionDto.CreateInstitutionRequest("SNAP-UNIV", "Snapshot University", "Belagavi", "India", null, null)
        );
        DepartmentDto.DepartmentResponse dept = catalogService.createDepartment(
                new DepartmentDto.CreateDepartmentRequest(inst.id(), "CSE-SNAP", "Computer Science & Engineering")
        );
        ProgrammeDto.ProgrammeResponse prog = catalogService.createProgramme(
                new ProgrammeDto.CreateProgrammeRequest(dept.id(), "BTECH-CSE", "B.Tech Computer Science", "UG", 4, 160)
        );
        this.programmeId = prog.id();

        CourseDto.CourseResponse course = catalogService.createCourse(
                new CourseDto.CreateCourseRequest(dept.id(), "CS301", "Operating Systems", "Core OS course", "CORE", 4, 3, 1, 0)
        );
        this.courseId = course.id();
    }

    @Test
    @DisplayName("6-Step Regression Test: Modifying CO/PO cannot alter approved historical curriculum outcome snapshots")
    void testHistoricalOutcomePreservationAcrossRevisions() {
        // =========================================================================
        // STEP 1: Create draft curriculum version 2024-V1
        // =========================================================================
        CurriculumVersionDto.CurriculumVersionResponse v1 = governanceService.createCurriculumVersion(
                facultyId,
                new CurriculumVersionDto.CreateCurriculumVersionRequest(programmeId, "2024-V1", 2024, 0, "BOS_APPROVED")
        );
        SemesterDto.SemesterResponse sem1 = governanceService.createSemester(
                new SemesterDto.CreateSemesterRequest(v1.id(), 1, "Semester 1", 20)
        );
        governanceService.addCourseToSemester(
                new CurriculumCourseDto.AddCourseToSemesterRequest(v1.id(), sem1.id(), courseId, 4, 3, 1, 0, false, null)
        );
        assertEquals("DRAFT", v1.lifecycleStatus());

        // =========================================================================
        // STEP 2: Define Programme Outcomes (PO1, PO2), Course Outcomes (CO1, CO2), and CO-PO mappings
        // =========================================================================
        OutcomeDto.ProgrammeOutcomeResponse po1 = outcomeService.createProgrammeOutcome(
                new OutcomeDto.CreateProgrammeOutcomeRequest(programmeId, "PO1", "Engineering Knowledge", "Apply fundamental engineering principles", "PO")
        );
        OutcomeDto.ProgrammeOutcomeResponse po2 = outcomeService.createProgrammeOutcome(
                new OutcomeDto.CreateProgrammeOutcomeRequest(programmeId, "PO2", "Problem Analysis", "Analyze complex computing problems", "PO")
        );

        OutcomeDto.CourseOutcomeResponse co1 = outcomeService.createCourseOutcome(
                new OutcomeDto.CreateCourseOutcomeRequest(courseId, "CO1", "Understand process scheduling and concurrency", "APPLY")
        );
        OutcomeDto.CourseOutcomeResponse co2 = outcomeService.createCourseOutcome(
                new OutcomeDto.CreateCourseOutcomeRequest(courseId, "CO2", "Implement virtual memory management algorithms", "ANALYZE")
        );

        OutcomeDto.CoPoMappingResponse map1 = outcomeService.createCoPoMapping(
                new OutcomeDto.CreateCoPoMappingRequest(co1.id(), po1.id(), 3, "Direct application of scheduling theory")
        );
        OutcomeDto.CoPoMappingResponse map2 = outcomeService.createCoPoMapping(
                new OutcomeDto.CreateCoPoMappingRequest(co2.id(), po2.id(), 2, "Analytical memory management models")
        );

        // Before approval: zero snapshots exist
        assertEquals(0, snapshotRepository.findByCurriculumVersionId(v1.id()).size());

        // =========================================================================
        // STEP 3: Formally approve 2024-V1 with DEAN authority -> snapshots captured
        // =========================================================================
        governanceService.transitionStatus(
                facultyId, v1.id(), new CurriculumVersionDto.TransitionStatusRequest("UNDER_REVIEW", null, "Submitted for senate review")
        );
        CurriculumVersionDto.CurriculumVersionResponse approvedV1 = governanceService.transitionStatus(
                deanId, v1.id(), new CurriculumVersionDto.TransitionStatusRequest("APPROVED", "SENATE-RES-2024-01", "Formally approved by Senate")
        );
        assertEquals("APPROVED", approvedV1.lifecycleStatus());
        assertTrue(approvedV1.isImmutable());

        // Verify snapshots created for 2024-V1: 2 POs + 2 COs + 2 CO-PO mappings = 6 snapshots
        List<OutcomeDto.CurriculumOutcomeSnapshotResponse> v1Snapshots = governanceService.getCurriculumOutcomeSnapshots(v1.id());
        assertEquals(6, v1Snapshots.size());

        OutcomeDto.CurriculumOutcomeSnapshotResponse snapPo1 = v1Snapshots.stream()
                .filter(s -> "PROGRAMME_OUTCOME".equals(s.snapshotType()) && "PO1".equals(s.outcomeCode()))
                .findFirst().orElseThrow();
        assertEquals("Apply fundamental engineering principles", snapPo1.description());

        OutcomeDto.CurriculumOutcomeSnapshotResponse snapCo1 = v1Snapshots.stream()
                .filter(s -> "COURSE_OUTCOME".equals(s.snapshotType()) && "CO1".equals(s.outcomeCode()))
                .findFirst().orElseThrow();
        assertEquals("Understand process scheduling and concurrency", snapCo1.description());
        assertEquals("APPLY", snapCo1.bloomLevel());

        OutcomeDto.CurriculumOutcomeSnapshotResponse snapMap1 = v1Snapshots.stream()
                .filter(s -> "CO_PO_MAPPING".equals(s.snapshotType()) && "CO1".equals(s.outcomeCode()) && "PO1".equals(s.targetOutcomeCode()))
                .findFirst().orElseThrow();
        assertEquals(3, snapMap1.mappingLevel());
        assertEquals("Direct application of scheduling theory", snapMap1.justification());

        // =========================================================================
        // STEP 4: Clone approved 2024-V1 into a new mutable draft 2025-V2
        // =========================================================================
        CurriculumVersionDto.CurriculumVersionResponse v2Draft = governanceService.cloneAsNewDraft(
                facultyId,
                v1.id(),
                new CurriculumVersionDto.CloneDraftRequest("2025-V2", 2025)
        );
        assertEquals("DRAFT", v2Draft.lifecycleStatus());
        assertFalse(v2Draft.isImmutable());

        // =========================================================================
        // STEP 5: Mutate live CO and PO definitions for next academic cycle
        // =========================================================================
        // Modify CO1 description and Bloom level in catalog
        OutcomeDto.CourseOutcomeResponse mutatedCo1 = outcomeService.updateCourseOutcome(
                co1.id(),
                new OutcomeDto.UpdateCourseOutcomeRequest(
                        "MODIFIED: Design and build advanced concurrent lock-free synchronization kernels",
                        "CREATE"
                )
        );
        assertEquals("CREATE", mutatedCo1.bloomLevel());
        assertTrue(mutatedCo1.description().contains("MODIFIED"));

        // Modify PO1 title and description in catalog
        OutcomeDto.ProgrammeOutcomeResponse mutatedPo1 = outcomeService.updateProgrammeOutcome(
                po1.id(),
                new OutcomeDto.UpdateProgrammeOutcomeRequest(
                        "Engineering Knowledge & Modern Computing",
                        "MODIFIED: Apply advanced engineering and cloud-native distributed concepts",
                        "PO"
                )
        );
        assertTrue(mutatedPo1.description().contains("MODIFIED"));

        // Add a brand new CO3 to the course
        OutcomeDto.CourseOutcomeResponse co3 = outcomeService.createCourseOutcome(
                new OutcomeDto.CreateCourseOutcomeRequest(courseId, "CO3", "Design microkernels and hypervisors", "EVALUATE")
        );
        assertNotNull(co3.id());

        // =========================================================================
        // STEP 6: Assert that approved 2024-V1 historical snapshots remain 100% UNCHANGED
        // =========================================================================
        List<OutcomeDto.CurriculumOutcomeSnapshotResponse> v1PreservedSnapshots = governanceService.getCurriculumOutcomeSnapshots(v1.id());
        assertEquals(6, v1PreservedSnapshots.size(), "Snapshot count for approved curriculum must not change when catalog is edited");

        // Assert PO1 in 2024-V1 snapshot still has original description, NOT the modified description
        OutcomeDto.CurriculumOutcomeSnapshotResponse preservedPo1 = v1PreservedSnapshots.stream()
                .filter(s -> "PROGRAMME_OUTCOME".equals(s.snapshotType()) && "PO1".equals(s.outcomeCode()))
                .findFirst().orElseThrow();
        assertEquals("Apply fundamental engineering principles", preservedPo1.description());
        assertEquals("Engineering Knowledge", preservedPo1.title());
        assertFalse(preservedPo1.description().contains("MODIFIED"), "Approved historical PO snapshot must not reflect subsequent catalog mutations");

        // Assert CO1 in 2024-V1 snapshot still has original description and bloom level
        OutcomeDto.CurriculumOutcomeSnapshotResponse preservedCo1 = v1PreservedSnapshots.stream()
                .filter(s -> "COURSE_OUTCOME".equals(s.snapshotType()) && "CO1".equals(s.outcomeCode()))
                .findFirst().orElseThrow();
        assertEquals("Understand process scheduling and concurrency", preservedCo1.description());
        assertEquals("APPLY", preservedCo1.bloomLevel());
        assertFalse(preservedCo1.description().contains("MODIFIED"), "Approved historical CO snapshot must not reflect subsequent catalog mutations");

        // Assert CO3 is NOT in 2024-V1 snapshots
        boolean hasCo3 = v1PreservedSnapshots.stream().anyMatch(s -> "CO3".equals(s.outcomeCode()));
        assertFalse(hasCo3, "Newly added CO3 must not retroactively appear in approved 2024-V1 snapshots");

        // Assert CO-PO mapping in 2024-V1 snapshot is intact
        OutcomeDto.CurriculumOutcomeSnapshotResponse preservedMap1 = v1PreservedSnapshots.stream()
                .filter(s -> "CO_PO_MAPPING".equals(s.snapshotType()) && "CO1".equals(s.outcomeCode()))
                .findFirst().orElseThrow();
        assertEquals(3, preservedMap1.mappingLevel());
        assertEquals("Direct application of scheduling theory", preservedMap1.justification());
    }
}

