package org.curriculumai.orchestration.llm.provider;

import java.util.Map;

public record LlmResponse(
        String content,
        String providerName,
        String modelName,
        int statusCode,
        long durationMs,
        boolean failoverOccurred,
        String failoverFrom,
        Map<String, Object> metadata
) {
    public static LlmResponse of(String content, String providerName, String modelName, int statusCode, long durationMs) {
        return new LlmResponse(content, providerName, modelName, statusCode, durationMs, false, null, Map.of());
    }

    public static LlmResponse ofFailover(String content, String providerName, String modelName, int statusCode, long durationMs, String failoverFrom) {
        return new LlmResponse(content, providerName, modelName, statusCode, durationMs, true, failoverFrom, Map.of());
    }
}

