package org.curriculumai.orchestration.llm.provider;

public class LlmException extends RuntimeException {

    private final String providerName;
    private final LlmErrorCategory category;
    private final int statusCode;

    public LlmException(String providerName, LlmErrorCategory category, int statusCode, String message) {
        super(String.format("[%s] %s (HTTP %d): %s", providerName, category, statusCode, message));
        this.providerName = providerName;
        this.category = category;
        this.statusCode = statusCode;
    }

    public LlmException(String providerName, LlmErrorCategory category, int statusCode, String message, Throwable cause) {
        super(String.format("[%s] %s (HTTP %d): %s", providerName, category, statusCode, message), cause);
        this.providerName = providerName;
        this.category = category;
        this.statusCode = statusCode;
    }

    public String getProviderName() {
        return providerName;
    }

    public LlmErrorCategory getCategory() {
        return category;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isFailoverEligible() {
        return category != null && category.isFailoverEligible();
    }
}

