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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroqLlmProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(GroqLlmProvider.class);

    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public GroqLlmProvider(String apiKey, String model, String baseUrl, ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model != null && !model.isBlank() ? model.trim() : "openai/gpt-oss-120b";
        this.baseUrl = baseUrl != null && !baseUrl.isBlank()
                ? baseUrl.replaceAll("/+$", "")
                : "https://api.groq.com/openai/v1";
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public String getName() {
        return "groq";
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
                    "Groq API key is unconfigured. Cannot execute request.");
        }

        long startMs = System.currentTimeMillis();
        String endpoint = baseUrl.endsWith("/chat/completions") ? baseUrl : baseUrl + "/chat/completions";
        int timeoutMs = (request.timeoutSeconds() > 0 ? request.timeoutSeconds() : 60) * 1000;

        try {
            URI uri = URI.create(endpoint);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setDoOutput(true);

            List<Map<String, String>> messages = new ArrayList<>();
            if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
                messages.add(Map.of("role", "system", "content", request.systemPrompt()));
            }
            messages.add(Map.of("role", "user", "content", request.userPrompt() != null ? request.userPrompt() : ""));

            Map<String, Object> requestMap = new LinkedHashMap<>();
            requestMap.put("model", model);
            requestMap.put("messages", messages);
            requestMap.put("temperature", request.temperature());
            if (request.maxTokens() > 0) {
                requestMap.put("max_tokens", request.maxTokens());
            }

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
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    String content = choices.get(0).path("message").path("content").asText("");
                    return LlmResponse.of(content, getName(), model, statusCode, durationMs);
                }
                throw new LlmException(getName(), LlmErrorCategory.SERVER_ERROR_5XX, statusCode,
                        "Groq returned HTTP 200 but payload contained no message choices.");
            }

            String errorMsg = "";
            try (InputStream es = conn.getErrorStream()) {
                if (es != null) {
                    errorMsg = new String(es.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (Exception ignored) {}

            LlmErrorCategory category = classifyStatusCode(statusCode, errorMsg);
            log.warn("Groq execution failed with HTTP {} (category={}) in {}ms: {}", statusCode, category, durationMs, errorMsg);
            throw new LlmException(getName(), category, statusCode, errorMsg);

        } catch (SocketTimeoutException ste) {
            long durationMs = System.currentTimeMillis() - startMs;
            throw new LlmException(getName(), LlmErrorCategory.TIMEOUT, 408,
                    "Groq request timed out after " + durationMs + "ms: " + ste.getMessage(), ste);
        } catch (LlmException le) {
            throw le;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startMs;
            LlmErrorCategory category = classifyException(e);
            throw new LlmException(getName(), category, 0,
                    "Groq request failed in " + durationMs + "ms: " + e.getMessage(), e);
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

