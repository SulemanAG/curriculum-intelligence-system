package org.curriculumai.academic;

import org.curriculumai.academic.dto.*;
import org.curriculumai.academic.service.AcademicCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PrerequisiteCycleTest {

    @Autowired
    private AcademicCatalogService catalogService;

    private String deptId;
    private String courseA;
    private String courseB;
    private String courseC;
    private String courseD;
    private String courseE;

    @BeforeEach
    void setupCourses() {
        InstitutionDto.InstitutionResponse inst = catalogService.createInstitution(
                new InstitutionDto.CreateInstitutionRequest("PREREQ-UNIV", "Prereq Test Univ", "Belagavi", "India", null, null)
        );
        DepartmentDto.DepartmentResponse dept = catalogService.createDepartment(
                new DepartmentDto.CreateDepartmentRequest(inst.id(), "CSE-PREREQ", "Computer Science")
        );
        this.deptId = dept.id();

        CourseDto.CourseResponse cA = catalogService.createCourse(
                new CourseDto.CreateCourseRequest(deptId, "CS101", "Programming Fundamentals", null, null, null, null, null, null)
        );
        CourseDto.CourseResponse cB = catalogService.createCourse(
                new CourseDto.CreateCourseRequest(deptId, "CS102", "Data Structures", null, null, null, null, null, null)
        );
        CourseDto.CourseResponse cC = catalogService.createCourse(
                new CourseDto.CreateCourseRequest(deptId, "CS103", "Algorithms", null, null, null, null, null, null)
        );
        CourseDto.CourseResponse cD = catalogService.createCourse(
                new CourseDto.CreateCourseRequest(deptId, "CS104", "Advanced Algorithms", null, null, null, null, null, null)
        );
        CourseDto.CourseResponse cE = catalogService.createCourse(
                new CourseDto.CreateCourseRequest(deptId, "CS105", "Computational Complexity", null, null, null, null, null, null)
        );

        this.courseA = cA.id();
        this.courseB = cB.id();
        this.courseC = cC.id();
        this.courseD = cD.id();
        this.courseE = cE.id();
    }

    @Test
    @DisplayName("Reject self-prerequisite (A -> A)")
    void testSelfLoopRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseA, courseA, "PREREQUISITE", "C"))
        );
        assertTrue(ex.getMessage().contains("cannot have itself as a prerequisite"));
    }

    @Test
    @DisplayName("Reject direct 2-node cycle (A -> B, then B -> A)")
    void testDirectTwoNodeCycleRejected() {
        // A requires B
        catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseA, courseB, "PREREQUISITE", "C"));

        // Attempt B requires A -> should fail
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseB, courseA, "PREREQUISITE", "C"))
        );
        assertTrue(ex.getMessage().contains("Cyclic prerequisite detected"));
    }

    @Test
    @DisplayName("Reject 3-node cycle (A -> B, B -> C, then C -> A)")
    void testThreeNodeCycleRejected() {
        // A requires B, B requires C
        catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseA, courseB, "PREREQUISITE", "C"));
        catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseB, courseC, "PREREQUISITE", "C"));

        // Attempt C requires A -> should fail because A indirectly requires C
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseC, courseA, "PREREQUISITE", "C"))
        );
        assertTrue(ex.getMessage().contains("Cyclic prerequisite detected"));
    }

    @Test
    @DisplayName("Reject deep multi-node cycle (A -> B -> C -> D -> E, then E -> A)")
    void testDeepCycleRejected() {
        catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseA, courseB, "PREREQUISITE", "C"));
        catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseB, courseC, "PREREQUISITE", "C"));
        catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseC, courseD, "PREREQUISITE", "C"));
        catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseD, courseE, "PREREQUISITE", "C"));

        // E requires A -> cycle of length 5
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseE, courseA, "PREREQUISITE", "C"))
        );
        assertTrue(ex.getMessage().contains("Cyclic prerequisite detected"));

        // E requires B -> cycle of length 4
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseE, courseB, "PREREQUISITE", "C"))
        );
        assertTrue(ex2.getMessage().contains("Cyclic prerequisite detected"));
    }

    @Test
    @DisplayName("Allow valid DAG with diamond branching (D -> B, D -> C, B -> A, C -> A)")
    void testValidDagDiamondAllowed() {
        // B requires A, C requires A
        catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseB, courseA, "PREREQUISITE", "C"));
        catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseC, courseA, "PREREQUISITE", "C"));

        // D requires B, D requires C (valid diamond DAG sharing prerequisite A)
        assertDoesNotThrow(() ->
                catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseD, courseB, "PREREQUISITE", "C"))
        );
        assertDoesNotThrow(() ->
                catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseD, courseC, "PREREQUISITE", "C"))
        );

        // Verify all 4 edges exist
        assertEquals(1, catalogService.listPrerequisitesForCourse(courseB).size());
        assertEquals(1, catalogService.listPrerequisitesForCourse(courseC).size());
        assertEquals(2, catalogService.listPrerequisitesForCourse(courseD).size());
    }

    @Test
    @DisplayName("Reject duplicate prerequisite edge")
    void testDuplicatePrerequisiteRejected() {
        catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseB, courseA, "PREREQUISITE", "C"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                catalogService.addPrerequisite(new PrerequisiteDto.CreatePrerequisiteRequest(courseB, courseA, "COREQUISITE", "B"))
        );
        assertTrue(ex.getMessage().contains("already exists"));
    }
}

