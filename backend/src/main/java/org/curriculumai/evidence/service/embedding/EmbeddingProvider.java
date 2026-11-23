package org.curriculumai.evidence.service.embedding;

import java.util.List;

public interface EmbeddingProvider {
    String getModelName();
    int getDimension();
    List<float[]> generateEmbeddings(List<String> texts) throws EmbeddingException;
    float[] generateEmbedding(String text) throws EmbeddingException;
    boolean isAvailable();
}

