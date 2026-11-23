package org.curriculumai.evidence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.curriculumai.evidence.domain.SourceType;

public record RegisterSourceRequest(
        @NotBlank(message = "Canonical URL or identifier is required")
        String canonicalUrl,

        @NotNull(message = "Source type is required")
        SourceType sourceType,

        String publisher,
        String title,
        String sourceClassification,
        String institutionId,
        String departmentId,
        String programmeId
) {}

