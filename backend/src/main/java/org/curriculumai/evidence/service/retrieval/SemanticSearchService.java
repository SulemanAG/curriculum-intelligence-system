package org.curriculumai.evidence.service.retrieval;

import org.curriculumai.evidence.domain.*;
import org.curriculumai.evidence.repository.*;
import org.curriculumai.evidence.service.embedding.EmbeddingConfigProperties;
import org.curriculumai.evidence.service.embedding.EmbeddingProvider;
import org.curriculumai.evidence.service.embedding.EmbeddingValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SemanticSearchService {

    private static final Logger log = LoggerFactory.getLogger(SemanticSearchService.class);

    private final EvidenceChunkEmbeddingRepository embeddingRepository;
    private final EvidenceChunkRepository chunkRepository;
    private final ExtractedDocumentRepository documentRepository;
    private final SourceVersionRepository versionRepository;
    private final ResearchSourceRepository sourceRepository;
    private final EmbeddingProvider embeddingProvider;
    private final EmbeddingConfigProperties config;
    private final JdbcTemplate jdbcTemplate;

    public record RankedResult(EvidenceChunk chunk, int rank, double score, double distance) {}

    public SemanticSearchService(EvidenceChunkEmbeddingRepository embeddingRepository,
                                 EvidenceChunkRepository chunkRepository,
                                 ExtractedDocumentRepository documentRepository,
                                 SourceVersionRepository versionRepository,
                                 ResearchSourceRepository sourceRepository,
                                 EmbeddingProvider embeddingProvider,
                                 EmbeddingConfigProperties config,
                                 JdbcTemplate jdbcTemplate) {
        this.embeddingRepository = embeddingRepository;
        this.chunkRepository = chunkRepository;
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
        this.sourceRepository = sourceRepository;
        this.embeddingProvider = embeddingProvider;
        this.config = config;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Executes semantic vector search against candidate chunks matching metadata filters.
     */
    public List<RankedResult> searchSemantic(String query, SourceType sourceType, String publisher,
                                             String institutionId, String departmentId, int limit) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        if (!embeddingProvider.isAvailable()) {
            log.warn("Semantic search skipped: embedding provider is not available or unconfigured");
            return Collections.emptyList();
        }

        float[] queryVector;
        try {
            queryVector = embeddingProvider.generateEmbedding(query.trim());
            EmbeddingValidationUtils.validateVector(queryVector, config.getDimension());
        } catch (Exception e) {
            log.error("Failed to generate query embedding for semantic search: {}", e.getMessage());
            return Collections.emptyList();
        }

        // Get completed chunk embeddings for the active model
        String modelName = embeddingProvider.getModelName();
        List<EvidenceChunkEmbedding> completedEmbeddings = embeddingRepository.findByStatus(EmbeddingStatus.COMPLETED)
                .stream()
                .filter(e -> modelName.equals(e.getModelName()))
                .filter(e -> e.getEmbeddingJson() != null && !e.getEmbeddingJson().isBlank())
                .toList();

        if (completedEmbeddings.isEmpty()) {
            return Collections.emptyList();
        }

        // Load chunks and apply metadata filters
        List<RankedCandidate> scoredCandidates = new ArrayList<>();
        for (EvidenceChunkEmbedding emb : completedEmbeddings) {
            EvidenceChunk chunk = chunkRepository.findById(emb.getChunkId()).orElse(null);
            if (chunk == null) continue;

            if (!passesMetadataFilters(chunk, sourceType, publisher, institutionId, departmentId)) {
                continue;
            }

            float[] chunkVector = EmbeddingValidationUtils.fromJson(emb.getEmbeddingJson());
            if (chunkVector == null || chunkVector.length != config.getDimension()) {
                continue;
            }

            double similarity = EmbeddingValidationUtils.cosineSimilarity(queryVector, chunkVector);
            double distance = 1.0 - similarity;
            scoredCandidates.add(new RankedCandidate(chunk, similarity, distance));
        }

        // Sort descending by similarity, ascending by sequence number
        scoredCandidates.sort((a, b) -> {
            int cmp = Double.compare(b.similarity, a.similarity);
            if (cmp != 0) return cmp;
            return Integer.compare(a.chunk.getSequenceNumber(), b.chunk.getSequenceNumber());
        });

        List<RankedResult> results = new ArrayList<>();
        int rank = 1;
        for (RankedCandidate c : scoredCandidates) {
            results.add(new RankedResult(c.chunk, rank++, c.similarity, c.distance));
            if (results.size() >= limit) break;
        }

        return results;
    }

    private boolean passesMetadataFilters(EvidenceChunk chunk, SourceType sourceType, String publisher,
                                          String institutionId, String departmentId) {
        if (sourceType == null && publisher == null && institutionId == null && departmentId == null) {
            return true;
        }

        ExtractedDocument doc = documentRepository.findById(chunk.getExtractedDocumentId()).orElse(null);
        if (doc == null) return false;

        SourceVersion version = versionRepository.findById(doc.getSourceVersionId()).orElse(null);
        if (version == null) return false;

        ResearchSource source = sourceRepository.findById(version.getSourceId()).orElse(null);
        if (source == null) return false;

        if (sourceType != null && source.getSourceType() != sourceType) {
            return false;
        }
        if (publisher != null && !publisher.isBlank()) {
            if (source.getPublisher() == null || !source.getPublisher().toLowerCase().contains(publisher.toLowerCase())) {
                return false;
            }
        }
        if (institutionId != null && !institutionId.isBlank()) {
            if (source.getInstitutionId() == null || !source.getInstitutionId().equalsIgnoreCase(institutionId)) {
                return false;
            }
        }
        if (departmentId != null && !departmentId.isBlank()) {
            if (source.getDepartmentId() == null || !source.getDepartmentId().equalsIgnoreCase(departmentId)) {
                return false;
            }
        }

        return true;
    }

    private record RankedCandidate(EvidenceChunk chunk, double similarity, double distance) {}
}

