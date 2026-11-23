package org.curriculumai.evidence;

import org.curriculumai.evidence.service.embedding.EmbeddingValidationException;
import org.curriculumai.evidence.service.embedding.EmbeddingValidationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingValidationUtilsTest {

    @Test
    @DisplayName("Valid 384-dimensional vector passes validation")
    void testValidVectorPasses() {
        float[] vector = new float[384];
        Arrays.fill(vector, 0.05f);
        assertDoesNotThrow(() -> EmbeddingValidationUtils.validateVector(vector, 384));
    }

    @Test
    @DisplayName("Null vector throws EmbeddingValidationException")
    void testNullVectorThrows() {
        assertThrows(EmbeddingValidationException.class,
                () -> EmbeddingValidationUtils.validateVector(null, 384));
    }

    @Test
    @DisplayName("Dimension mismatch throws EmbeddingValidationException")
    void testDimensionMismatchThrows() {
        float[] vector = new float[128];
        assertThrows(EmbeddingValidationException.class,
                () -> EmbeddingValidationUtils.validateVector(vector, 384));

        float[] vector768 = new float[768];
        assertThrows(EmbeddingValidationException.class,
                () -> EmbeddingValidationUtils.validateVector(vector768, 384));
    }

    @Test
    @DisplayName("Non-finite values (NaN, Infinity) throw EmbeddingValidationException")
    void testNonFiniteValuesThrow() {
        float[] nanVector = new float[384];
        nanVector[42] = Float.NaN;
        assertThrows(EmbeddingValidationException.class,
                () -> EmbeddingValidationUtils.validateVector(nanVector, 384));

        float[] infVector = new float[384];
        infVector[100] = Float.POSITIVE_INFINITY;
        assertThrows(EmbeddingValidationException.class,
                () -> EmbeddingValidationUtils.validateVector(infVector, 384));

        float[] negInfVector = new float[384];
        negInfVector[200] = Float.NEGATIVE_INFINITY;
        assertThrows(EmbeddingValidationException.class,
                () -> EmbeddingValidationUtils.validateVector(negInfVector, 384));
    }

    @Test
    @DisplayName("Cosine similarity returns expected values for orthogonal and identical vectors")
    void testCosineSimilarityCalculation() {
        float[] a = new float[]{1.0f, 0.0f, 0.0f};
        float[] b = new float[]{1.0f, 0.0f, 0.0f};
        float[] c = new float[]{0.0f, 1.0f, 0.0f};
        float[] d = new float[]{-1.0f, 0.0f, 0.0f};

        assertEquals(1.0, EmbeddingValidationUtils.cosineSimilarity(a, b), 1e-6);
        assertEquals(0.0, EmbeddingValidationUtils.cosineSimilarity(a, c), 1e-6);
        assertEquals(-1.0, EmbeddingValidationUtils.cosineSimilarity(a, d), 1e-6);

        assertEquals(0.0, EmbeddingValidationUtils.cosineDistance(a, b), 1e-6);
        assertEquals(1.0, EmbeddingValidationUtils.cosineDistance(a, c), 1e-6);
        assertEquals(2.0, EmbeddingValidationUtils.cosineDistance(a, d), 1e-6);
    }

    @Test
    @DisplayName("JSON serialization and deserialization roundtrip preserves all float values")
    void testJsonRoundtrip() {
        float[] original = new float[384];
        for (int i = 0; i < 384; i++) {
            original[i] = (float) (Math.sin(i) * 0.5);
        }

        String json = EmbeddingValidationUtils.toJson(original);
        assertNotNull(json);
        assertTrue(json.startsWith("[") && json.endsWith("]"));

        float[] restored = EmbeddingValidationUtils.fromJson(json);
        assertNotNull(restored);
        assertEquals(384, restored.length);

        for (int i = 0; i < 384; i++) {
            assertEquals(original[i], restored[i], 1e-5f);
        }
    }

    @Test
    @DisplayName("PostgreSQL array string format renders correctly")
    void testPgArrayStringFormatting() {
        float[] v = new float[]{0.1f, 0.2f, 0.3f};
        String pgArray = EmbeddingValidationUtils.toPgArrayString(v);
        assertEquals("{0.1,0.2,0.3}", pgArray);
    }
}

