package org.curriculumai.orchestration.openclaw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * OpenClaw / OpenCLA External Agent Gateway Service
 * Provides governed integration boundaries with external OpenClaw runtimes.
 * In standard local environments, operates honestly in STANDBY mode without simulating fake connections.
 */
@Service
public class OpenClawGatewayService {

    private static final Logger log = LoggerFactory.getLogger(OpenClawGatewayService.class);

    private final OpenClawProperties properties;
    private final HttpClient httpClient;

    public record OpenClawStatus(
            boolean enabled,
            String state,
            String gatewayUrl,
            List<String> capabilities,
            String connectionMessage,
            Instant lastChecked
    ) {}

    public OpenClawGatewayService(OpenClawProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /**
     * Returns the honest runtime connectivity status of OpenClaw.
     */
    public OpenClawStatus getIntegrationStatus() {
        if (!properties.isEnabled()) {
            return new OpenClawStatus(
                    false,
                    "STANDBY",
                    properties.getGatewayUrl(),
                    List.of("TASK_DISPATCH", "EXTERNAL_AUDIT", "TOOL_BRIDGING"),
                    "OpenClaw external gateway is in STANDBY. AURA is operating in autonomous native multi-agent mode.",
                    Instant.now()
            );
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getGatewayUrl() + "/health"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return new OpenClawStatus(
                        true,
                        "CONNECTED",
                        properties.getGatewayUrl(),
                        List.of("TASK_DISPATCH", "EXTERNAL_AUDIT", "TOOL_BRIDGING"),
                        "OpenClaw external agent runtime is connected and responding at " + properties.getGatewayUrl(),
                        Instant.now()
                );
            } else {
                return new OpenClawStatus(
                        true,
                        "UNREACHABLE",
                        properties.getGatewayUrl(),
                        List.of("TASK_DISPATCH", "EXTERNAL_AUDIT", "TOOL_BRIDGING"),
                        "OpenClaw gateway responded with HTTP status " + response.statusCode(),
                        Instant.now()
                );
            }
        } catch (Exception e) {
            return new OpenClawStatus(
                    true,
                    "OFFLINE",
                    properties.getGatewayUrl(),
                    List.of("TASK_DISPATCH", "EXTERNAL_AUDIT", "TOOL_BRIDGING"),
                    "OpenClaw external daemon is not running on configured port. Gracefully defaulting to native AURA orchestration.",
                    Instant.now()
            );
        }
    }
}
