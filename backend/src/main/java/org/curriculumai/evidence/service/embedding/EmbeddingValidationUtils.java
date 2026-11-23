package org.curriculumai.evidence.service.embedding;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public final class EmbeddingValidationUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private EmbeddingValidationUtils() {
    }

    /**
     * Validates that the vector is non-null, has exact expected dimension, and contains only finite numbers.
     */
    public static void validateVector(float[] vector, int expectedDimension) {
        if (vector == null) {
            throw new EmbeddingValidationException("Embedding vector cannot be null");
        }
        if (vector.length != expectedDimension) {
            throw new EmbeddingValidationException(
                    "Invalid embedding dimension: expected " + expectedDimension + ", got " + vector.length
            );
        }
        for (int i = 0; i < vector.length; i++) {
            if (!Float.isFinite(vector[i])) {
                throw new EmbeddingValidationException(
                        "Embedding vector contains non-finite value at index " + i + ": " + vector[i]
                );
            }
        }
    }

    /**
     * Serializes a float[] vector to JSON array string.
     */
    public static String toJson(float[] vector) {
        if (vector == null) return null;
        try {
            return OBJECT_MAPPER.writeValueAsString(vector);
        } catch (Exception e) {
            throw new EmbeddingValidationException("Failed to serialize embedding vector to JSON: " + e.getMessage());
        }
    }

    /**
     * Deserializes a JSON array string to float[] vector.
     */
    public static float[] fromJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            List<Double> list = OBJECT_MAPPER.readValue(json, new TypeReference<List<Double>>() {});
            float[] result = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                result[i] = list.get(i).floatValue();
            }
            return result;
        } catch (Exception e) {
            throw new EmbeddingValidationException("Failed to deserialize embedding vector from JSON: " + e.getMessage());
        }
    }

    /**
     * Formats float[] to PostgreSQL array literal "{val1,val2,...}".
     */
    public static String toPgArrayString(float[] vector) {
        if (vector == null) return "{}";
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(vector[i]);
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Computes cosine similarity between two float vectors.
     */
    public static double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA <= 0.0 || normB <= 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Computes cosine distance between two float vectors (1.0 - cosineSimilarity).
     */
    public static double cosineDistance(float[] a, float[] b) {
        return 1.0 - cosineSimilarity(a, b);
    }
}

