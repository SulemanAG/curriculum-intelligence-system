package org.curriculumai.orchestration.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.orchestration.llm.provider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LlmExecutionService {

    private static final Logger log = LoggerFactory.getLogger(LlmExecutionService.class);

    private final LlmProperties properties;
    private final ObjectMapper objectMapper;
    private final ProviderHealthTracker healthTracker;
    private final Map<String, LlmProvider> providers = new LinkedHashMap<>();

    // Thread-local or stateful last-execution details for telemetry
    public record ExecutionDetails(
            String providerName,
            String modelName,
            boolean failoverOccurred,
            String failoverFrom,
            int statusCode,
            long durationMs
    ) {}

    private volatile ExecutionDetails lastExecutionDetails = new ExecutionDetails("unknown", "unknown", false, null, 200, 0);

    @Autowired
    public LlmExecutionService(LlmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.healthTracker = new ProviderHealthTracker();

        initializeProviders();
    }

    public LlmExecutionService(LlmProperties properties) {
        this(properties, new ObjectMapper());
    }

    /**
     * Constructor allowing injected mock/custom providers (primarily for unit testing failover).
     */
    public LlmExecutionService(LlmProperties properties, ObjectMapper objectMapper, Map<String, LlmProvider> customProviders) {
        this.properties = properties;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.healthTracker = new ProviderHealthTracker();

        if (customProviders != null) {
            this.providers.putAll(customProviders);
            customProviders.forEach((name, p) ->
                    healthTracker.registerProvider(name, p.getModelName(), p.isConfigured()));
        } else {
            initializeProviders();
        }
    }

    private void initializeProviders() {
        // 1. Groq Provider
        String groqKey = properties.getEffectiveGroqApiKey();
        String groqModel = properties.getEffectiveGroqModel();
        String groqBaseUrl = properties.getEffectiveGroqBaseUrl();
        GroqLlmProvider groqProvider = new GroqLlmProvider(groqKey, groqModel, groqBaseUrl, objectMapper);
        providers.put("groq", groqProvider);
        healthTracker.registerProvider("groq", groqModel, groqProvider.isConfigured());

        // 2. Gemini Provider
        String geminiKey = properties.getEffectiveGeminiApiKey();
        String geminiModel = properties.getEffectiveGeminiModel();
        GeminiLlmProvider geminiProvider = new GeminiLlmProvider(geminiKey, geminiModel, objectMapper);
        providers.put("gemini", geminiProvider);
        healthTracker.registerProvider("gemini", geminiModel, geminiProvider.isConfigured());

        log.info("LlmExecutionService initialized. Primary: '{}', FailoverEnabled: {}, GroqConfigured: {}, GeminiConfigured: {}",
                properties.getPrimaryProvider(), properties.isFailoverEnabled(), groqProvider.isConfigured(), geminiProvider.isConfigured());
    }

    public boolean isAvailable() {
        if (!properties.isEnabled()) {
            return false;
        }
        return providers.values().stream().anyMatch(LlmProvider::isConfigured);
    }

    public String getProviderName() {
        if (lastExecutionDetails != null && !"unknown".equals(lastExecutionDetails.providerName())) {
            return lastExecutionDetails.providerName();
        }
        return properties.getPrimaryProvider();
    }

    public String getModelName() {
        if (lastExecutionDetails != null && !"unknown".equals(lastExecutionDetails.modelName())) {
            return lastExecutionDetails.modelName();
        }
        LlmProvider primary = providers.get(properties.getPrimaryProvider());
        return primary != null ? primary.getModelName() : "unknown";
    }

    public String getLastExecutedProvider() {
        return lastExecutionDetails.providerName();
    }

    public String getLastExecutedModel() {
        return lastExecutionDetails.modelName();
    }

    public boolean isLastExecutionFailover() {
        return lastExecutionDetails.failoverOccurred();
    }

    public String getLastExecutionFailoverFrom() {
        return lastExecutionDetails.failoverFrom();
    }

    public ExecutionDetails getLastExecutionDetails() {
        return lastExecutionDetails;
    }

    public ProviderHealthTracker getHealthTracker() {
        return healthTracker;
    }

    public Map<String, LlmProvider> getProviders() {
        return Collections.unmodifiableMap(providers);
    }

    public String executePrompt(String systemPrompt, String userPrompt) {
        return executePrompt(systemPrompt, userPrompt, 3500, properties.getTemperature());
    }

    public String executePrompt(String systemPrompt, String userPrompt, int maxTokens, double temperature) {
        LlmRequest request = LlmRequest.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .timeoutSeconds(properties.getTimeoutSeconds())
                .build();

        LlmResponse response = executeWithFailover(request);
        return response.content();
    }

    public LlmResponse executeWithFailover(LlmRequest request) {
        if (!isAvailable()) {
            throw new IllegalStateException("LLM execution service is not available. Please configure at least one provider (Groq or Gemini) with valid credentials.");
        }

        String primaryName = properties.getPrimaryProvider();
        String secondaryName = "groq".equalsIgnoreCase(primaryName) ? "gemini" : "groq";

        LlmProvider primary = providers.get(primaryName);
        LlmProvider secondary = providers.get(secondaryName);

        // Case 1: Primary provider is configured and available (or secondary is unconfigured)
        if (primary != null && primary.isConfigured() && healthTracker.isAvailable(primaryName)) {
            try {
                log.info("Attempting LLM request with primary provider '{}' (model={})", primaryName, primary.getModelName());
                LlmResponse resp = executeWithRetries(primary, request);
                healthTracker.recordSuccess(primaryName, resp.statusCode());

                this.lastExecutionDetails = new ExecutionDetails(
                        primaryName, primary.getModelName(), false, null, resp.statusCode(), resp.durationMs()
                );
                return resp;

            } catch (LlmException ex) {
                healthTracker.recordFailure(primaryName, ex.getCategory(), ex.getStatusCode(), ex.getMessage(), 30L);

                if (!properties.isFailoverEnabled() || secondary == null || !secondary.isConfigured()) {
                    log.error("Primary provider '{}' failed and failover is unavailable or disabled: {}", primaryName, ex.getMessage());
                    throw ex;
                }

                if (!ex.isFailoverEligible()) {
                    log.error("Primary provider '{}' failed with non-failover-eligible error (category={}): {}", primaryName, ex.getCategory(), ex.getMessage());
                    throw ex;
                }

                // TRIGGER FAILOVER TO SECONDARY
                log.warn("FAILOVER TRIGGERED: Primary provider '{}' failed with [{} - HTTP {}]. Attempting secondary provider '{}' (model={}).",
                        primaryName, ex.getCategory(), ex.getStatusCode(), secondaryName, secondary.getModelName());

                return executeSecondaryFallback(secondary, request, primaryName, ex);
            }
        }

        // Case 2: Primary is unavailable, circuit-broken, or unconfigured -> Direct route to secondary
        if (secondary != null && secondary.isConfigured() && healthTracker.isAvailable(secondaryName)) {
            log.info("Primary provider '{}' is unavailable or circuit-open. Directly routing to secondary provider '{}' (model={})",
                    primaryName, secondaryName, secondary.getModelName());
            try {
                LlmResponse resp = executeWithRetries(secondary, request);
                healthTracker.recordSuccess(secondaryName, resp.statusCode());

                this.lastExecutionDetails = new ExecutionDetails(
                        secondaryName, secondary.getModelName(), true, primaryName, resp.statusCode(), resp.durationMs()
                );
                return LlmResponse.ofFailover(resp.content(), secondaryName, secondary.getModelName(), resp.statusCode(), resp.durationMs(), primaryName);

            } catch (LlmException ex) {
                healthTracker.recordFailure(secondaryName, ex.getCategory(), ex.getStatusCode(), ex.getMessage(), 30L);
                throw new RuntimeException("Secondary LLM provider '" + secondaryName + "' failed while primary '" + primaryName + "' was unavailable: " + ex.getMessage(), ex);
            }
        }

        // Case 3: Both providers are unavailable or circuit-broken
        throw new IllegalStateException(String.format(
                "All configured LLM providers are currently unavailable or circuit-broken. Primary: '%s' (available=%b), Secondary: '%s' (available=%b)",
                primaryName, healthTracker.isAvailable(primaryName), secondaryName, healthTracker.isAvailable(secondaryName)
        ));
    }

    private LlmResponse executeSecondaryFallback(LlmProvider secondary, LlmRequest request, String primaryName, LlmException primaryEx) {
        long fallbackStart = System.currentTimeMillis();
        try {
            LlmResponse resp = executeWithRetries(secondary, request);
            healthTracker.recordSuccess(secondary.getName(), resp.statusCode());
            long totalDuration = System.currentTimeMillis() - fallbackStart;

            this.lastExecutionDetails = new ExecutionDetails(
                    secondary.getName(), secondary.getModelName(), true, primaryName, resp.statusCode(), totalDuration
            );

            log.info("FAILOVER SUCCEEDED: Request successfully satisfied by fallback provider '{}' in {}ms",
                    secondary.getName(), totalDuration);

            return LlmResponse.ofFailover(resp.content(), secondary.getName(), secondary.getModelName(), resp.statusCode(), totalDuration, primaryName);

        } catch (LlmException secEx) {
            healthTracker.recordFailure(secondary.getName(), secEx.getCategory(), secEx.getStatusCode(), secEx.getMessage(), 30L);
            log.error("FAILOVER EXHAUSTED: Both providers failed. Primary ('{}'): {}. Secondary ('{}'): {}",
                    primaryName, primaryEx.getMessage(), secondary.getName(), secEx.getMessage());

            throw new LlmException(
                    secondary.getName(),
                    secEx.getCategory() != null ? secEx.getCategory() : LlmErrorCategory.SERVER_ERROR_5XX,
                    secEx.getStatusCode(),
                    String.format("All configured LLM providers failed. Primary [%s]: %s | Secondary [%s]: %s",
                            primaryName, primaryEx.getMessage(), secondary.getName(), secEx.getMessage()),
                    secEx
            );
        }
    }

    private LlmResponse executeWithRetries(LlmProvider provider, LlmRequest request) throws LlmException {
        int maxAttempts = 2; // Bounded internal retries per provider before failover
        LlmException lastEx = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return provider.execute(request);
            } catch (LlmException ex) {
                lastEx = ex;

                // 401/403 or non-failover errors should not retry on the same provider
                if (ex.getCategory() == LlmErrorCategory.AUTH_ERROR_401 ||
                        ex.getCategory() == LlmErrorCategory.AUTH_ERROR_403 ||
                        ex.getCategory() == LlmErrorCategory.INVALID_REQUEST_400) {
                    throw ex;
                }

                // On rate-limit 429, if this is attempt 1, brief pause or failover immediately
                if (ex.getCategory() == LlmErrorCategory.RATE_LIMIT_429) {
                    if (properties.isFailoverEnabled()) {
                        // Failover immediately to avoid stalling when alternative provider is available
                        throw ex;
                    }
                    if (attempt < maxAttempts) {
                        try {
                            Thread.sleep(2000L);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw ex;
                        }
                    }
                } else if (attempt < maxAttempts && ex.isFailoverEligible()) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw ex;
                    }
                }
            }
        }

        if (lastEx != null) {
            throw lastEx;
        }
        throw new LlmException(provider.getName(), LlmErrorCategory.UNKNOWN, 500, "Unknown provider failure");
    }
}
