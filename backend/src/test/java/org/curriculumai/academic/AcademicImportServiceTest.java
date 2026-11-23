package org.curriculumai.academic;

import org.curriculumai.academic.dto.*;
import org.curriculumai.academic.service.AcademicCatalogService;
import org.curriculumai.academic.service.AcademicImportService;
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
class AcademicImportServiceTest {

    @Autowired
    private AcademicImportService importService;

    @Autowired
    private AcademicCatalogService catalogService;

    private final String userId = "usr-default-faculty";

    @BeforeEach
    void setupInstitution() {
        InstitutionDto.InstitutionResponse inst = catalogService.createInstitution(
                new InstitutionDto.CreateInstitutionRequest("IMPORT-UNIV", "Import University", "Belagavi", "India", null, null)
        );
        catalogService.createDepartment(
                new DepartmentDto.CreateDepartmentRequest(inst.id(), "CSE", "Computer Science")
        );
    }

    @Test
    @DisplayName("Import validation detects missing fields, duplicates, and uncommitted preview")
    void testImportPreviewValidation() {
        List<ImportDto.CourseImportItem> items = List.of(
                // 1. Valid course
                new ImportDto.CourseImportItem("CSE", "CS101", "Programming Fundamentals", "Intro C", "CORE", 4, 3, 0, 2),
                // 2. Duplicate course code within same payload
                new ImportDto.CourseImportItem("CSE", "CS101", "Duplicate Programming", null, "CORE", 4, 3, 0, 2),
                // 3. Missing course code
                new ImportDto.CourseImportItem("CSE", "", "Unnamed Course", null, "CORE", 3, 3, 0, 0),
                // 4. Missing title
                new ImportDto.CourseImportItem("CSE", "CS102", "", null, "CORE", 3, 3, 0, 0),
                // 5. Non-existent department
                new ImportDto.CourseImportItem("NON_EXISTENT_DEPT", "CS103", "Valid Title", null, "CORE", 3, 3, 0, 0)
        );

        ImportDto.AcademicImportRequest req = new ImportDto.AcademicImportRequest("JSON", "IMPORT-UNIV", items);
        ImportDto.ImportPreviewResponse preview = importService.createImportPreview(userId, req);

        assertNotNull(preview.batchId());
        assertEquals("PREVIEW", preview.status());
        assertEquals(5, preview.totalRecords());
        assertEquals(1, preview.validRecords()); // Only the first record is valid
        assertEquals(4, preview.errorRecords()); // 4 records have errors
        assertTrue(preview.validationErrors().size() >= 4);

        // Verification of safe preview: Course CS101 must NOT be committed yet
        List<CourseDto.CourseResponse> coursesBeforeCommit = catalogService.listCoursesByDepartment(
                catalogService.listInstitutions().get(0).id()
        );
        assertTrue(coursesBeforeCommit.isEmpty());

        // Commit valid items
        ImportDto.CommitImportResponse commitRes = importService.commitImport(userId, preview.batchId());
        assertEquals("COMMITTED", commitRes.status());
        assertEquals(1, commitRes.committedRecords());

        // Now CS101 exists in database
        CourseDto.CourseResponse committedCourse = catalogService.getCourse(
                catalogService.listCoursesPaged(null, org.springframework.data.domain.PageRequest.of(0, 10))
                        .getContent().get(0).id()
        );
        assertEquals("CS101", committedCourse.code());

        // Attempting to re-commit an already COMMITTED batch throws exception
        assertThrows(IllegalStateException.class, () -> importService.commitImport(userId, preview.batchId()));
    }
}

