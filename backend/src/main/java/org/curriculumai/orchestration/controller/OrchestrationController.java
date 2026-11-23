package org.curriculumai.orchestration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.curriculumai.common.ApiResponse;
import org.curriculumai.orchestration.agent.AgentRegistry;
import org.curriculumai.orchestration.dto.*;
import org.curriculumai.orchestration.engine.AuraOrchestratorService;
import org.curriculumai.security.UserIdentityResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orchestration")
@Tag(name = "AURA Multi-Agent Orchestration", description = "Central workflow coordination, DAG execution, checkpointing, and agent governance")
public class OrchestrationController {

    private final AuraOrchestratorService orchestratorService;
    private final AgentRegistry agentRegistry;
    private final UserIdentityResolver identityResolver;

    public OrchestrationController(
            AuraOrchestratorService orchestratorService,
            AgentRegistry agentRegistry,
            UserIdentityResolver identityResolver
    ) {
        this.orchestratorService = orchestratorService;
        this.agentRegistry = agentRegistry;
        this.identityResolver = identityResolver;
    }

    @PostMapping("/runs")
    @Operation(summary = "Submit a workflow request, synthesize DAG plan, and begin execution")
    public ResponseEntity<ApiResponse<RunOverviewDto>> createRun(
            @Valid @RequestBody CreateRunRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String devHeader
    ) {
        String userId = identityResolver.resolveUserId(devHeader);
        RunOverviewDto run = orchestratorService.createAndStartRun(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(run, "Workflow run created and execution initiated successfully."));
    }

    @GetMapping("/runs")
    @Operation(summary = "List orchestration runs for the authenticated user")
    public ResponseEntity<ApiResponse<Page<RunOverviewDto>>> listRuns(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-User-Id", required = false) String devHeader
    ) {
        String userId = identityResolver.resolveUserId(devHeader);
        Page<RunOverviewDto> runs = orchestratorService.listRuns(userId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(runs, "Orchestration runs retrieved successfully."));
    }

    @GetMapping("/runs/{id}")
    @Operation(summary = "Retrieve run status, metrics, and progress overview")
    public ResponseEntity<ApiResponse<RunOverviewDto>> getRun(
            @PathVariable("id") String id,
            @RequestHeader(value = "X-User-Id", required = false) String devHeader
    ) {
        String userId = identityResolver.resolveUserId(devHeader);
        RunOverviewDto run = orchestratorService.getRunOverview(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(run, "Run details retrieved successfully."));
    }

    @GetMapping("/runs/{id}/tasks")
    @Operation(summary = "Retrieve workflow plan and task dependency graph for a run")
    public ResponseEntity<ApiResponse<WorkflowPlanDto>> getWorkflowTasks(
            @PathVariable("id") String id,
            @RequestHeader(value = "X-User-Id", required = false) String devHeader
    ) {
        String userId = identityResolver.resolveUserId(devHeader);
        WorkflowPlanDto plan = orchestratorService.getWorkflowPlan(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(plan, "Workflow plan retrieved successfully."));
    }

    @GetMapping("/runs/{id}/plan")
    @Operation(summary = "Alias to retrieve workflow plan and task dependency graph")
    public ResponseEntity<ApiResponse<WorkflowPlanDto>> getWorkflowPlan(
            @PathVariable("id") String id,
            @RequestHeader(value = "X-User-Id", required = false) String devHeader
    ) {
        return getWorkflowTasks(id, devHeader);
    }

    @GetMapping("/runs/{id}/events")
    @Operation(summary = "Retrieve execution audit events history for a run")
    public ResponseEntity<ApiResponse<List<WorkflowEventDto>>> getWorkflowEvents(
            @PathVariable("id") String id,
            @RequestHeader(value = "X-User-Id", required = false) String devHeader
    ) {
        String userId = identityResolver.resolveUserId(devHeader);
        List<WorkflowEventDto> events = orchestratorService.listEvents(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(events, "Workflow events retrieved successfully."));
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Retrieve detailed information for a specific task")
    public ResponseEntity<ApiResponse<WorkflowTaskDto>> getTask(
            @PathVariable("taskId") String taskId,
            @RequestHeader(value = "X-User-Id", required = false) String devHeader
    ) {
        String userId = identityResolver.resolveUserId(devHeader);
        WorkflowTaskDto task = orchestratorService.getTaskDetails(taskId, userId);
        return ResponseEntity.ok(ApiResponse.ok(task, "Task details retrieved successfully."));
    }

    @PostMapping("/runs/{id}/cancel")
    @Operation(summary = "Cancel an active or pending workflow run")
    public ResponseEntity<ApiResponse<RunOverviewDto>> cancelRun(
            @PathVariable("id") String id,
            @RequestHeader(value = "X-User-Id", required = false) String devHeader
    ) {
        String userId = identityResolver.resolveUserId(devHeader);
        RunOverviewDto run = orchestratorService.cancelRun(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(run, "Workflow run successfully cancelled."));
    }

    @PostMapping("/runs/{id}/resume")
    @Operation(summary = "Resume an interrupted, paused, or failed workflow run")
    public ResponseEntity<ApiResponse<RunOverviewDto>> resumeRun(
            @PathVariable("id") String id,
            @RequestHeader(value = "X-User-Id", required = false) String devHeader
    ) {
        String userId = identityResolver.resolveUserId(devHeader);
        RunOverviewDto run = orchestratorService.resumeRun(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(run, "Workflow run resumed successfully."));
    }

    @GetMapping("/agents")
    @Operation(summary = "List registered specialist agents and their implementation status")
    public ResponseEntity<ApiResponse<List<AgentDefinitionDto>>> listAgents() {
        List<AgentDefinitionDto> agents = agentRegistry.getAllAgents().stream()
                .map(AgentDefinitionDto::fromRecord)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(agents, "Registered specialist agents retrieved successfully."));
    }
}

