package org.curriculumai.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.curriculumai.academic.dto.*;
import org.curriculumai.academic.service.AcademicCatalogService;
import org.curriculumai.common.ApiResponse;
import org.curriculumai.security.UserIdentityResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic")
@Tag(name = "Academic Catalog", description = "Institutions, Departments, Programmes, Courses and Prerequisites")
public class AcademicCatalogController {

    private final AcademicCatalogService catalogService;
    private final UserIdentityResolver userIdentityResolver;

    public AcademicCatalogController(AcademicCatalogService catalogService, UserIdentityResolver userIdentityResolver) {
        this.catalogService = catalogService;
        this.userIdentityResolver = userIdentityResolver;
    }

    // --- Institutions ---
    @PostMapping("/institutions")
    @Operation(summary = "Register an educational institution")
    public ResponseEntity<ApiResponse<InstitutionDto.InstitutionResponse>> createInstitution(
            @Valid @RequestBody InstitutionDto.CreateInstitutionRequest request
    ) {
        InstitutionDto.InstitutionResponse res = catalogService.createInstitution(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res, "Institution registered."));
    }

    @GetMapping("/institutions")
    @Operation(summary = "List all registered institutions")
    public ResponseEntity<ApiResponse<List<InstitutionDto.InstitutionResponse>>> listInstitutions() {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.listInstitutions()));
    }

    @GetMapping("/institutions/{id}")
    @Operation(summary = "Get institution details by ID")
    public ResponseEntity<ApiResponse<InstitutionDto.InstitutionResponse>> getInstitution(@PathVariable("id") String id) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getInstitution(id)));
    }

    // --- Departments ---
    @PostMapping("/departments")
    @Operation(summary = "Create an academic department")
    public ResponseEntity<ApiResponse<DepartmentDto.DepartmentResponse>> createDepartment(
            @Valid @RequestBody DepartmentDto.CreateDepartmentRequest request
    ) {
        DepartmentDto.DepartmentResponse res = catalogService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res, "Department created."));
    }

    @GetMapping("/departments")
    @Operation(summary = "List departments for an institution")
    public ResponseEntity<ApiResponse<List<DepartmentDto.DepartmentResponse>>> listDepartments(
            @RequestParam("institutionId") String institutionId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.listDepartmentsByInstitution(institutionId)));
    }

    @GetMapping("/departments/{id}")
    @Operation(summary = "Get department details by ID")
    public ResponseEntity<ApiResponse<DepartmentDto.DepartmentResponse>> getDepartment(@PathVariable("id") String id) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getDepartment(id)));
    }

    // --- Programmes ---
    @PostMapping("/programmes")
    @Operation(summary = "Create an academic degree programme")
    public ResponseEntity<ApiResponse<ProgrammeDto.ProgrammeResponse>> createProgramme(
            @Valid @RequestBody ProgrammeDto.CreateProgrammeRequest request
    ) {
        ProgrammeDto.ProgrammeResponse res = catalogService.createProgramme(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res, "Programme created."));
    }

    @GetMapping("/programmes")
    @Operation(summary = "List programmes offered by a department")
    public ResponseEntity<ApiResponse<List<ProgrammeDto.ProgrammeResponse>>> listProgrammes(
            @RequestParam("departmentId") String departmentId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.listProgrammesByDepartment(departmentId)));
    }

    @GetMapping("/programmes/{id}")
    @Operation(summary = "Get programme details by ID")
    public ResponseEntity<ApiResponse<ProgrammeDto.ProgrammeResponse>> getProgramme(@PathVariable("id") String id) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getProgramme(id)));
    }

    // --- Courses ---
    @PostMapping("/courses")
    @Operation(summary = "Define a canonical course in a department catalog")
    public ResponseEntity<ApiResponse<CourseDto.CourseResponse>> createCourse(
            @Valid @RequestBody CourseDto.CreateCourseRequest request
    ) {
        CourseDto.CourseResponse res = catalogService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res, "Course created in catalog."));
    }

    @GetMapping("/courses")
    @Operation(summary = "List courses with optional department filter and pagination")
    public ResponseEntity<ApiResponse<Page<CourseDto.CourseResponse>>> listCourses(
            @RequestParam(value = "departmentId", required = false) String departmentId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("code").ascending());
        return ResponseEntity.ok(ApiResponse.ok(catalogService.listCoursesPaged(departmentId, pageRequest)));
    }

    @GetMapping("/courses/{id}")
    @Operation(summary = "Get course catalog details by ID")
    public ResponseEntity<ApiResponse<CourseDto.CourseResponse>> getCourse(@PathVariable("id") String id) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getCourse(id)));
    }

    // --- Prerequisites ---
    @PostMapping("/prerequisites")
    @Operation(summary = "Add a prerequisite rule between courses")
    public ResponseEntity<ApiResponse<PrerequisiteDto.CoursePrerequisiteResponse>> addPrerequisite(
            @Valid @RequestBody PrerequisiteDto.CreatePrerequisiteRequest request
    ) {
        PrerequisiteDto.CoursePrerequisiteResponse res = catalogService.addPrerequisite(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res, "Prerequisite defined."));
    }

    @GetMapping("/prerequisites")
    @Operation(summary = "List prerequisites for a course")
    public ResponseEntity<ApiResponse<List<PrerequisiteDto.CoursePrerequisiteResponse>>> listPrerequisites(
            @RequestParam("courseId") String courseId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.listPrerequisitesForCourse(courseId)));
    }
}

