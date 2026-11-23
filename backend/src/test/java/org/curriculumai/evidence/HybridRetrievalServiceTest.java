package org.curriculumai.evidence;

import org.curriculumai.evidence.domain.EvidenceChunk;
import org.curriculumai.evidence.dto.EvidenceChunkDto;
import org.curriculumai.evidence.dto.HybridSearchRequest;
import org.curriculumai.evidence.dto.HybridSearchResponse;
import org.curriculumai.evidence.service.EvidenceIngestionService;
import org.curriculumai.evidence.service.embedding.EmbeddingConfigProperties;
import org.curriculumai.evidence.service.retrieval.HybridRetrievalService;
import org.curriculumai.evidence.service.retrieval.KeywordSearchService;
import org.curriculumai.evidence.service.retrieval.SemanticSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HybridRetrievalServiceTest {

    @Mock
    private SemanticSearchService semanticSearchService;

    @Mock
    private KeywordSearchService keywordSearchService;

    @Mock
    private EvidenceIngestionService ingestionService;

    private EmbeddingConfigProperties config;
    private HybridRetrievalService hybridRetrievalService;

    @BeforeEach
    void setUp() {
        config = new EmbeddingConfigProperties();
        config.setRrfConstant(60);
        config.setModel("BAAI/bge-small-en-v1.5");
        config.setDimension(384);

        hybridRetrievalService = new HybridRetrievalService(
                semanticSearchService,
                keywordSearchService,
                ingestionService,
                config
        );
    }

    private EvidenceChunk createMockChunk(String id, int sequence) {
        EvidenceChunk chunk = new EvidenceChunk();
        chunk.setId(id);
        chunk.setExtractedDocumentId("doc-1");
        chunk.setSequenceNumber(sequence);
        chunk.setTextContent("Sample text for " + id);
        return chunk;
    }

    private EvidenceChunkDto createMockDto(EvidenceChunk c) {
        return new EvidenceChunkDto(
                c.getId(),
                c.getExtractedDocumentId(),
                c.getSequenceNumber(),
                "stable-" + c.getId(),
                c.getTextContent(),
                1,
                "Introduction",
                "PARAGRAPH",
                "hash",
                null
        );
    }

    @Test
    @DisplayName("Reciprocal Rank Fusion correctly combines semantic and keyword ranks")
    void testReciprocalRankFusionCalculation() {
        EvidenceChunk chunk1 = createMockChunk("chk-1", 1);
        EvidenceChunk chunk2 = createMockChunk("chk-2", 2);
        EvidenceChunk chunk3 = createMockChunk("chk-3", 3);

        when(ingestionService.mapChunkToDto(any())).thenAnswer(invocation -> {
            EvidenceChunk c = invocation.getArgument(0);
            return createMockDto(c);
        });

        // Semantic: chk-1 (rank 1), chk-2 (rank 2)
        when(semanticSearchService.searchSemantic(eq("neural networks"), isNull(), isNull(), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(
                        new SemanticSearchService.RankedResult(chunk1, 1, 0.95, 0.05),
                        new SemanticSearchService.RankedResult(chunk2, 2, 0.85, 0.15)
                ));

        // Keyword: chk-2 (rank 1), chk-3 (rank 2)
        when(keywordSearchService.searchKeyword(eq("neural networks"), isNull(), isNull(), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(
                        new KeywordSearchService.RankedResult(chunk2, 1, 3.5),
                        new KeywordSearchService.RankedResult(chunk3, 2, 2.0)
                ));

        HybridSearchRequest req = new HybridSearchRequest("neural networks", null, null, null, null, 10);
        HybridSearchResponse resp = hybridRetrievalService.searchHybrid(req);

        assertEquals("neural networks", resp.query());
        assertEquals(3, resp.totalFound());
        assertEquals(60, resp.rrfConstant());
        assertEquals("BAAI/bge-small-en-v1.5", resp.embeddingModel());

        // Expected RRF scores with k=60:
        // chk-2 appears in both: 1/(60+2) + 1/(60+1) = 1/62 + 1/61 ≈ 0.016129 + 0.016393 = 0.032522
        // chk-1 appears in semantic only: 1/(60+1) = 1/61 ≈ 0.016393
        // chk-3 appears in keyword only: 1/(60+2) = 1/62 ≈ 0.016129
        // Therefore, chk-2 should be ranked 1st!

        assertEquals("chk-2", resp.results().get(0).chunk().id());
        assertEquals(0.032522, resp.results().get(0).rrfScore(), 1e-4);
        assertEquals(2, resp.results().get(0).semanticRank());
        assertEquals(1, resp.results().get(0).keywordRank());

        assertEquals("chk-1", resp.results().get(1).chunk().id());
        assertEquals(0.016393, resp.results().get(1).rrfScore(), 1e-4);
        assertEquals(1, resp.results().get(1).semanticRank());
        assertNull(resp.results().get(1).keywordRank());

        assertEquals("chk-3", resp.results().get(2).chunk().id());
        assertEquals(0.016129, resp.results().get(2).rrfScore(), 1e-4);
        assertNull(resp.results().get(2).semanticRank());
        assertEquals(2, resp.results().get(2).keywordRank());
    }

    @Test
    @DisplayName("Deterministic tie-breaking preserves consistent ordering when RRF scores are equal")
    void testDeterministicTieBreaking() {
        EvidenceChunk chunkA = createMockChunk("chk-A", 1);
        EvidenceChunk chunkB = createMockChunk("chk-B", 2);

        when(ingestionService.mapChunkToDto(any())).thenAnswer(invocation -> createMockDto(invocation.getArgument(0)));

        // Both appear only in semantic with identical rank 1
        when(semanticSearchService.searchSemantic(anyString(), any(), any(), any(), any(), anyInt()))
                .thenReturn(List.of(
                        new SemanticSearchService.RankedResult(chunkB, 1, 0.90, 0.10),
                        new SemanticSearchService.RankedResult(chunkA, 1, 0.90, 0.10)
                ));
        when(keywordSearchService.searchKeyword(anyString(), any(), any(), any(), any(), anyInt()))
                .thenReturn(java.util.Collections.emptyList());

        HybridSearchRequest req = new HybridSearchRequest("deep learning", null, null, null, null, 10);
        HybridSearchResponse resp = hybridRetrievalService.searchHybrid(req);

        assertEquals(2, resp.totalFound());
        // Tie broken by sequenceNumber ascending: chk-A (seq 1) before chk-B (seq 2)
        assertEquals("chk-A", resp.results().get(0).chunk().id());
        assertEquals("chk-B", resp.results().get(1).chunk().id());
    }

    @Test
    @DisplayName("Dual-path retrieval exposes non-null semantic and keyword scores and combines RRF contributions")
    void testDualPathRrfVerificationExposingNonNullableMetrics() {
        EvidenceChunk sharedChunk = createMockChunk("chk-hybrid-core", 1);
        EvidenceChunk semanticOnlyChunk = createMockChunk("chk-sem-only", 2);
        EvidenceChunk keywordOnlyChunk = createMockChunk("chk-kw-only", 3);

        when(ingestionService.mapChunkToDto(any())).thenAnswer(invocation -> createMockDto(invocation.getArgument(0)));

        when(semanticSearchService.searchSemantic(eq("distributed consensus"), isNull(), isNull(), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(
                        new SemanticSearchService.RankedResult(sharedChunk, 1, 0.942, 0.058),
                        new SemanticSearchService.RankedResult(semanticOnlyChunk, 2, 0.815, 0.185)
                ));

        when(keywordSearchService.searchKeyword(eq("distributed consensus"), isNull(), isNull(), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(
                        new KeywordSearchService.RankedResult(sharedChunk, 1, 4.2),
                        new KeywordSearchService.RankedResult(keywordOnlyChunk, 2, 1.8)
                ));

        HybridSearchRequest req = new HybridSearchRequest("distributed consensus", null, null, null, null, 10);
        HybridSearchResponse resp = hybridRetrievalService.searchHybrid(req);

        assertNotNull(resp);
        assertEquals(3, resp.totalFound());

        // Top result must be the shared chunk receiving both contributions
        var top = resp.results().get(0);
        assertEquals("chk-hybrid-core", top.chunk().id());
        assertNotNull(top.semanticRank(), "Semantic rank must be non-null for chunk found via semantic search");
        assertNotNull(top.semanticScore(), "Semantic score must be non-null for chunk found via semantic search");
        assertNotNull(top.keywordRank(), "Keyword rank must be non-null for chunk found via keyword search");
        assertNotNull(top.keywordScore(), "Keyword score must be non-null for chunk found via keyword search");

        assertEquals(1, top.semanticRank());
        assertEquals(0.942, top.semanticScore(), 1e-4);
        assertEquals(1, top.keywordRank());
        assertEquals(4.2, top.keywordScore(), 1e-4);

        // Expected RRF: 1/(60+1) + 1/(60+1) = 2/61 ≈ 0.03278688
        double expectedRrf = (1.0 / 61.0) + (1.0 / 61.0);
        assertEquals(expectedRrf, top.rrfScore(), 1e-6);

        // Verify semantic-only chunk has null keyword metrics
        var semOnly = resp.results().stream().filter(r -> r.chunk().id().equals("chk-sem-only")).findFirst().orElseThrow();
        assertNotNull(semOnly.semanticRank());
        assertNotNull(semOnly.semanticScore());
        assertNull(semOnly.keywordRank());
        assertNull(semOnly.keywordScore());
        assertEquals(1.0 / 62.0, semOnly.rrfScore(), 1e-6);

        // Verify keyword-only chunk has null semantic metrics
        var kwOnly = resp.results().stream().filter(r -> r.chunk().id().equals("chk-kw-only")).findFirst().orElseThrow();
        assertNull(kwOnly.semanticRank());
        assertNull(kwOnly.semanticScore());
        assertNotNull(kwOnly.keywordRank());
        assertNotNull(kwOnly.keywordScore());
        assertEquals(1.0 / 62.0, kwOnly.rrfScore(), 1e-6);
    }
}
