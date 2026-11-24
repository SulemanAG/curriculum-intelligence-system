package org.curriculumai.orchestration.llm.provider;

public interface LlmProvider {

    /**
     * Unique identifier for provider, e.g. "groq" or "gemini".
     */
    String getName();

    /**
     * Convenience alias for getName().
     */
    default String getProviderName() {
        return getName();
    }

    /**
     * Currently configured model identifier.
     */
    String getModelName();

    /**
     * Whether provider has valid API credentials configured.
     */
    boolean isConfigured();

    /**
     * Executes the requested prompt against this provider.
     *
     * @param request unified LLM request parameters
     * @return unified LLM response
     * @throws LlmException on provider failure (contains classification and status code)
     */
    LlmResponse execute(LlmRequest request) throws LlmException;
}
