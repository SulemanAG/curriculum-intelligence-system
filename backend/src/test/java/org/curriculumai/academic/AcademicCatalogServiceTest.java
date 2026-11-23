package org.curriculumai.academic;

import org.curriculumai.academic.dto.CourseDto;
import org.curriculumai.academic.dto.DepartmentDto;
import org.curriculumai.academic.dto.InstitutionDto;
import org.curriculumai.academic.dto.PrerequisiteDto;
import org.curriculumai.academic.service.AcademicCatalogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AcademicCatalogServiceTest {

    @Autowired
    private AcademicCatalogService catalogService;

    @Test
    @DisplayName("Create institution, department, and course with uniqueness constraints")
    void testCatalogCreationAndUniqueness() {
        // 1. Institution
        InstitutionDto.CreateInstitutionRequest instReq = new InstitutionDto.CreateInstitutionRequest(
                "TEST-UNIV", "Test State University", "Test City", "India", "https://test.edu", null
        );
        InstitutionDto.InstitutionResponse inst = catalogService.createInstitution(instReq);
        assertNotNull(inst.id());
        assertEquals("TEST-UNIV", inst.code());

        // Attempt duplicate institution code
        assertThrows(IllegalArgumentException.class, () -> catalogService.createInstitution(instReq));

        // 2. Department
        DepartmentDto.CreateDepartmentRequest deptReq = new DepartmentDto.CreateDepartmentRequest(
                inst.id(), "CSE", "Computer Science & Engineering"
        );
        DepartmentDto.DepartmentResponse dept = catalogService.createDepartment(deptReq);
        assertNotNull(dept.id());
        assertEquals("CSE", dept.code());

        // Attempt duplicate department code within same institution
        assertThrows(IllegalArgumentException.class, () -> catalogService.createDepartment(deptReq));

        // 3. Course
        CourseDto.CreateCourseRequest crsReq = new CourseDto.CreateCourseRequest(
                dept.id(), "CS201", "Data Structures", "Core CS Course", "CORE", 4, 3, 1, 0
        );
        CourseDto.CourseResponse course = catalogService.createCourse(crsReq);
        assertNotNull(course.id());
        assertEquals("CS201", course.code());
        assertEquals(4, course.defaultCredits());

        // Attempt duplicate course code within department
        assertThrows(IllegalArgumentException.class, () -> catalogService.createCourse(crsReq));
    }

    @Test
    @DisplayName("Course prerequisites enforce self-reference check and duplication check")
    void testPrerequisites() {
        InstitutionDto.InstitutionResponse inst = catalogService.createInstitution(
                new InstitutionDto.CreateInstitutionRequest("PREREQ-UNIV", "Prereq University", "City", "India", null, null)
        );
        DepartmentDto.DepartmentResponse dept = catalogService.createDepartment(
                new DepartmentDto.CreateDepartmentRequest(inst.id(), "ECE", "Electronics & Communication")
        );

        CourseDto.CourseResponse c1 = catalogService.createCourse(
                new CourseDto.CreateCourseRequest(dept.id(), "EC101", "Basic Electronics", null, "CORE", 3, 3, 0, 0)
        );
        CourseDto.CourseResponse c2 = catalogService.createCourse(
                new CourseDto.CreateCourseRequest(dept.id(), "EC201", "Signals & Systems", null, "CORE", 4, 3, 1, 0)
        );

        // Self-prerequisite check
        assertThrows(IllegalArgumentException.class, () ->
                catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(c1.id(), c1.id(), "HARD_PREREQUISITE", null))
        );

        // Valid prerequisite: EC201 requires EC101
        PrerequisiteDto.CoursePrerequisiteResponse prereq = catalogService.addPrerequisite(
                new PrerequisiteDto.CreatePrerequisiteRequest(c2.id(), c1.id(), "HARD_PREREQUISITE", "C")
        );
        assertNotNull(prereq.id());
        assertEquals("EC201", prereq.courseCode());
        assertEquals("EC101", prereq.prerequisiteCourseCode());

        // Duplicate prerequisite check
        assertThrows(IllegalArgumentException.class, () ->
                catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(c2.id(), c1.id(), "HARD_PREREQUISITE", null))
        );

        List<PrerequisiteDto.CoursePrerequisiteResponse> list = catalogService.listPrerequisitesForCourse(c2.id());
        assertEquals(1, list.size());
    }
}

