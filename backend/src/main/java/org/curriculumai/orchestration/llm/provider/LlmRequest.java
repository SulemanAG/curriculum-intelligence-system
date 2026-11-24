package org.curriculumai.orchestration.llm.provider;

public record LlmRequest(
        String systemPrompt,
        String userPrompt,
        int maxTokens,
        double temperature,
        int timeoutSeconds,
        String requestId,
        String runId,
        String agentId
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String systemPrompt = "";
        private String userPrompt = "";
        private int maxTokens = 3500;
        private double temperature = 0.2;
        private int timeoutSeconds = 60;
        private String requestId;
        private String runId;
        private String agentId;

        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder userPrompt(String userPrompt) {
            this.userPrompt = userPrompt;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder timeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder runId(String runId) {
            this.runId = runId;
            return this;
        }

        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        public LlmRequest build() {
            return new LlmRequest(systemPrompt, userPrompt, maxTokens, temperature, timeoutSeconds, requestId, runId, agentId);
        }
    }
}

