package org.curriculumai.evidence;

import org.curriculumai.evidence.domain.EmbeddingStatus;
import org.curriculumai.evidence.domain.EvidenceChunk;
import org.curriculumai.evidence.domain.EvidenceChunkEmbedding;
import org.curriculumai.evidence.domain.ExtractedDocument;
import org.curriculumai.evidence.dto.EmbeddingStatusSummaryDto;
import org.curriculumai.evidence.repository.EvidenceChunkEmbeddingRepository;
import org.curriculumai.evidence.repository.EvidenceChunkRepository;
import org.curriculumai.evidence.repository.ExtractedDocumentRepository;
import org.curriculumai.evidence.service.embedding.EmbeddingConfigProperties;
import org.curriculumai.evidence.service.embedding.EmbeddingProvider;
import org.curriculumai.evidence.service.embedding.EvidenceEmbeddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EvidenceEmbeddingServiceTest {

    @Mock
    private EvidenceChunkEmbeddingRepository embeddingRepository;

    @Mock
    private EvidenceChunkRepository chunkRepository;

    @Mock
    private ExtractedDocumentRepository documentRepository;

    @Mock
    private EmbeddingProvider embeddingProvider;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private EmbeddingConfigProperties config;
    private EvidenceEmbeddingService service;

    @BeforeEach
    void setUp() {
        config = new EmbeddingConfigProperties();
        config.setModel("BAAI/bge-small-en-v1.5");
        config.setDimension(384);
        config.setBatchSize(32);

        when(embeddingProvider.getModelName()).thenReturn("BAAI/bge-small-en-v1.5");
        when(embeddingProvider.getDimension()).thenReturn(384);

        service = new EvidenceEmbeddingService(
                embeddingRepository,
                chunkRepository,
                documentRepository,
                embeddingProvider,
                config,
                jdbcTemplate
        );
    }

    private EvidenceChunk createChunk(String id, String docId, int seq, String text) {
        EvidenceChunk c = new EvidenceChunk();
        c.setId(id);
        c.setExtractedDocumentId(docId);
        c.setSequenceNumber(seq);
        c.setTextContent(text);
        return c;
    }

    @Test
    @DisplayName("Enqueueing embeddings marks document as PROCESSING and creates PENDING chunk records")
    void testEnqueueEmbeddings() {
        String docId = "doc-101";
        ExtractedDocument doc = new ExtractedDocument();
        doc.setId(docId);
        doc.setEmbeddingStatus(EmbeddingStatus.PENDING);

        EvidenceChunk c1 = createChunk("chk-1", docId, 1, "First paragraph");
        EvidenceChunk c2 = createChunk("chk-2", docId, 2, "Second paragraph");
        List<EvidenceChunk> chunks = List.of(c1, c2);

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(chunkRepository.findByExtractedDocumentIdOrderBySequenceNumberAsc(docId)).thenReturn(chunks);
        when(embeddingRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When provider is available
        when(embeddingProvider.isAvailable()).thenReturn(true);
        float[] v1 = new float[384];
        Arrays.fill(v1, 0.1f);
        float[] v2 = new float[384];
        Arrays.fill(v2, 0.2f);
        when(embeddingProvider.generateEmbeddings(anyList())).thenReturn(List.of(v1, v2));

        // Enqueue
        List<EvidenceChunkEmbedding> result = service.enqueueEmbeddingsForDocument(doc, chunks);

        assertEquals(2, result.size());
        verify(documentRepository, atLeastOnce()).save(doc);
    }

    @Test
    @DisplayName("When embedding provider is unconfigured, embeddings are honestly marked as FAILED with error message")
    void testProviderUnavailableMarksFailed() {
        String docId = "doc-102";
        ExtractedDocument doc = new ExtractedDocument();
        doc.setId(docId);

        EvidenceChunk c1 = createChunk("chk-10", docId, 1, "Sample chunk text");
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(chunkRepository.findByExtractedDocumentIdOrderBySequenceNumberAsc(docId)).thenReturn(List.of(c1));

        EvidenceChunkEmbedding pendingEmb = new EvidenceChunkEmbedding(
                "emb-1", "chk-10", "BAAI/bge-small-en-v1.5", 384, null,
                EmbeddingStatus.PENDING, null, 0
        );
        when(embeddingRepository.findByChunkIdInAndModelName(any(), eq("BAAI/bge-small-en-v1.5")))
                .thenReturn(List.of(pendingEmb));

        when(embeddingProvider.isAvailable()).thenReturn(false);

        service.processPendingEmbeddingsForDocument(docId);

        assertEquals(EmbeddingStatus.FAILED, pendingEmb.getStatus());
        assertTrue(pendingEmb.getErrorMessage().contains("not available or unconfigured"));
        assertEquals(EmbeddingStatus.FAILED, doc.getEmbeddingStatus());
    }

    @Test
    @DisplayName("getDocumentEmbeddingStatus accurately reports counts and status")
    void testGetDocumentEmbeddingStatus() {
        String docId = "doc-103";
        ExtractedDocument doc = new ExtractedDocument();
        doc.setId(docId);
        doc.setEmbeddingStatus(EmbeddingStatus.PROCESSING);

        EvidenceChunk c1 = createChunk("c1", docId, 1, "t1");
        EvidenceChunk c2 = createChunk("c2", docId, 2, "t2");
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(chunkRepository.findByExtractedDocumentIdOrderBySequenceNumberAsc(docId)).thenReturn(List.of(c1, c2));

        EvidenceChunkEmbedding e1 = new EvidenceChunkEmbedding("emb-1", "c1", "BAAI/bge-small-en-v1.5", 384, "[0.1]", EmbeddingStatus.COMPLETED, null, 0);
        EvidenceChunkEmbedding e2 = new EvidenceChunkEmbedding("emb-2", "c2", "BAAI/bge-small-en-v1.5", 384, null, EmbeddingStatus.PENDING, null, 0);
        when(embeddingRepository.findByChunkIdInAndModelName(any(), eq("BAAI/bge-small-en-v1.5"))).thenReturn(List.of(e1, e2));

        EmbeddingStatusSummaryDto summary = service.getDocumentEmbeddingStatus(docId);

        assertEquals(docId, summary.documentId());
        assertEquals(2, summary.totalChunks());
        assertEquals(1, summary.completedChunks());
        assertEquals(1, summary.pendingChunks());
        assertEquals(0, summary.failedChunks());
        assertEquals(EmbeddingStatus.PROCESSING, summary.overallStatus());
    }
}
