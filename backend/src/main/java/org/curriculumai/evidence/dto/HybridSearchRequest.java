package org.curriculumai.evidence.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.curriculumai.evidence.domain.SourceType;

@Schema(description = "Request payload for hybrid semantic + keyword evidence search")
public record HybridSearchRequest(
        @NotBlank(message = "Search query must not be blank")
        @Schema(description = "Natural language search query", example = "Deep learning architectures and transformer models")
        String query,

        @Schema(description = "Optional filter by source type", example = "CURRICULUM_DOCUMENT")
        SourceType sourceType,

        @Schema(description = "Optional filter by publisher or issuing body", example = "ACM/IEEE")
        String publisher,

        @Schema(description = "Optional filter by academic institution ID")
        String institutionId,

        @Schema(description = "Optional filter by department ID")
        String departmentId,

        @Schema(description = "Maximum results to return (default: 10, max: 100)", example = "10")
        Integer limit
) {
    public int resolvedLimit() {
        if (limit == null || limit <= 0) return 10;
        return Math.min(limit, 100);
    }
}

