package org.curriculumai.orchestration.llm.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderHealthTracker {

    private final Map<String, ProviderHealth> healthMap = new ConcurrentHashMap<>();

    public void registerProvider(String providerName, String modelName, boolean configured) {
        healthMap.put(providerName.toLowerCase(), new ProviderHealth(providerName.toLowerCase(), modelName, configured));
    }

    public ProviderHealth getHealth(String providerName) {
        if (providerName == null) return null;
        return healthMap.get(providerName.toLowerCase());
    }

    public boolean isAvailable(String providerName) {
        ProviderHealth health = getHealth(providerName);
        return health != null && health.isAvailableForExecution();
    }

    public void recordSuccess(String providerName, int statusCode) {
        ProviderHealth health = getHealth(providerName);
        if (health != null) {
            health.recordSuccess(statusCode);
        }
    }

    public void recordFailure(String providerName, LlmErrorCategory category, int statusCode, String message, long cooldownSeconds) {
        ProviderHealth health = getHealth(providerName);
        if (health != null) {
            health.recordFailure(category, statusCode, message, cooldownSeconds);
        }
    }

    public Map<String, ProviderHealth> getAllHealth() {
        return Map.copyOf(healthMap);
    }
}

