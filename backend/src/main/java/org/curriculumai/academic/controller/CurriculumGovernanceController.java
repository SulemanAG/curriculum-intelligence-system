package org.curriculumai.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.curriculumai.academic.dto.*;
import org.curriculumai.academic.service.CurriculumGovernanceService;
import org.curriculumai.common.ApiResponse;
import org.curriculumai.security.UserIdentityResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic/curricula")
@Tag(name = "Curriculum Governance", description = "Lifecycle Management, Approval Workflows, Semesters and Course Allocations")
public class CurriculumGovernanceController {

    private final CurriculumGovernanceService governanceService;
    private final UserIdentityResolver userIdentityResolver;

    public CurriculumGovernanceController(CurriculumGovernanceService governanceService, UserIdentityResolver userIdentityResolver) {
        this.governanceService = governanceService;
        this.userIdentityResolver = userIdentityResolver;
    }

    private String resolveUserId(String devHeader) {
        return userIdentityResolver.resolveUserId(devHeader);
    }

    @PostMapping
    @Operation(summary = "Initialize a new curriculum version in DRAFT state")
    public ResponseEntity<ApiResponse<CurriculumVersionDto.CurriculumVersionResponse>> createCurriculumVersion(
            @RequestHeader(value = "X-User-Id", defaultValue = "usr-default-faculty") String devUserId,
            @Valid @RequestBody CurriculumVersionDto.CreateCurriculumVersionRequest request
    ) {
        CurriculumVersionDto.CurriculumVersionResponse res = governanceService.createCurriculumVersion(resolveUserId(devUserId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res, "Curriculum version initialized in DRAFT state."));
    }

    @GetMapping
    @Operation(summary = "List curriculum versions for an academic programme")
    public ResponseEntity<ApiResponse<List<CurriculumVersionDto.CurriculumVersionResponse>>> listVersions(
            @RequestParam("programmeId") String programmeId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(governanceService.listVersionsByProgramme(programmeId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve curriculum version details by ID")
    public ResponseEntity<ApiResponse<CurriculumVersionDto.CurriculumVersionResponse>> getVersion(@PathVariable("id") String id) {
        return ResponseEntity.ok(ApiResponse.ok(governanceService.getCurriculumVersion(id)));
    }

    @PostMapping("/{id}/transition")
    @Operation(summary = "Transition curriculum lifecycle (DRAFT -> UNDER_REVIEW -> APPROVED -> ARCHIVED)")
    public ResponseEntity<ApiResponse<CurriculumVersionDto.CurriculumVersionResponse>> transitionStatus(
            @RequestHeader(value = "X-User-Id", defaultValue = "usr-default-faculty") String devUserId,
            @PathVariable("id") String id,
            @Valid @RequestBody CurriculumVersionDto.TransitionStatusRequest request
    ) {
        CurriculumVersionDto.CurriculumVersionResponse res = governanceService.transitionStatus(resolveUserId(devUserId), id, request);
        return ResponseEntity.ok(ApiResponse.ok(res, "Curriculum lifecycle status updated to " + res.lifecycleStatus()));
    }

    @PostMapping("/{id}/clone-draft")
    @Operation(summary = "Clone an approved or existing curriculum into a new editable DRAFT revision")
    public ResponseEntity<ApiResponse<CurriculumVersionDto.CurriculumVersionResponse>> cloneDraft(
            @RequestHeader(value = "X-User-Id", defaultValue = "usr-default-faculty") String devUserId,
            @PathVariable("id") String id,
            @Valid @RequestBody CurriculumVersionDto.CloneDraftRequest request
    ) {
        CurriculumVersionDto.CurriculumVersionResponse res = governanceService.cloneAsNewDraft(resolveUserId(devUserId), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res, "Cloned new curriculum revision in DRAFT state. Historical version preserved."));
    }

    // --- Semesters ---
    @PostMapping("/{id}/semesters")
    @Operation(summary = "Add a semester structure to a draft curriculum")
    public ResponseEntity<ApiResponse<SemesterDto.SemesterResponse>> createSemester(
            @PathVariable("id") String curriculumVersionId,
            @Valid @RequestBody SemesterDto.CreateSemesterRequest request
    ) {
        if (!curriculumVersionId.equals(request.curriculumVersionId())) {
            throw new IllegalArgumentException("Path variable curriculumId does not match request body curriculumVersionId.");
        }
        SemesterDto.SemesterResponse res = governanceService.createSemester(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res, "Semester added to curriculum."));
    }

    @GetMapping("/{id}/semesters")
    @Operation(summary = "List semesters in a curriculum version")
    public ResponseEntity<ApiResponse<List<SemesterDto.SemesterResponse>>> listSemesters(@PathVariable("id") String id) {
        return ResponseEntity.ok(ApiResponse.ok(governanceService.listSemesters(id)));
    }

    // --- Curriculum Courses (Offerings) ---
    @PostMapping("/{id}/courses")
    @Operation(summary = "Allocate a course offering to a semester (Mutable drafts only)")
    public ResponseEntity<ApiResponse<CurriculumCourseDto.CurriculumCourseResponse>> addCourse(
            @PathVariable("id") String curriculumVersionId,
            @Valid @RequestBody CurriculumCourseDto.AddCourseToSemesterRequest request
    ) {
        if (!curriculumVersionId.equals(request.curriculumVersionId())) {
            throw new IllegalArgumentException("Path variable curriculumId does not match request body curriculumVersionId.");
        }
        CurriculumCourseDto.CurriculumCourseResponse res = governanceService.addCourseToSemester(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res, "Course offering added to semester."));
    }

    @DeleteMapping("/{id}/courses/{courseOfferingId}")
    @Operation(summary = "Remove a course offering from a semester (Mutable drafts only)")
    public ResponseEntity<ApiResponse<Void>> removeCourse(
            @PathVariable("id") String curriculumVersionId,
            @PathVariable("courseOfferingId") String courseOfferingId
    ) {
        governanceService.removeCourseFromSemester(curriculumVersionId, courseOfferingId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Course offering removed from curriculum version."));
    }

    @GetMapping("/{id}/courses")
    @Operation(summary = "List all course offerings in a curriculum version")
    public ResponseEntity<ApiResponse<List<CurriculumCourseDto.CurriculumCourseResponse>>> listCourses(
            @PathVariable("id") String id,
            @RequestParam(value = "semesterId", required = false) String semesterId
    ) {
        if (semesterId != null && !semesterId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok(governanceService.listCoursesBySemester(semesterId)));
        }
        return ResponseEntity.ok(ApiResponse.ok(governanceService.listCoursesByCurriculumVersion(id)));
    }

    @GetMapping("/{id}/snapshots")
    @Operation(summary = "List immutable outcome snapshots for an approved curriculum version")
    public ResponseEntity<ApiResponse<List<OutcomeDto.CurriculumOutcomeSnapshotResponse>>> listOutcomeSnapshots(
            @PathVariable("id") String id
    ) {
        return ResponseEntity.ok(ApiResponse.ok(governanceService.getCurriculumOutcomeSnapshots(id)));
    }
}

