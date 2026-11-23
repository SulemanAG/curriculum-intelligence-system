package org.curriculumai.evidence.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Individual retrieved evidence passage with Reciprocal Rank Fusion (RRF) metrics and provenance")
public record HybridSearchResultDto(
        @Schema(description = "The matched evidence chunk passage with full provenance citation")
        EvidenceChunkDto chunk,

        @Schema(description = "Reciprocal Rank Fusion score: sum(1 / (k + rank))", example = "0.03278")
        double rrfScore,

        @Schema(description = "1-based rank in the semantic retrieval path (null if not in semantic candidate set)", example = "1")
        Integer semanticRank,

        @Schema(description = "Raw cosine similarity score [0.0 - 1.0] from semantic retrieval", example = "0.8924")
        Double semanticScore,

        @Schema(description = "1-based rank in the keyword retrieval path (null if not in keyword candidate set)", example = "2")
        Integer keywordRank,

        @Schema(description = "Raw keyword match score from PostgreSQL Full-Text Search / text ranking", example = "0.7512")
        Double keywordScore
) {}

