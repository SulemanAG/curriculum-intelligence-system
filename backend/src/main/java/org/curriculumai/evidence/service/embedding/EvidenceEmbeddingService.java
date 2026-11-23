package org.curriculumai.evidence.service.embedding;

import org.curriculumai.common.exceptions.ResourceNotFoundException;
import org.curriculumai.evidence.domain.EmbeddingStatus;
import org.curriculumai.evidence.domain.EvidenceChunk;
import org.curriculumai.evidence.domain.EvidenceChunkEmbedding;
import org.curriculumai.evidence.domain.ExtractedDocument;
import org.curriculumai.evidence.dto.EmbeddingStatusSummaryDto;
import org.curriculumai.evidence.repository.EvidenceChunkEmbeddingRepository;
import org.curriculumai.evidence.repository.EvidenceChunkRepository;
import org.curriculumai.evidence.repository.ExtractedDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EvidenceEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EvidenceEmbeddingService.class);

    private final EvidenceChunkEmbeddingRepository embeddingRepository;
    private final EvidenceChunkRepository chunkRepository;
    private final ExtractedDocumentRepository documentRepository;
    private final EmbeddingProvider embeddingProvider;
    private final EmbeddingConfigProperties config;
    private final JdbcTemplate jdbcTemplate;

    public EvidenceEmbeddingService(EvidenceChunkEmbeddingRepository embeddingRepository,
                                    EvidenceChunkRepository chunkRepository,
                                    ExtractedDocumentRepository documentRepository,
                                    EmbeddingProvider embeddingProvider,
                                    EmbeddingConfigProperties config,
                                    JdbcTemplate jdbcTemplate) {
        this.embeddingRepository = embeddingRepository;
        this.chunkRepository = chunkRepository;
        this.documentRepository = documentRepository;
        this.embeddingProvider = embeddingProvider;
        this.config = config;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Enqueue embedding generation for all chunks belonging to an extracted document.
     */
    @Transactional
    public List<EvidenceChunkEmbedding> enqueueEmbeddingsForDocument(ExtractedDocument document, List<EvidenceChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            document.setEmbeddingStatus(EmbeddingStatus.COMPLETED);
            documentRepository.save(document);
            return Collections.emptyList();
        }

        document.setEmbeddingStatus(EmbeddingStatus.PROCESSING);
        documentRepository.save(document);

        List<EvidenceChunkEmbedding> embeddingsToSave = new ArrayList<>();
        String modelName = embeddingProvider.getModelName();
        int dimension = embeddingProvider.getDimension();

        for (EvidenceChunk chunk : chunks) {
            Optional<EvidenceChunkEmbedding> existing = embeddingRepository.findByChunkIdAndModelName(chunk.getId(), modelName);
            if (existing.isPresent()) {
                EvidenceChunkEmbedding emb = existing.get();
                if (emb.getStatus() != EmbeddingStatus.COMPLETED) {
                    emb.setStatus(EmbeddingStatus.PENDING);
                    emb.setErrorMessage(null);
                    embeddingsToSave.add(emb);
                }
            } else {
                String embId = "emb-" + UUID.randomUUID();
                EvidenceChunkEmbedding emb = new EvidenceChunkEmbedding(
                        embId,
                        chunk.getId(),
                        modelName,
                        dimension,
                        null,
                        EmbeddingStatus.PENDING,
                        null,
                        0
                );
                embeddingsToSave.add(emb);
            }
        }

        List<EvidenceChunkEmbedding> saved = embeddingRepository.saveAll(embeddingsToSave);
        log.info("Enqueued {} embeddings for document '{}' with model '{}'", saved.size(), document.getId(), modelName);

        // Process immediately (synchronous or triggered)
        processPendingEmbeddingsForDocument(document.getId());

        return saved;
    }

    /**
     * Process pending embeddings for a specific document.
     */
    @Transactional
    public void processPendingEmbeddingsForDocument(String documentId) {
        ExtractedDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("ExtractedDocument not found: " + documentId));

        List<EvidenceChunk> chunks = chunkRepository.findByExtractedDocumentIdOrderBySequenceNumberAsc(documentId);
        if (chunks.isEmpty()) {
            document.setEmbeddingStatus(EmbeddingStatus.COMPLETED);
            documentRepository.save(document);
            return;
        }

        List<String> chunkIds = chunks.stream().map(EvidenceChunk::getId).toList();
        Map<String, EvidenceChunk> chunkMap = chunks.stream().collect(Collectors.toMap(EvidenceChunk::getId, c -> c));

        List<EvidenceChunkEmbedding> pendingList = embeddingRepository.findByChunkIdInAndModelName(chunkIds, embeddingProvider.getModelName())
                .stream()
                .filter(e -> e.getStatus() == EmbeddingStatus.PENDING || e.getStatus() == EmbeddingStatus.PROCESSING)
                .toList();

        if (pendingList.isEmpty()) {
            updateDocumentOverallStatus(document, chunkIds);
            return;
        }

        if (!embeddingProvider.isAvailable()) {
            log.warn("Embedding provider unavailable: marking {} embeddings as FAILED for document '{}'", pendingList.size(), documentId);
            for (EvidenceChunkEmbedding emb : pendingList) {
                emb.setStatus(EmbeddingStatus.FAILED);
                emb.setErrorMessage("Embedding provider is not available or unconfigured. (EMBEDDING_API_URL is empty)");
                emb.setRetryCount(emb.getRetryCount() + 1);
            }
            embeddingRepository.saveAll(pendingList);
            document.setEmbeddingStatus(EmbeddingStatus.FAILED);
            documentRepository.save(document);
            return;
        }

        int batchSize = Math.max(1, config.getBatchSize());
        for (int i = 0; i < pendingList.size(); i += batchSize) {
            List<EvidenceChunkEmbedding> batch = pendingList.subList(i, Math.min(i + batchSize, pendingList.size()));
            List<String> texts = batch.stream()
                    .map(e -> chunkMap.get(e.getChunkId()).getTextContent())
                    .toList();

            try {
                List<float[]> generated = embeddingProvider.generateEmbeddings(texts);
                for (int j = 0; j < batch.size(); j++) {
                    EvidenceChunkEmbedding emb = batch.get(j);
                    float[] vector = generated.get(j);

                    EmbeddingValidationUtils.validateVector(vector, config.getDimension());

                    emb.setEmbeddingJson(EmbeddingValidationUtils.toJson(vector));
                    emb.setStatus(EmbeddingStatus.COMPLETED);
                    emb.setErrorMessage(null);
                    embeddingRepository.save(emb);

                    // Sync to PostgreSQL native real[] column if supported
                    syncNativeVectorColumn(emb.getId(), vector);
                }
            } catch (Exception e) {
                log.error("Failed to generate embeddings batch for document '{}': {}", documentId, e.getMessage());
                for (EvidenceChunkEmbedding emb : batch) {
                    emb.setStatus(EmbeddingStatus.FAILED);
                    emb.setErrorMessage("Embedding generation failed: " + e.getMessage());
                    emb.setRetryCount(emb.getRetryCount() + 1);
                }
                embeddingRepository.saveAll(batch);
            }
        }

        updateDocumentOverallStatus(document, chunkIds);
    }

    private void syncNativeVectorColumn(String embeddingId, float[] vector) {
        try {
            String pgArray = EmbeddingValidationUtils.toPgArrayString(vector);
            jdbcTemplate.update(
                    "UPDATE evidence_chunk_embeddings SET embedding = cast(? as real[]) WHERE id = ?",
                    pgArray, embeddingId
            );
            try {
                jdbcTemplate.update(
                        "UPDATE evidence_chunk_embeddings SET embedding_vec = cast(? as vector) WHERE id = ?",
                        pgArray, embeddingId
                );
            } catch (Exception ignored) {
                // embedding_vec column not present or pgvector not loaded
            }
        } catch (Exception ignored) {
            // Handled safely in H2 or if vector column is absent
        }
    }

    private void updateDocumentOverallStatus(ExtractedDocument document, List<String> chunkIds) {
        List<EvidenceChunkEmbedding> allEmbeddings = embeddingRepository.findByChunkIdInAndModelName(chunkIds, embeddingProvider.getModelName());
        long completed = allEmbeddings.stream().filter(e -> e.getStatus() == EmbeddingStatus.COMPLETED).count();
        long failed = allEmbeddings.stream().filter(e -> e.getStatus() == EmbeddingStatus.FAILED).count();
        long pending = allEmbeddings.stream().filter(e -> e.getStatus() == EmbeddingStatus.PENDING || e.getStatus() == EmbeddingStatus.PROCESSING).count();

        if (pending > 0) {
            document.setEmbeddingStatus(EmbeddingStatus.PROCESSING);
        } else if (failed > 0) {
            document.setEmbeddingStatus(EmbeddingStatus.FAILED);
        } else if (completed == chunkIds.size()) {
            document.setEmbeddingStatus(EmbeddingStatus.COMPLETED);
        } else {
            document.setEmbeddingStatus(EmbeddingStatus.PENDING);
        }

        documentRepository.save(document);
    }

    /**
     * Inspect embedding status summary for a document.
     */
    public EmbeddingStatusSummaryDto getDocumentEmbeddingStatus(String documentId) {
        ExtractedDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("ExtractedDocument not found: " + documentId));

        List<EvidenceChunk> chunks = chunkRepository.findByExtractedDocumentIdOrderBySequenceNumberAsc(documentId);
        List<String> chunkIds = chunks.stream().map(EvidenceChunk::getId).toList();

        List<EvidenceChunkEmbedding> embeddings = embeddingRepository.findByChunkIdInAndModelName(chunkIds, embeddingProvider.getModelName());

        int total = chunks.size();
        int completed = (int) embeddings.stream().filter(e -> e.getStatus() == EmbeddingStatus.COMPLETED).count();
        int failed = (int) embeddings.stream().filter(e -> e.getStatus() == EmbeddingStatus.FAILED).count();
        int pending = total - (completed + failed);

        String firstErrorMessage = embeddings.stream()
                .filter(e -> e.getStatus() == EmbeddingStatus.FAILED && e.getErrorMessage() != null)
                .map(EvidenceChunkEmbedding::getErrorMessage)
                .findFirst()
                .orElse(null);

        return new EmbeddingStatusSummaryDto(
                documentId,
                embeddingProvider.getModelName(),
                embeddingProvider.getDimension(),
                total,
                completed,
                Math.max(0, pending),
                failed,
                document.getEmbeddingStatus(),
                firstErrorMessage
        );
    }

    /**
     * Retry failed embeddings for a document.
     */
    @Transactional
    public EmbeddingStatusSummaryDto retryFailedEmbeddings(String documentId) {
        ExtractedDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("ExtractedDocument not found: " + documentId));

        List<EvidenceChunk> chunks = chunkRepository.findByExtractedDocumentIdOrderBySequenceNumberAsc(documentId);
        List<String> chunkIds = chunks.stream().map(EvidenceChunk::getId).toList();

        List<EvidenceChunkEmbedding> embeddings = embeddingRepository.findByChunkIdInAndModelName(chunkIds, embeddingProvider.getModelName());
        List<EvidenceChunkEmbedding> toRetry = embeddings.stream()
                .filter(e -> e.getStatus() == EmbeddingStatus.FAILED)
                .toList();

        for (EvidenceChunkEmbedding emb : toRetry) {
            emb.setStatus(EmbeddingStatus.PENDING);
            emb.setErrorMessage(null);
        }
        embeddingRepository.saveAll(toRetry);

        document.setEmbeddingStatus(EmbeddingStatus.PROCESSING);
        documentRepository.save(document);

        processPendingEmbeddingsForDocument(documentId);

        return getDocumentEmbeddingStatus(documentId);
    }
}

