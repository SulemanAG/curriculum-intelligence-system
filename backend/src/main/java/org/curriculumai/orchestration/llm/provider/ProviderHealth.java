package org.curriculumai.orchestration.llm.provider;

import java.time.Instant;

public class ProviderHealth {

    public enum CircuitState {
        CLOSED,     // Normal operations
        OPEN,       // Tripped due to failures / rate limits; requests blocked during cooldown
        HALF_OPEN   // Cooldown expired; testing recovery with trial request
    }

    private final String providerName;
    private final String modelName;
    private boolean configured;
    private Instant lastSuccessTime;
    private Instant lastFailureTime;
    private int lastStatusCode;
    private LlmErrorCategory lastErrorCategory = LlmErrorCategory.UNKNOWN;
    private String lastErrorMessage;
    private int consecutiveFailures = 0;
    private CircuitState circuitState = CircuitState.CLOSED;
    private Instant cooldownUntil;
    private long totalRequests = 0;
    private long totalSuccesses = 0;
    private long totalFailures = 0;

    public ProviderHealth(String providerName, String modelName, boolean configured) {
        this.providerName = providerName;
        this.modelName = modelName;
        this.configured = configured;
    }

    public synchronized void recordSuccess(int statusCode) {
        this.totalRequests++;
        this.totalSuccesses++;
        this.consecutiveFailures = 0;
        this.lastSuccessTime = Instant.now();
        this.lastStatusCode = statusCode;
        this.lastErrorCategory = null;
        this.lastErrorMessage = null;
        this.circuitState = CircuitState.CLOSED;
        this.cooldownUntil = null;
    }

    public synchronized void recordFailure(LlmErrorCategory category, int statusCode, String message, long cooldownSeconds) {
        this.totalRequests++;
        this.totalFailures++;
        this.consecutiveFailures++;
        this.lastFailureTime = Instant.now();
        this.lastStatusCode = statusCode;
        this.lastErrorCategory = category;
        this.lastErrorMessage = message;

        // Trip circuit on rate limits, auth errors, or repeated failures (>= 2)
        if (category == LlmErrorCategory.RATE_LIMIT_429 || category == LlmErrorCategory.AUTH_ERROR_401 ||
                category == LlmErrorCategory.AUTH_ERROR_403 || consecutiveFailures >= 2) {
            this.circuitState = CircuitState.OPEN;
            long actualCooldown = cooldownSeconds > 0 ? cooldownSeconds : 30L;
            this.cooldownUntil = Instant.now().plusSeconds(actualCooldown);
        }
    }

    public synchronized boolean isAvailableForExecution() {
        if (!configured) return false;
        if (circuitState == CircuitState.CLOSED) return true;

        if (circuitState == CircuitState.OPEN) {
            if (cooldownUntil != null && Instant.now().isAfter(cooldownUntil)) {
                // Cooldown elapsed; transition to HALF_OPEN to attempt recovery
                this.circuitState = CircuitState.HALF_OPEN;
                return true;
            }
            return false;
        }

        return circuitState == CircuitState.HALF_OPEN;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getModelName() {
        return modelName;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public Instant getLastSuccessTime() {
        return lastSuccessTime;
    }

    public Instant getLastFailureTime() {
        return lastFailureTime;
    }

    public int getLastStatusCode() {
        return lastStatusCode;
    }

    public LlmErrorCategory getLastErrorCategory() {
        return lastErrorCategory;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public CircuitState getCircuitState() {
        return circuitState;
    }

    public Instant getCooldownUntil() {
        return cooldownUntil;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getTotalSuccesses() {
        return totalSuccesses;
    }

    public long getTotalFailures() {
        return totalFailures;
    }
}

