package org.curriculumai.evidence.dto;

import org.curriculumai.evidence.domain.ExtractionStatus;

public record ExtractedDocumentDto(
        String id,
        String sourceVersionId,
        ExtractionStatus extractionStatus,
        String extractorName,
        String extractorVersion,
        String extractionTimestamp,
        String documentTitle,
        String detectedLanguage,
        Integer totalPages,
        Integer totalParagraphs,
        Integer totalTables,
        Integer totalCharacters,
        String errorDetails
) {}

