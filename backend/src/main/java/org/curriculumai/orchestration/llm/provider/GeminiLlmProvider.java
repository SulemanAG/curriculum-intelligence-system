package org.curriculumai.orchestration.llm.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeminiLlmProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiLlmProvider.class);

    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;

    public GeminiLlmProvider(String apiKey, String model, ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model != null && !model.isBlank() ? model.trim() : "gemini-2.5-flash";
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public String getName() {
        return "gemini";
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    @Override
    public LlmResponse execute(LlmRequest request) throws LlmException {
        if (!isConfigured()) {
            throw new LlmException(getName(), LlmErrorCategory.UNCONFIGURED, 0,
                    "Google Gemini API key is unconfigured. Cannot execute request.");
        }

        long startMs = System.currentTimeMillis();
        int timeoutMs = (request.timeoutSeconds() > 0 ? request.timeoutSeconds() : 60) * 1000;
        String endpoint = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                model, apiKey);

        try {
            URI uri = URI.create(endpoint);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setDoOutput(true);

            Map<String, Object> requestMap = new LinkedHashMap<>();
            if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
                requestMap.put("system_instruction", Map.of(
                        "parts", List.of(Map.of("text", request.systemPrompt()))
                ));
            }

            requestMap.put("contents", List.of(
                    Map.of(
                            "role", "user",
                            "parts", List.of(Map.of("text", request.userPrompt() != null ? request.userPrompt() : ""))
                    )
            ));

            Map<String, Object> genConfig = new LinkedHashMap<>();
            genConfig.put("temperature", request.temperature());
            if (request.maxTokens() > 0) {
                genConfig.put("maxOutputTokens", request.maxTokens());
            }
            requestMap.put("generationConfig", genConfig);

            String jsonPayload = objectMapper.writeValueAsString(requestMap);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int statusCode = conn.getResponseCode();
            long durationMs = System.currentTimeMillis() - startMs;

            if (statusCode == 200) {
                byte[] respBytes;
                try (InputStream is = conn.getInputStream()) {
                    respBytes = is.readAllBytes();
                }
                JsonNode root = objectMapper.readTree(respBytes);
                JsonNode candidate = root.path("candidates").path(0);
                String content = candidate.path("content").path("parts").path(0).path("text").asText("");
                return LlmResponse.of(content, getName(), model, statusCode, durationMs);
            }

            String errorMsg = "";
            try (InputStream es = conn.getErrorStream()) {
                if (es != null) {
                    errorMsg = new String(es.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (Exception ignored) {}

            LlmErrorCategory category = classifyStatusCode(statusCode, errorMsg);
            log.warn("Gemini execution failed with HTTP {} (category={}) in {}ms: {}", statusCode, category, durationMs, errorMsg);
            throw new LlmException(getName(), category, statusCode, errorMsg);

        } catch (SocketTimeoutException ste) {
            long durationMs = System.currentTimeMillis() - startMs;
            throw new LlmException(getName(), LlmErrorCategory.TIMEOUT, 408,
                    "Gemini request timed out after " + durationMs + "ms: " + ste.getMessage(), ste);
        } catch (LlmException le) {
            throw le;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startMs;
            LlmErrorCategory category = classifyException(e);
            throw new LlmException(getName(), category, 0,
                    "Gemini request failed in " + durationMs + "ms: " + e.getMessage(), e);
        }
    }

    private LlmErrorCategory classifyStatusCode(int statusCode, String errorMsg) {
        if (statusCode == 429) return LlmErrorCategory.RATE_LIMIT_429;
        if (statusCode == 401) return LlmErrorCategory.AUTH_ERROR_401;
        if (statusCode == 403) return LlmErrorCategory.AUTH_ERROR_403;
        if (statusCode == 400) return LlmErrorCategory.INVALID_REQUEST_400;
        if (statusCode >= 500) return LlmErrorCategory.SERVER_ERROR_5XX;
        return LlmErrorCategory.UNKNOWN;
    }

    private LlmErrorCategory classifyException(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (e instanceof SocketTimeoutException || msg.contains("timeout") || msg.contains("timed out")) {
            return LlmErrorCategory.TIMEOUT;
        }
        if (msg.contains("refused") || msg.contains("reset") || msg.contains("host") || msg.contains("unknownhost")) {
            return LlmErrorCategory.NETWORK_ERROR;
        }
        return LlmErrorCategory.UNKNOWN;
    }
}

