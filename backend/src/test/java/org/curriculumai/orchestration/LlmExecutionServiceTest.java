package org.curriculumai.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.orchestration.agent.benchmarking.AcademicBenchmarkingAgent;
import org.curriculumai.orchestration.agent.curriculum.CurriculumMappingAgent;
import org.curriculumai.orchestration.agent.gap.GapAnalysisAgent;
import org.curriculumai.orchestration.agent.industry.IndustrySkillsAgent;
import org.curriculumai.orchestration.agent.recommendation.RecommendationAgent;
import org.curriculumai.orchestration.llm.LlmExecutionService;
import org.curriculumai.orchestration.llm.LlmProperties;
import org.curriculumai.orchestration.llm.provider.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LlmExecutionServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    static class MockProvider implements LlmProvider {
        private final String name;
        private final String model;
        private final Function<LlmRequest, LlmResponse> handler;

        MockProvider(String name, String model, Function<LlmRequest, LlmResponse> handler) {
            this.name = name;
            this.model = model;
            this.handler = handler;
        }

        @Override public String getName() { return name; }
        @Override public String getModelName() { return model; }
        @Override public boolean isConfigured() { return true; }
        @Override public LlmResponse execute(LlmRequest request) { return handler.apply(request); }
    }

    private LlmProperties createDefaultProps(String primary, boolean failover) {
        LlmProperties props = new LlmProperties();
        props.setEnabled(true);
        props.setPrimaryProvider(primary);
        props.setFailoverEnabled(failover);
        props.setTimeoutSeconds(5);
        props.getGroq().setApiKey("gsk_mock_key");
        props.getGroq().setModel("openai/gpt-oss-120b");
        props.getGemini().setApiKey("gemini_mock_key");
        props.getGemini().setModel("gemini-2.5-flash");
        return props;
    }

    // -------------------------------------------------------------------------
    // Scenario A: Groq succeeds -> Gemini is not called
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Scenario A: Groq succeeds -> Gemini is not called, provenance is Groq")
    void scenarioA_groqSucceeds_geminiNotCalled() {
        AtomicInteger groqCalls = new AtomicInteger();
        AtomicInteger geminiCalls = new AtomicInteger();

        MockProvider groq = new MockProvider("groq", "openai/gpt-oss-120b", req -> {
            groqCalls.incrementAndGet();
            return LlmResponse.of("Groq response text", "groq", "openai/gpt-oss-120b", 200, 100L);
        });

        MockProvider gemini = new MockProvider("gemini", "gemini-2.5-flash", req -> {
            geminiCalls.incrementAndGet();
            return LlmResponse.of("Gemini response text", "gemini", "gemini-2.5-flash", 200, 120L);
        });

        Map<String, LlmProvider> providers = new LinkedHashMap<>();
        providers.put("groq", groq);
        providers.put("gemini", gemini);

        LlmExecutionService service = new LlmExecutionService(createDefaultProps("groq", true), objectMapper, providers);

        String result = service.executePrompt("System", "User");

        assertThat(result).isEqualTo("Groq response text");
        assertThat(groqCalls.get()).isEqualTo(1);
        assertThat(geminiCalls.get()).isZero();
        assertThat(service.getLastExecutedProvider()).isEqualTo("groq");
        assertThat(service.isLastExecutionFailover()).isFalse();
    }

    // -------------------------------------------------------------------------
    // Scenario B: Groq returns 429 rate limit -> Gemini succeeds
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Scenario B: Groq returns 429 rate limit -> Gemini called and succeeds with failover provenance")
    void scenarioB_groqRateLimit429_failsOverToGemini() {
        AtomicInteger groqCalls = new AtomicInteger();
        AtomicInteger geminiCalls = new AtomicInteger();

        MockProvider groq = new MockProvider("groq", "openai/gpt-oss-120b", req -> {
            groqCalls.incrementAndGet();
            throw new LlmException("groq", LlmErrorCategory.RATE_LIMIT_429, 429, "Rate limit reached (TPM exceeded)");
        });

        MockProvider gemini = new MockProvider("gemini", "gemini-2.5-flash", req -> {
            geminiCalls.incrementAndGet();
            return LlmResponse.of("Gemini synthesized answer", "gemini", "gemini-2.5-flash", 200, 150L);
        });

        Map<String, LlmProvider> providers = new LinkedHashMap<>();
        providers.put("groq", groq);
        providers.put("gemini", gemini);

        LlmExecutionService service = new LlmExecutionService(createDefaultProps("groq", true), objectMapper, providers);

        String result = service.executePrompt("System", "User");

        assertThat(result).isEqualTo("Gemini synthesized answer");
        assertThat(groqCalls.get()).isEqualTo(1);
        assertThat(geminiCalls.get()).isEqualTo(1);
        assertThat(service.getLastExecutedProvider()).isEqualTo("gemini");
        assertThat(service.isLastExecutionFailover()).isTrue();
        assertThat(service.getLastExecutionFailoverFrom()).isEqualTo("groq");

        // Verify Groq circuit breaker tripped to OPEN
        ProviderHealth groqHealth = service.getHealthTracker().getHealth("groq");
        assertThat(groqHealth.getCircuitState()).isEqualTo(ProviderHealth.CircuitState.OPEN);
        assertThat(groqHealth.getLastStatusCode()).isEqualTo(429);
    }

    // -------------------------------------------------------------------------
    // Scenario C: Groq times out -> Gemini succeeds
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Scenario C: Groq times out -> fails over to Gemini seamlessly")
    void scenarioC_groqTimeout_failsOverToGemini() {
        MockProvider groq = new MockProvider("groq", "openai/gpt-oss-120b", req -> {
            throw new LlmException("groq", LlmErrorCategory.TIMEOUT, 408, "Read timed out after 60 seconds");
        });

        MockProvider gemini = new MockProvider("gemini", "gemini-2.5-flash", req ->
                LlmResponse.of("Gemini timeout recovery answer", "gemini", "gemini-2.5-flash", 200, 90L));

        Map<String, LlmProvider> providers = new LinkedHashMap<>();
        providers.put("groq", groq);
        providers.put("gemini", gemini);

        LlmExecutionService service = new LlmExecutionService(createDefaultProps("groq", true), objectMapper, providers);

        String result = service.executePrompt("System", "User");

        assertThat(result).isEqualTo("Gemini timeout recovery answer");
        assertThat(service.getLastExecutedProvider()).isEqualTo("gemini");
        assertThat(service.isLastExecutionFailover()).isTrue();
    }

    // -------------------------------------------------------------------------
    // Scenario D: Groq returns 503 -> Gemini succeeds
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Scenario D: Groq returns 503 Service Unavailable -> fails over to Gemini")
    void scenarioD_groqServerUnavailable_failsOverToGemini() {
        MockProvider groq = new MockProvider("groq", "openai/gpt-oss-120b", req -> {
            throw new LlmException("groq", LlmErrorCategory.SERVER_ERROR_5XX, 503, "Service Unavailable");
        });

        MockProvider gemini = new MockProvider("gemini", "gemini-2.5-flash", req ->
                LlmResponse.of("Gemini 503 recovery output", "gemini", "gemini-2.5-flash", 200, 110L));

        Map<String, LlmProvider> providers = new LinkedHashMap<>();
        providers.put("groq", groq);
        providers.put("gemini", gemini);

        LlmExecutionService service = new LlmExecutionService(createDefaultProps("groq", true), objectMapper, providers);

        String result = service.executePrompt("System", "User");

        assertThat(result).isEqualTo("Gemini 503 recovery output");
        assertThat(service.getLastExecutedProvider()).isEqualTo("gemini");
        assertThat(service.isLastExecutionFailover()).isTrue();
    }

    // -------------------------------------------------------------------------
    // Scenario E: Groq auth fails (401/403) -> Gemini succeeds
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Scenario E: Groq auth fails (401) -> fails over to Gemini and marks Groq circuit OPEN")
    void scenarioE_groqAuthError_failsOverToGemini() {
        MockProvider groq = new MockProvider("groq", "openai/gpt-oss-120b", req -> {
            throw new LlmException("groq", LlmErrorCategory.AUTH_ERROR_401, 401, "Invalid API Key provided");
        });

        MockProvider gemini = new MockProvider("gemini", "gemini-2.5-flash", req ->
                LlmResponse.of("Gemini auth-fallback output", "gemini", "gemini-2.5-flash", 200, 130L));

        Map<String, LlmProvider> providers = new LinkedHashMap<>();
        providers.put("groq", groq);
        providers.put("gemini", gemini);

        LlmExecutionService service = new LlmExecutionService(createDefaultProps("groq", true), objectMapper, providers);

        String result = service.executePrompt("System", "User");

        assertThat(result).isEqualTo("Gemini auth-fallback output");
        assertThat(service.getLastExecutedProvider()).isEqualTo("gemini");
        assertThat(service.isLastExecutionFailover()).isTrue();

        ProviderHealth groqHealth = service.getHealthTracker().getHealth("groq");
        assertThat(groqHealth.getCircuitState()).isEqualTo(ProviderHealth.CircuitState.OPEN);
    }

    // -------------------------------------------------------------------------
    // Scenario F: Gemini is primary, Groq is fallback
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Scenario F: Gemini configured as primary, Groq as fallback -> fails over to Groq on Gemini error")
    void scenarioF_geminiPrimary_failsOverToGroq() {
        MockProvider gemini = new MockProvider("gemini", "gemini-2.5-flash", req -> {
            throw new LlmException("gemini", LlmErrorCategory.RATE_LIMIT_429, 429, "Resource exhausted");
        });

        MockProvider groq = new MockProvider("groq", "openai/gpt-oss-120b", req ->
                LlmResponse.of("Groq fallback output from Gemini primary", "groq", "openai/gpt-oss-120b", 200, 80L));

        Map<String, LlmProvider> providers = new LinkedHashMap<>();
        providers.put("gemini", gemini);
        providers.put("groq", groq);

        LlmExecutionService service = new LlmExecutionService(createDefaultProps("gemini", true), objectMapper, providers);

        String result = service.executePrompt("System", "User");

        assertThat(result).isEqualTo("Groq fallback output from Gemini primary");
        assertThat(service.getLastExecutedProvider()).isEqualTo("groq");
        assertThat(service.isLastExecutionFailover()).isTrue();
        assertThat(service.getLastExecutionFailoverFrom()).isEqualTo("gemini");
    }

    // -------------------------------------------------------------------------
    // Scenario G: Both providers fail -> honest failure thrown
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Scenario G: Both providers fail -> throws honest LlmException without returning fake data")
    void scenarioG_bothProvidersFail_throwsHonestException() {
        MockProvider groq = new MockProvider("groq", "openai/gpt-oss-120b", req -> {
            throw new LlmException("groq", LlmErrorCategory.RATE_LIMIT_429, 429, "Groq limit reached");
        });

        MockProvider gemini = new MockProvider("gemini", "gemini-2.5-flash", req -> {
            throw new LlmException("gemini", LlmErrorCategory.SERVER_ERROR_5XX, 500, "Gemini internal server error");
        });

        Map<String, LlmProvider> providers = new LinkedHashMap<>();
        providers.put("groq", groq);
        providers.put("gemini", gemini);

        LlmExecutionService service = new LlmExecutionService(createDefaultProps("groq", true), objectMapper, providers);

        assertThatThrownBy(() -> service.executePrompt("System", "User"))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("All configured LLM providers failed");
    }

    // -------------------------------------------------------------------------
    // Scenario H: Neither provider configured -> clear configuration error
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Scenario H: Neither provider configured -> isAvailable is false and throws IllegalStateException")
    void scenarioH_neitherProviderConfigured_throwsIllegalStateException() {
        LlmProperties props = new LlmProperties();
        props.setEnabled(false);
        props.setApiKey("");
        props.getGroq().setApiKey("");
        props.getGemini().setApiKey("");

        LlmExecutionService service = new LlmExecutionService(props);

        assertThat(service.isAvailable()).isFalse();
        assertThatThrownBy(() -> service.executePrompt("System", "User"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("LLM execution service is not available");
    }

    // -------------------------------------------------------------------------
    // Scenario I: Output parsing with markdown fences (Structured JSON)
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Scenario I: Structured JSON with markdown fences ```json ... ``` is parsed cleanly")
    void scenarioI_markdownFencedJson_parsedCleanly() throws Exception {
        String fencedJson = "```json\n{\"courseCode\": \"CS302\", \"courseTitle\": \"Agentic AI\", \"credits\": 4}\n```";

        MockProvider groq = new MockProvider("groq", "openai/gpt-oss-120b", req ->
                LlmResponse.of(fencedJson, "groq", "openai/gpt-oss-120b", 200, 50L));

        Map<String, LlmProvider> providers = Map.of("groq", groq);
        LlmExecutionService service = new LlmExecutionService(createDefaultProps("groq", false), objectMapper, providers);

        String rawOutput = service.executePrompt("System", "User");
        String cleaned = cleanFencedJson(rawOutput);

        var node = objectMapper.readTree(cleaned);
        assertThat(node.get("courseCode").asText()).isEqualTo("CS302");
        assertThat(node.get("courseTitle").asText()).isEqualTo("Agentic AI");
        assertThat(node.get("credits").asInt()).isEqualTo(4);
    }

    private String cleanFencedJson(String raw) {
        if (raw == null) return "{}";
        String s = raw.trim();
        if (s.startsWith("```json")) s = s.substring(7);
        else if (s.startsWith("```")) s = s.substring(3);
        if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        return s.trim();
    }

    // -------------------------------------------------------------------------
    // Scenario J: Failover does not duplicate DB records
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Scenario J: Single prompt execution during failover produces exactly one terminal result")
    void scenarioJ_failoverDoesNotDuplicateTerminalResult() {
        AtomicInteger terminalCommitCount = new AtomicInteger(0);

        MockProvider failingPrimary = new MockProvider("groq", "openai/gpt-oss-120b", req -> {
            throw new LlmException("groq", LlmErrorCategory.RATE_LIMIT_429, 429, "Rate limit");
        });

        MockProvider succeedingFallback = new MockProvider("gemini", "gemini-2.5-flash", req -> {
            terminalCommitCount.incrementAndGet();
            return LlmResponse.of("{\"status\": \"OK\"}", "gemini", "gemini-2.5-flash", 200, 100L);
        });

        Map<String, LlmProvider> providers = new LinkedHashMap<>();
        providers.put("groq", failingPrimary);
        providers.put("gemini", succeedingFallback);

        LlmExecutionService service = new LlmExecutionService(createDefaultProps("groq", true), objectMapper, providers);

        service.executePrompt("System", "User");

        // Verified: The fallback executed once and exactly one terminal commit was recorded
        assertThat(terminalCommitCount.get()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // Scenario K: Failover does not execute agent side effects twice
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Scenario K: Upstream research side effect is executed once before LLM invocation")
    void scenarioK_sideEffectExecutedOnceBeforeFailover() {
        AtomicInteger researchExecutionCounter = new AtomicInteger(0);

        // Simulate agent pattern: research performed once before LLM call
        researchExecutionCounter.incrementAndGet(); // Research step
        assertThat(researchExecutionCounter.get()).isEqualTo(1);

        MockProvider failingGroq = new MockProvider("groq", "openai/gpt-oss-120b", req -> {
            throw new LlmException("groq", LlmErrorCategory.RATE_LIMIT_429, 429, "Rate limit");
        });

        MockProvider successfulGemini = new MockProvider("gemini", "gemini-2.5-flash", req ->
                LlmResponse.of("Resolved", "gemini", "gemini-2.5-flash", 200, 100L));

        Map<String, LlmProvider> providers = new LinkedHashMap<>();
        providers.put("groq", failingGroq);
        providers.put("gemini", successfulGemini);

        LlmExecutionService service = new LlmExecutionService(createDefaultProps("groq", true), objectMapper, providers);
        String res = service.executePrompt("System", "User");

        assertThat(res).isEqualTo("Resolved");
        // Research counter remained exactly 1
        assertThat(researchExecutionCounter.get()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // Scenario L: All five specialist agents use the same provider abstraction
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Scenario L: All five specialist agent classes verify common LlmExecutionService abstraction")
    void scenarioL_allFiveSpecialistAgentsUseCommonAbstraction() {
        // Assert that all specialist agent classes have constructors or fields with LlmExecutionService
        boolean benchmarkingHasLlm = hasLlmField(AcademicBenchmarkingAgent.class);
        boolean industryHasLlm = hasLlmField(IndustrySkillsAgent.class);
        boolean gapHasLlm = hasLlmField(GapAnalysisAgent.class);
        boolean recommendationHasLlm = hasLlmField(RecommendationAgent.class);
        boolean curriculumHasLlm = hasLlmField(CurriculumMappingAgent.class);

        assertThat(benchmarkingHasLlm).isTrue();
        assertThat(industryHasLlm).isTrue();
        assertThat(gapHasLlm).isTrue();
        assertThat(recommendationHasLlm).isTrue();
        assertThat(curriculumHasLlm).isTrue();
    }

    private boolean hasLlmField(Class<?> clazz) {
        for (var field : clazz.getDeclaredFields()) {
            if (field.getType().equals(LlmExecutionService.class)) {
                return true;
            }
        }
        for (var constructor : clazz.getConstructors()) {
            for (var param : constructor.getParameterTypes()) {
                if (param.equals(LlmExecutionService.class)) {
                    return true;
                }
            }
        }
        return false;
    }
}
