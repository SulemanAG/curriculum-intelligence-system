package org.curriculumai.evidence.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Hybrid retrieval response fusing semantic embeddings and keyword rankings")
public record HybridSearchResponse(
        @Schema(description = "The submitted natural language query")
        String query,

        @Schema(description = "Total number of fused passages returned")
        int totalFound,

        @Schema(description = "Ranked list of evidence passages ordered by RRF score descending")
        List<HybridSearchResultDto> results,

        @Schema(description = "RRF smoothing constant k used for rank fusion", example = "60")
        int rrfConstant,

        @Schema(description = "Target embedding model name", example = "BAAI/bge-small-en-v1.5")
        String embeddingModel
) {}

