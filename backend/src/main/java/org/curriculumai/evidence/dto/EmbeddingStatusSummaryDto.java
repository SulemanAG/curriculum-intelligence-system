package org.curriculumai.evidence.dto;

import org.curriculumai.evidence.domain.EmbeddingStatus;

public record EmbeddingStatusSummaryDto(
        String documentId,
        String modelName,
        int dimension,
        int totalChunks,
        int completedChunks,
        int pendingChunks,
        int failedChunks,
        EmbeddingStatus overallStatus,
        String errorMessage
) {}

