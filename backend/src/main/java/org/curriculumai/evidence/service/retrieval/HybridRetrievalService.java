package org.curriculumai.evidence.service.retrieval;

import org.curriculumai.evidence.domain.EvidenceChunk;
import org.curriculumai.evidence.dto.EvidenceChunkDto;
import org.curriculumai.evidence.dto.HybridSearchRequest;
import org.curriculumai.evidence.dto.HybridSearchResponse;
import org.curriculumai.evidence.dto.HybridSearchResultDto;
import org.curriculumai.evidence.service.EvidenceIngestionService;
import org.curriculumai.evidence.service.embedding.EmbeddingConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HybridRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(HybridRetrievalService.class);

    private final SemanticSearchService semanticSearchService;
    private final KeywordSearchService keywordSearchService;
    private final EvidenceIngestionService ingestionService;
    private final EmbeddingConfigProperties config;

    public HybridRetrievalService(SemanticSearchService semanticSearchService,
                                  KeywordSearchService keywordSearchService,
                                  EvidenceIngestionService ingestionService,
                                  EmbeddingConfigProperties config) {
        this.semanticSearchService = semanticSearchService;
        this.keywordSearchService = keywordSearchService;
        this.ingestionService = ingestionService;
        this.config = config;
    }

    /**
     * Executes hybrid retrieval fusing semantic vector similarity and keyword relevance using RRF.
     */
    public HybridSearchResponse searchHybrid(HybridSearchRequest request) {
        String query = request.query();
        int limit = request.resolvedLimit();
        int candidatePoolSize = Math.max(limit * 3, 30);
        int k = config.getRrfConstant() > 0 ? config.getRrfConstant() : 60;

        // 1. Semantic retrieval path
        List<SemanticSearchService.RankedResult> semanticResults = semanticSearchService.searchSemantic(
                query,
                request.sourceType(),
                request.publisher(),
                request.institutionId(),
                request.departmentId(),
                candidatePoolSize
        );

        // 2. Keyword retrieval path
        List<KeywordSearchService.RankedResult> keywordResults = keywordSearchService.searchKeyword(
                query,
                request.sourceType(),
                request.publisher(),
                request.institutionId(),
                request.departmentId(),
                candidatePoolSize
        );

        // 3. Reciprocal Rank Fusion (RRF)
        Map<String, FusedCandidate> fusedMap = new LinkedHashMap<>();

        for (SemanticSearchService.RankedResult sr : semanticResults) {
            String chunkId = sr.chunk().getId();
            double rrfComponent = 1.0 / (k + sr.rank());
            FusedCandidate candidate = fusedMap.computeIfAbsent(chunkId, id -> new FusedCandidate(sr.chunk()));
            candidate.rrfScore += rrfComponent;
            candidate.semanticRank = sr.rank();
            candidate.semanticScore = sr.score();
        }

        for (KeywordSearchService.RankedResult kr : keywordResults) {
            String chunkId = kr.chunk().getId();
            double rrfComponent = 1.0 / (k + kr.rank());
            FusedCandidate candidate = fusedMap.computeIfAbsent(chunkId, id -> new FusedCandidate(kr.chunk()));
            candidate.rrfScore += rrfComponent;
            candidate.keywordRank = kr.rank();
            candidate.keywordScore = kr.score();
        }

        // 4. Deterministic tie-breaking and sorting
        List<FusedCandidate> candidates = new ArrayList<>(fusedMap.values());
        candidates.sort((a, b) -> {
            // Primary: RRF score descending
            int cmpRrf = Double.compare(b.rrfScore, a.rrfScore);
            if (cmpRrf != 0) return cmpRrf;

            // Secondary: Semantic rank ascending (nulls last)
            int semA = a.semanticRank != null ? a.semanticRank : Integer.MAX_VALUE;
            int semB = b.semanticRank != null ? b.semanticRank : Integer.MAX_VALUE;
            int cmpSem = Integer.compare(semA, semB);
            if (cmpSem != 0) return cmpSem;

            // Tertiary: Sequence number ascending
            int seqA = a.chunk.getSequenceNumber() != null ? a.chunk.getSequenceNumber() : 0;
            int seqB = b.chunk.getSequenceNumber() != null ? b.chunk.getSequenceNumber() : 0;
            int cmpSeq = Integer.compare(seqA, seqB);
            if (cmpSeq != 0) return cmpSeq;

            // Quaternary: Chunk ID ascending
            return a.chunk.getId().compareTo(b.chunk.getId());
        });

        // 5. Build DTOs up to limit
        List<HybridSearchResultDto> results = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, candidates.size()); i++) {
            FusedCandidate c = candidates.get(i);
            EvidenceChunkDto chunkDto = ingestionService.mapChunkToDto(c.chunk);
            results.add(new HybridSearchResultDto(
                    chunkDto,
                    c.rrfScore,
                    c.semanticRank,
                    c.semanticScore,
                    c.keywordRank,
                    c.keywordScore
            ));
        }

        log.info("Hybrid retrieval for query '{}' yielded {} results (semantic: {}, keyword: {})",
                query, results.size(), semanticResults.size(), keywordResults.size());

        return new HybridSearchResponse(
                query,
                results.size(),
                results,
                k,
                config.getModel()
        );
    }

    private static class FusedCandidate {
        final EvidenceChunk chunk;
        double rrfScore = 0.0;
        Integer semanticRank = null;
        Double semanticScore = null;
        Integer keywordRank = null;
        Double keywordScore = null;

        FusedCandidate(EvidenceChunk chunk) {
            this.chunk = chunk;
        }
    }
}

