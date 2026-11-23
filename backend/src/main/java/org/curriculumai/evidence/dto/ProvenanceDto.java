package org.curriculumai.evidence.dto;

import org.curriculumai.evidence.domain.SourceType;

public record ProvenanceDto(
        String sourceId,
        String canonicalUrl,
        SourceType sourceType,
        String publisher,
        String sourceTitle,
        String originalPublicationDate,
        String retrievedTimestamp,
        String versionIdentifier,
        String contentHash,
        String documentTitle,
        Integer pageNumber,
        String sectionHeading
) {}

