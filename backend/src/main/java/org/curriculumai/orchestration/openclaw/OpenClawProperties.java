package org.curriculumai.orchestration.openclaw;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "openclaw")
public class OpenClawProperties {

    /**
     * Whether OpenClaw external agent gateway integration is active.
     * Default: false (AURA operates in native autonomous mode).
     */
    private boolean enabled = false;

    /**
     * Target OpenClaw / OpenCLA gateway endpoint.
     */
    private String gatewayUrl = "http://localhost:9000/api/v1/agents";

    /**
     * Secret API authentication key for OpenClaw gateway (if required).
     */
    private String apiKey = "";

    /**
     * Request timeout in seconds.
     */
    private int timeoutSeconds = 30;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
