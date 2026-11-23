package org.curriculumai.evidence.dto;

public record SourceVersionDto(
        String id,
        String sourceId,
        String retrievalId,
        String contentHash,
        String versionIdentifier,
        String originalPublicationDate,
        String retrievedTimestamp,
        String originalFilename,
        String mimeType,
        Long fileSizeBytes,
        String storageReference
) {}

