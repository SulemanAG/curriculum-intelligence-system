package org.curriculumai.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.curriculumai.academic.dto.OutcomeDto;
import org.curriculumai.academic.service.OutcomeMappingService;
import org.curriculumai.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic/outcomes")
@Tag(name = "Outcome Mapping", description = "Course Outcomes (CO), Programme Outcomes (PO/PSO) and CO-PO Matrices")
public class OutcomeMappingController {

    private final OutcomeMappingService outcomeService;

    public OutcomeMappingController(OutcomeMappingService outcomeService) {
        this.outcomeService = outcomeService;
    }

    // --- Programme Outcomes ---
    @PostMapping("/programmes")
    @Operation(summary = "Define a Programme Outcome (PO) or Programme Specific Outcome (PSO)")
    public ResponseEntity<ApiResponse<OutcomeDto.ProgrammeOutcomeResponse>> createProgrammeOutcome(
            @Valid @RequestBody OutcomeDto.CreateProgrammeOutcomeRequest request
    ) {
        OutcomeDto.ProgrammeOutcomeResponse res = outcomeService.createProgrammeOutcome(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res, "Programme outcome defined."));
    }

    @GetMapping("/programmes")
    @Operation(summary = "List programme outcomes for a degree programme")
    public ResponseEntity<ApiResponse<List<OutcomeDto.ProgrammeOutcomeResponse>>> listProgrammeOutcomes(
            @RequestParam("programmeId") String programmeId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(outcomeService.listProgrammeOutcomes(programmeId)));
    }

    @PutMapping("/programmes/{id}")
    @Operation(summary = "Update a Programme Outcome definition")
    public ResponseEntity<ApiResponse<OutcomeDto.ProgrammeOutcomeResponse>> updateProgrammeOutcome(
            @PathVariable("id") String id,
            @RequestBody OutcomeDto.UpdateProgrammeOutcomeRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(outcomeService.updateProgrammeOutcome(id, request), "Programme outcome updated."));
    }

    // --- Course Outcomes ---
    @PostMapping("/courses")
    @Operation(summary = "Define a Course Outcome (CO) for a course")
    public ResponseEntity<ApiResponse<OutcomeDto.CourseOutcomeResponse>> createCourseOutcome(
            @Valid @RequestBody OutcomeDto.CreateCourseOutcomeRequest request
    ) {
        OutcomeDto.CourseOutcomeResponse res = outcomeService.createCourseOutcome(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res, "Course outcome defined."));
    }

    @GetMapping("/courses")
    @Operation(summary = "List course outcomes for a course")
    public ResponseEntity<ApiResponse<List<OutcomeDto.CourseOutcomeResponse>>> listCourseOutcomes(
            @RequestParam("courseId") String courseId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(outcomeService.listCourseOutcomes(courseId)));
    }

    @PutMapping("/courses/{id}")
    @Operation(summary = "Update a Course Outcome definition")
    public ResponseEntity<ApiResponse<OutcomeDto.CourseOutcomeResponse>> updateCourseOutcome(
            @PathVariable("id") String id,
            @RequestBody OutcomeDto.UpdateCourseOutcomeRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(outcomeService.updateCourseOutcome(id, request), "Course outcome updated."));
    }

    // --- CO-PO Mappings ---
    @PostMapping("/mappings")
    @Operation(summary = "Map a Course Outcome to a Programme Outcome with strength level (1, 2, or 3)")
    public ResponseEntity<ApiResponse<OutcomeDto.CoPoMappingResponse>> createCoPoMapping(
            @Valid @RequestBody OutcomeDto.CreateCoPoMappingRequest request
    ) {
        OutcomeDto.CoPoMappingResponse res = outcomeService.createCoPoMapping(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res, "CO-PO mapping established."));
    }

    @GetMapping("/mappings")
    @Operation(summary = "List CO-PO mappings filtered by Course Outcome or Programme Outcome")
    public ResponseEntity<ApiResponse<List<OutcomeDto.CoPoMappingResponse>>> listMappings(
            @RequestParam(value = "courseOutcomeId", required = false) String courseOutcomeId,
            @RequestParam(value = "programmeOutcomeId", required = false) String programmeOutcomeId
    ) {
        if (courseOutcomeId != null && !courseOutcomeId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok(outcomeService.listMappingsByCourseOutcome(courseOutcomeId)));
        }
        if (programmeOutcomeId != null && !programmeOutcomeId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok(outcomeService.listMappingsByProgrammeOutcome(programmeOutcomeId)));
        }
        throw new IllegalArgumentException("Either courseOutcomeId or programmeOutcomeId must be provided as a query parameter.");
    }
}

