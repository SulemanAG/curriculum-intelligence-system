package org.curriculumai.evidence.service.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class HttpEmbeddingProvider implements EmbeddingProvider {

    private static final Logger log = LoggerFactory.getLogger(HttpEmbeddingProvider.class);

    private final EmbeddingConfigProperties config;
    private final ObjectMapper objectMapper;

    public HttpEmbeddingProvider(EmbeddingConfigProperties config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getModelName() {
        return config.getModel();
    }

    @Override
    public int getDimension() {
        return config.getDimension();
    }

    @Override
    public boolean isAvailable() {
        return config.isEnabled() && config.getApiUrl() != null && !config.getApiUrl().isBlank();
    }

    @Override
    public float[] generateEmbedding(String text) throws EmbeddingException {
        if (text == null || text.isBlank()) {
            throw new EmbeddingException("Cannot generate embedding for null or blank text");
        }
        List<float[]> results = generateEmbeddings(Collections.singletonList(text));
        if (results.isEmpty()) {
            throw new EmbeddingException("Provider returned empty embeddings list for single input");
        }
        return results.get(0);
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> texts) throws EmbeddingException {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }

        if (!isAvailable()) {
            throw new EmbeddingException(
                    "Embedding provider is not available or unconfigured. (EMBEDDING_API_URL is empty)"
            );
        }

        int maxRetries = Math.max(1, config.getMaxRetries());
        long delayMs = Math.max(100L, config.getRetryDelayMs());
        EmbeddingException lastError = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return executeBatchRequest(texts);
            } catch (EmbeddingException e) {
                lastError = e;
                log.warn("Embedding generation attempt {}/{} failed: {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delayMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new EmbeddingException("Embedding generation interrupted during retry delay", ie);
                    }
                }
            } catch (Exception e) {
                lastError = new EmbeddingException("Unexpected error during embedding generation: " + e.getMessage(), e);
                log.warn("Embedding generation attempt {}/{} failed with unexpected error: {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delayMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new EmbeddingException("Embedding generation interrupted during retry delay", ie);
                    }
                }
            }
        }

        throw lastError != null ? lastError : new EmbeddingException("Embedding generation failed after retries");
    }

    private List<float[]> executeBatchRequest(List<String> texts) throws Exception {
        URI uri = URI.create(config.getApiUrl().trim());
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(config.getTimeoutSeconds() * 1000);
        conn.setReadTimeout(config.getTimeoutSeconds() * 1000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");

        if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
            conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey().trim());
        }

        conn.setDoOutput(true);

        // Prepare JSON payload: try standard TEI/HF format first: ["text1", "text2"]
        // Or if OpenAI style endpoint: {"input": ["text1", "text2"], "model": "..."}
        String payload;
        if (config.getApiUrl().contains("/v1/embeddings") || config.getApiUrl().contains("openai")) {
            payload = objectMapper.writeValueAsString(new OpenAiEmbeddingRequest(config.getModel(), texts));
        } else {
            payload = objectMapper.writeValueAsString(texts);
        }

        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
            os.flush();
        }

        int statusCode = conn.getResponseCode();
        if (statusCode < 200 || statusCode >= 300) {
            String errorBody = "";
            try (InputStream es = conn.getErrorStream()) {
                if (es != null) {
                    errorBody = new String(es.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (Exception ignored) {
            }
            throw new EmbeddingException("Embedding HTTP request failed with status " + statusCode + ": " + errorBody);
        }

        byte[] responseBytes;
        try (InputStream is = conn.getInputStream()) {
            responseBytes = is.readAllBytes();
        }

        JsonNode rootNode = objectMapper.readTree(responseBytes);
        List<float[]> embeddings = parseEmbeddingsFromResponse(rootNode, texts.size());

        // Validate every vector
        for (float[] emb : embeddings) {
            EmbeddingValidationUtils.validateVector(emb, config.getDimension());
        }

        if (embeddings.size() != texts.size()) {
            throw new EmbeddingValidationException(
                    "Embedding count mismatch: expected " + texts.size() + " vectors, got " + embeddings.size()
            );
        }

        return embeddings;
    }

    private List<float[]> parseEmbeddingsFromResponse(JsonNode rootNode, int expectedCount) {
        List<float[]> results = new ArrayList<>();

        if (rootNode.isArray()) {
            // Case 1: Array of arrays: [[0.1, 0.2, ...], ...]
            if (!rootNode.isEmpty() && rootNode.get(0).isArray()) {
                for (JsonNode vectorNode : rootNode) {
                    results.add(jsonNodeToFloatArray(vectorNode));
                }
                return results;
            }
            // Case 2: Single flat vector: [0.1, 0.2, ...] (for single input)
            if (expectedCount == 1 && !rootNode.isEmpty() && rootNode.get(0).isNumber()) {
                results.add(jsonNodeToFloatArray(rootNode));
                return results;
            }
        } else if (rootNode.isObject()) {
            // Case 3: OpenAI format: {"data": [{"embedding": [...]}, ...]}
            JsonNode dataNode = rootNode.get("data");
            if (dataNode != null && dataNode.isArray()) {
                for (JsonNode item : dataNode) {
                    JsonNode embNode = item.get("embedding");
                    if (embNode != null && embNode.isArray()) {
                        results.add(jsonNodeToFloatArray(embNode));
                    }
                }
                return results;
            }
            // Case 4: Single embedding object: {"embedding": [...]}
            JsonNode embNode = rootNode.get("embedding");
            if (embNode != null && embNode.isArray()) {
                results.add(jsonNodeToFloatArray(embNode));
                return results;
            }
        }

        throw new EmbeddingValidationException("Unrecognized embedding response format: " + rootNode.getNodeType());
    }

    private float[] jsonNodeToFloatArray(JsonNode arrayNode) {
        int size = arrayNode.size();
        float[] vector = new float[size];
        for (int i = 0; i < size; i++) {
            JsonNode item = arrayNode.get(i);
            if (item.isNumber()) {
                vector[i] = (float) item.asDouble();
            } else {
                throw new EmbeddingValidationException("Vector element at index " + i + " is not a number");
            }
        }
        return vector;
    }

    private record OpenAiEmbeddingRequest(String model, List<String> input) {}
}

