package org.curriculumai.evidence.dto;

import org.curriculumai.evidence.domain.RetrievalOutcome;

public record SourceRetrievalDto(
        String id,
        String sourceId,
        String retrievalTimestamp,
        Integer httpStatus,
        String finalUrl,
        String contentType,
        RetrievalOutcome outcome,
        String failureReason,
        String contentHash,
        String etag,
        Long elapsedMillis,
        String initiatedBy
) {}

