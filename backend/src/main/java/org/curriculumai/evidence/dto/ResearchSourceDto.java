package org.curriculumai.evidence.dto;

import org.curriculumai.evidence.domain.SourceType;

public record ResearchSourceDto(
        String id,
        String canonicalUrl,
        SourceType sourceType,
        String publisher,
        String title,
        String domain,
        String sourceClassification,
        String institutionId,
        String departmentId,
        String programmeId,
        Boolean isActive,
        String createdBy,
        String createdAt,
        String updatedAt
) {}

