package org.curriculumai.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.curriculumai.academic.dto.ImportDto;
import org.curriculumai.academic.service.AcademicImportService;
import org.curriculumai.common.ApiResponse;
import org.curriculumai.security.UserIdentityResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/academic/import")
@Tag(name = "Academic Import", description = "Safe Structured Import Foundation (Preview, Validation & Confirmed Commit)")
public class AcademicImportController {

    private final AcademicImportService importService;
    private final UserIdentityResolver userIdentityResolver;

    public AcademicImportController(AcademicImportService importService, UserIdentityResolver userIdentityResolver) {
        this.importService = importService;
        this.userIdentityResolver = userIdentityResolver;
    }

    private String resolveUserId(String devHeader) {
        return userIdentityResolver.resolveUserId(devHeader);
    }

    @PostMapping("/preview")
    @Operation(summary = "Validate import payload, report missing information/duplicates, and produce an import preview")
    public ResponseEntity<ApiResponse<ImportDto.ImportPreviewResponse>> createPreview(
            @RequestHeader(value = "X-User-Id", defaultValue = "usr-default-faculty") String devUserId,
            @Valid @RequestBody ImportDto.AcademicImportRequest request
    ) {
        ImportDto.ImportPreviewResponse res = importService.createImportPreview(resolveUserId(devUserId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res, "Import preview generated."));
    }

    @PostMapping("/{batchId}/commit")
    @Operation(summary = "Explicitly confirm and commit a validated import batch to the academic catalog")
    public ResponseEntity<ApiResponse<ImportDto.CommitImportResponse>> commitBatch(
            @RequestHeader(value = "X-User-Id", defaultValue = "usr-default-faculty") String devUserId,
            @PathVariable("batchId") String batchId
    ) {
        ImportDto.CommitImportResponse res = importService.commitImport(resolveUserId(devUserId), batchId);
        return ResponseEntity.ok(ApiResponse.ok(res, res.message()));
    }

    @GetMapping("/{batchId}")
    @Operation(summary = "Inspect an import batch preview and its validation report")
    public ResponseEntity<ApiResponse<ImportDto.ImportPreviewResponse>> getBatch(@PathVariable("batchId") String batchId) {
        return ResponseEntity.ok(ApiResponse.ok(importService.getBatchPreview(batchId)));
    }
}

