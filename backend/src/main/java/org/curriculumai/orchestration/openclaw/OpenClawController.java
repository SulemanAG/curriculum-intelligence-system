package org.curriculumai.orchestration.openclaw;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.curriculumai.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Endpoint for Inspecting OpenCLA / OpenClaw Integration Status
 */
@RestController
@RequestMapping("/api/curriculum/openclaw")
@Tag(name = "OpenCLA / OpenClaw Integration", description = "Endpoints for OpenClaw agent gateway status and boundary inspection")
public class OpenClawController {

    private final OpenClawGatewayService gatewayService;

    public OpenClawController(OpenClawGatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @GetMapping("/status")
    @Operation(summary = "Get honest OpenCLA / OpenClaw external gateway integration status")
    public ResponseEntity<ApiResponse<OpenClawGatewayService.OpenClawStatus>> getStatus() {
        var status = gatewayService.getIntegrationStatus();
        return ResponseEntity.ok(ApiResponse.ok(status, "OpenCLA / OpenClaw integration status retrieved"));
    }
}
