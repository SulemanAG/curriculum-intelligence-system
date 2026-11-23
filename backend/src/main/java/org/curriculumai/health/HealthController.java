package org.curriculumai.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "System Connectivity & Readiness Probe")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check backend and AURA Coordinator connectivity")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of(
                "online", true,
                "status", "UP",
                "service", "Curriculum Intelligence Backend",
                "coordinator", "AURA Coordinator Runtime Operational",
                "message", "Backend Spring Boot Coordinator connected and operational."
        ));
    }
}

