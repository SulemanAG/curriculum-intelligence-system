package org.curriculumai.coordinator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.curriculumai.common.ApiResponse;
import org.curriculumai.coordinator.dto.*;
import org.curriculumai.orchestration.llm.LlmExecutionService;
import org.curriculumai.security.UserIdentityResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/curriculum")
@Tag(name = "Curriculum Intelligence", description = "Multi-Agent Coordinator, Swarm Execution & Proposal Endpoints")
public class CurriculumController {

    private final AuraCoordinatorService coordinatorService;
    private final UserIdentityResolver userIdentityResolver;
    private final KleTechDocumentService documentService;
    private final LlmExecutionService llmExecutionService;

    public CurriculumController(AuraCoordinatorService coordinatorService,
                                UserIdentityResolver userIdentityResolver,
                                KleTechDocumentService documentService,
                                LlmExecutionService llmExecutionService) {
        this.coordinatorService = coordinatorService;
        this.userIdentityResolver = userIdentityResolver;
        this.documentService = documentService;
        this.llmExecutionService = llmExecutionService;
    }

    private String resolveUserId(String userId) {
        return userIdentityResolver.resolveUserId(userId);
    }

    @PostMapping("/requests")
    @Operation(summary = "Submit a course design request to the AURA Coordinator")
    public ResponseEntity<ApiResponse<RunResponse>> submitCourseRequest(
            @RequestHeader(value = "X-User-Id", defaultValue = "usr-default-faculty") String userId,
            @Valid @RequestBody CourseRequestInput input
    ) {
        RunResponse runResponse = coordinatorService.initiateRun(resolveUserId(userId), input);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(runResponse, "Course research request accepted; Coordinator initiating specialist agents."));
    }

    @GetMapping("/runs/{runId}")
    @Operation(summary = "Poll the execution status of an active analysis run")
    public ResponseEntity<ApiResponse<RunResponse>> getRunStatus(
            @PathVariable("runId") String runId
    ) {
        RunResponse response = coordinatorService.getRunStatus(runId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/runs/{runId}/alternatives")
    @Operation(summary = "Retrieve exactly four generated course-plan proposals")
    public ResponseEntity<ApiResponse<List<AlternativeResponse>>> getAlternatives(
            @PathVariable("runId") String runId
    ) {
        List<AlternativeResponse> alternatives = coordinatorService.getCourseAlternatives(runId);
        return ResponseEntity.ok(ApiResponse.ok(alternatives));
    }

    @PostMapping("/feedback")
    @Operation(summary = "Commit faculty proposal selection and rationale to the preference dataset")
    public ResponseEntity<ApiResponse<Map<String, String>>> submitFeedback(
            @RequestHeader(value = "X-User-Id", defaultValue = "usr-default-faculty") String userId,
            @Valid @RequestBody FeedbackInput feedback
    ) {
        Map<String, String> result = coordinatorService.submitSelectionFeedback(resolveUserId(userId), feedback);
        return ResponseEntity.ok(ApiResponse.ok(result, "Faculty preference successfully committed to audit trail."));
    }

    @GetMapping("/plans/{planId}")
    @Operation(summary = "Retrieve the finalized institutional course plan")
    public ResponseEntity<ApiResponse<FinalPlanResponse>> getFinalPlan(
            @PathVariable("planId") String planId
    ) {
        FinalPlanResponse plan = coordinatorService.getFinalCoursePlan(planId);
        return ResponseEntity.ok(ApiResponse.ok(plan));
    }

    @GetMapping("/plans/{planId}/pdf")
    @Operation(summary = "Download or verify the compiled course plan PDF document")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable("planId") String planId
    ) {
        FinalPlanResponse plan = coordinatorService.getFinalCoursePlan(planId);
        byte[] pdfBytes = documentService.generatePdf(plan);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + plan.courseCode() + "_Course_Plan.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/plans/{planId}/docx")
    @Operation(summary = "Download or verify the compiled course plan DOCX document")
    public ResponseEntity<byte[]> downloadDocx(
            @PathVariable("planId") String planId
    ) {
        FinalPlanResponse plan = coordinatorService.getFinalCoursePlan(planId);
        byte[] docxBytes = documentService.generateDocx(plan);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + plan.courseCode() + "_Course_Plan.docx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(docxBytes);
    }

    @GetMapping("/llm/status")
    @Operation(summary = "Retrieve current LLM provider health, active routing, and failover status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLlmStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("primaryProvider", llmExecutionService.getProviderName());
        status.put("activeProvider", llmExecutionService.getLastExecutedProvider());
        status.put("activeModel", llmExecutionService.getLastExecutedModel());
        status.put("available", llmExecutionService.isAvailable());
        status.put("lastExecution", llmExecutionService.getLastExecutionDetails());

        Map<String, Object> providersMap = new LinkedHashMap<>();
        llmExecutionService.getHealthTracker().getAllHealth().forEach((name, health) -> {
            Map<String, Object> h = new LinkedHashMap<>();
            h.put("providerName", health.getProviderName());
            h.put("modelName", health.getModelName());
            h.put("configured", health.isConfigured());
            h.put("circuitState", health.getCircuitState().name());
            h.put("consecutiveFailures", health.getConsecutiveFailures());
            h.put("lastSuccess", health.getLastSuccessTime() != null ? health.getLastSuccessTime().toString() : null);
            h.put("lastFailure", health.getLastFailureTime() != null ? health.getLastFailureTime().toString() : null);
            h.put("lastHttpStatus", health.getLastStatusCode());
            h.put("lastErrorCategory", health.getLastErrorCategory() != null ? health.getLastErrorCategory().name() : null);
            h.put("totalRequests", health.getTotalRequests());
            h.put("totalSuccesses", health.getTotalSuccesses());
            h.put("totalFailures", health.getTotalFailures());
            providersMap.put(name, h);
        });
        status.put("providers", providersMap);

        return ResponseEntity.ok(ApiResponse.ok(status, "LLM provider runtime status retrieved"));
    }
}

