package org.curriculumai.academic;

import org.curriculumai.academic.domain.Course;
import org.curriculumai.academic.domain.Programme;
import org.curriculumai.academic.dto.*;
import org.curriculumai.academic.repository.CourseRepository;
import org.curriculumai.academic.repository.ProgrammeRepository;
import org.curriculumai.academic.service.AcademicCatalogService;
import org.curriculumai.academic.service.AcademicImportService;
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
class NullAcademicDefaultsTest {

    @Autowired
    private AcademicCatalogService catalogService;

    @Autowired
    private AcademicImportService importService;

    @Autowired
    private ProgrammeRepository programmeRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    private String deptId;
    private String instCode;
    private final String userId = "usr-faculty-defaults";

    @BeforeEach
    void setupCatalog() {
        if (!userRepository.existsById(userId)) {
            userRepository.save(new User(userId, "faculty_def", "def@test.edu", "Dr. Defaults", "FACULTY"));
        }

        InstitutionDto.InstitutionResponse inst = catalogService.createInstitution(
                new InstitutionDto.CreateInstitutionRequest("DEF-UNIV", "Defaults Test Univ", "Belagavi", "India", null, null)
        );
        this.instCode = inst.code();

        DepartmentDto.DepartmentResponse dept = catalogService.createDepartment(
                new DepartmentDto.CreateDepartmentRequest(inst.id(), "CSE-DEF", "Computer Science")
        );
        this.deptId = dept.id();
    }

    @Test
    @DisplayName("Verify Programme preserves null for unknown degreeLevel and durationYears without synthetic defaults")
    void testProgrammeUnknownAttributesRemainNull() {
        // Create programme with null degreeLevel and null durationYears
        ProgrammeDto.CreateProgrammeRequest req = new ProgrammeDto.CreateProgrammeRequest(
                deptId, "PROG-UNKNOWN", "Unknown Level Programme", null, null, null
        );
        ProgrammeDto.ProgrammeResponse resp = catalogService.createProgramme(req);

        // Assert in response DTO
        assertNull(resp.degreeLevel(), "degreeLevel must be null when not provided, not defaulted to 'UG'");
        assertNull(resp.durationYears(), "durationYears must be null when not provided, not defaulted to 4");

        // Assert in persisted entity
        Programme entity = programmeRepository.findById(resp.id()).orElseThrow();
        assertNull(entity.getDegreeLevel());
        assertNull(entity.getDurationYears());
    }

    @Test
    @DisplayName("Verify Course preserves null for unknown hours and courseType without synthetic 0/CORE defaults")
    void testCourseUnknownAttributesRemainNull() {
        // Create course with null credits, null hours, null courseType
        CourseDto.CreateCourseRequest req = new CourseDto.CreateCourseRequest(
                deptId, "CRS-NULL-TEST", "Special Topics Seminar", null, null, null, null, null, null
        );
        CourseDto.CourseResponse resp = catalogService.createCourse(req);

        // Assert in response DTO
        assertNull(resp.courseType(), "courseType must be null when not provided, not defaulted to 'CORE'");
        assertNull(resp.defaultCredits());
        assertNull(resp.lectureHours(), "lectureHours must be null when not provided, not defaulted to 0");
        assertNull(resp.tutorialHours(), "tutorialHours must be null when not provided, not defaulted to 0");
        assertNull(resp.practicalHours(), "practicalHours must be null when not provided, not defaulted to 0");

        // Assert in persisted entity
        Course entity = courseRepository.findById(resp.id()).orElseThrow();
        assertNull(entity.getCourseType());
        assertNull(entity.getDefaultCredits());
        assertNull(entity.getLectureHours());
        assertNull(entity.getTutorialHours());
        assertNull(entity.getPracticalHours());
    }

    @Test
    @DisplayName("Verify AcademicImport preserves null attributes during batch commit")
    void testImportPreservesNullAttributes() {
        ImportDto.CourseImportItem item = new ImportDto.CourseImportItem(
                "CSE-DEF",
                "IMPORT-NULL-1",
                "Imported Course Without Hours",
                null, // description
                null, // courseType
                null, // credits
                null, // lectureHours
                null, // tutorialHours
                null  // practicalHours
        );

        ImportDto.AcademicImportRequest req = new ImportDto.AcademicImportRequest(
                "JSON", instCode, List.of(item)
        );

        ImportDto.ImportPreviewResponse preview = importService.createImportPreview(userId, req);
        assertEquals(1, preview.validRecords());
        assertEquals(0, preview.errorRecords());

        ImportDto.CommitImportResponse commit = importService.commitImport(userId, preview.batchId());
        assertEquals("COMMITTED", commit.status());
        assertEquals(1, commit.committedRecords());

        // Verify persisted course has null hours, NOT 0
        Course importedCourse = courseRepository.findByDepartmentIdAndCode(deptId, "IMPORT-NULL-1").orElseThrow();
        assertNull(importedCourse.getLectureHours(), "Imported course lectureHours must remain null when omitted");
        assertNull(importedCourse.getTutorialHours(), "Imported course tutorialHours must remain null when omitted");
        assertNull(importedCourse.getPracticalHours(), "Imported course practicalHours must remain null when omitted");
        assertNull(importedCourse.getCourseType(), "Imported course courseType must remain null when omitted");
    }

