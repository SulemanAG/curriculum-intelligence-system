package org.curriculumai.evidence.dto;

public record EvidenceChunkDto(
        String id,
        String extractedDocumentId,
        Integer sequenceNumber,
        String stableChunkId,
        String textContent,
        Integer pageNumber,
        String sectionHeading,
        String chunkingStrategy,
        String contentHash,
        ProvenanceDto provenance
) {}