    @Test
    @DisplayName("Audit D: Explicit zero contact hours is preserved and distinguishable from unknown/null")
    void testExplicitZeroHoursDistinguishableFromNull() {
        // Course with explicit 0 contact hours (e.g. self-study / online module)
        CourseDto.CreateCourseRequest zeroReq = new CourseDto.CreateCourseRequest(
                deptId, "CRS-ZERO-TEST", "Independent Study Seminar", "Self-guided", "PROJECT", 2, 0, 0, 0
        );
        CourseDto.CourseResponse zeroResp = catalogService.createCourse(zeroReq);

        assertEquals(0, zeroResp.lectureHours(), "Explicit 0 lecture hours must remain 0, not coerced to null");
        assertEquals(0, zeroResp.tutorialHours(), "Explicit 0 tutorial hours must remain 0, not coerced to null");
        assertEquals(0, zeroResp.practicalHours(), "Explicit 0 practical hours must remain 0, not coerced to null");

        Course zeroEntity = courseRepository.findById(zeroResp.id()).orElseThrow();
        assertEquals(Integer.valueOf(0), zeroEntity.getLectureHours());
        assertEquals(Integer.valueOf(0), zeroEntity.getTutorialHours());
        assertEquals(Integer.valueOf(0), zeroEntity.getPracticalHours());

        // Contrast with course where hours are unknown/null
        CourseDto.CreateCourseRequest nullReq = new CourseDto.CreateCourseRequest(
                deptId, "CRS-UNKNOWN-TEST", "Emerging Tech Seminar", "Hours TBD", null, null, null, null, null
        );
        CourseDto.CourseResponse nullResp = catalogService.createCourse(nullReq);
        assertNull(nullResp.lectureHours(), "Missing lecture hours must remain null");
        assertNull(nullResp.tutorialHours(), "Missing tutorial hours must remain null");
        assertNull(nullResp.practicalHours(), "Missing practical hours must remain null");

        Course nullEntity = courseRepository.findById(nullResp.id()).orElseThrow();
        assertNull(nullEntity.getLectureHours());
        assertNull(nullEntity.getTutorialHours());
        assertNull(nullEntity.getPracticalHours());
    }

    @Test
    @DisplayName("Audit D: Import preview does not persist records before explicit commit, and repeated commit is rejected")
    void testImportPreviewIsolationAndRepeatedCommitRejection() {
        ImportDto.CourseImportItem item = new ImportDto.CourseImportItem(
                "CSE-DEF",
                "IMPORT-ISOLATION-1",
                "Preview Isolation Test Course",
                "Description",
                "CORE",
                4, 3, 1, 0
        );

        ImportDto.AcademicImportRequest req = new ImportDto.AcademicImportRequest(
                "JSON", instCode, List.of(item)
        );

        // 1. Stage Preview
        ImportDto.ImportPreviewResponse preview = importService.createImportPreview(userId, req);
        assertEquals(1, preview.validRecords());

        // Assert that courses table DOES NOT contain the course yet
        assertFalse(courseRepository.existsByDepartmentIdAndCode(deptId, "IMPORT-ISOLATION-1"),
                "Staging import preview must not persist academic catalog courses before explicit commit");

        // 2. Explicit Commit
        ImportDto.CommitImportResponse commit = importService.commitImport(userId, preview.batchId());
        assertEquals(1, commit.committedRecords());
        assertTrue(courseRepository.existsByDepartmentIdAndCode(deptId, "IMPORT-ISOLATION-1"));

        // 3. Repeated Commit Attempt on already COMMITTED batch must fail
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                importService.commitImport(userId, preview.batchId())
        );
        assertTrue(ex.getMessage().contains("is in status 'COMMITTED' and cannot be committed"),
                "Accidental repeated commit of an import batch must be strictly rejected");
    }
}
