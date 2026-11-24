package org.curriculumai.evidence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.common.exceptions.ResourceNotFoundException;
import org.curriculumai.evidence.domain.*;
import org.curriculumai.evidence.dto.*;
import org.curriculumai.evidence.repository.*;
import org.curriculumai.evidence.service.embedding.EvidenceEmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Service
public class EvidenceIngestionService {

    private static final Logger log = LoggerFactory.getLogger(EvidenceIngestionService.class);

    private final ResearchSourceRepository sourceRepository;
    private final SourceRetrievalRepository retrievalRepository;
    private final SourceVersionRepository versionRepository;
    private final ExtractedDocumentRepository documentRepository;
    private final EvidenceChunkRepository chunkRepository;
    private final DocumentStorageService storageService;
    private final SafeUrlRetrievalService retrievalService;
    private final DocumentExtractionService extractionService;
    private final EvidenceEmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    public EvidenceIngestionService(ResearchSourceRepository sourceRepository,
                                   SourceRetrievalRepository retrievalRepository,
                                   SourceVersionRepository versionRepository,
                                   ExtractedDocumentRepository documentRepository,
                                   EvidenceChunkRepository chunkRepository,
                                   DocumentStorageService storageService,
                                   SafeUrlRetrievalService retrievalService,
                                   DocumentExtractionService extractionService,
                                   EvidenceEmbeddingService embeddingService,
                                   ObjectMapper objectMapper) {
        this.sourceRepository = sourceRepository;
        this.retrievalRepository = retrievalRepository;
        this.versionRepository = versionRepository;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.storageService = storageService;
        this.retrievalService = retrievalService;
        this.extractionService = extractionService;
        this.embeddingService = embeddingService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ResearchSourceDto registerSource(RegisterSourceRequest req, String userId) {
        String canonicalUrl = req.canonicalUrl().trim();
        Optional<ResearchSource> existing = sourceRepository.findByCanonicalUrl(canonicalUrl);
        if (existing.isPresent()) {
            return mapSourceToDto(existing.get());
        }

        String domain = null;
        try {
            URI uri = URI.create(canonicalUrl);
            domain = uri.getHost();
        } catch (Exception ignored) {
        }

        String sourceId = "src-" + UUID.randomUUID();
        ResearchSource source = new ResearchSource(
                sourceId,
                canonicalUrl,
                req.sourceType(),
                req.publisher(),
                req.title(),
                domain,
                req.sourceClassification(),
                req.institutionId(),
                req.departmentId(),
                req.programmeId(),
                userId
        );

        ResearchSource saved = sourceRepository.save(source);
        log.info("Registered research source '{}' (type: {}) by user '{}'", saved.getId(), saved.getSourceType(), userId);
        return mapSourceToDto(saved);
    }

    public Page<ResearchSourceDto> listSources(Pageable pageable) {
        return sourceRepository.findAll(pageable).map(this::mapSourceToDto);
    }

    public ResearchSourceDto getSource(String sourceId) {
        ResearchSource source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("ResearchSource not found: " + sourceId));
        return mapSourceToDto(source);
    }

    @Transactional
    public SourceRetrievalDto retrieveSourceUrl(String sourceId, String userId) {
        ResearchSource source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("ResearchSource not found: " + sourceId));

        SafeUrlRetrievalService.RetrievalResult result = retrievalService.retrieveUrl(source.getCanonicalUrl());

        String retrievalId = "ret-" + UUID.randomUUID();
        SourceRetrieval retrieval = new SourceRetrieval(
                retrievalId,
                sourceId,
                result.httpStatus(),
                result.finalUrl(),
                result.contentType(),
                result.outcome(),
                result.failureReason(),
                result.contentHash(),
                result.etag(),
                result.lastModified(),
                result.elapsedMillis(),
                userId
        );

        retrievalRepository.save(retrieval);
        log.info("Recorded retrieval attempt '{}' for source '{}', outcome: {}", retrievalId, sourceId, result.outcome());

        // If retrieved successfully and body is present, process version and extraction
        if (result.outcome() == RetrievalOutcome.RETRIEVED && result.contentBytes() != null && result.contentBytes().length > 0) {
            processSourceBytes(source, retrievalId, result.contentBytes(), result.contentType(),
                    extractFilenameFromUrl(result.finalUrl()), null, result.contentHash());
        }

        return mapRetrievalToDto(retrieval);
    }

    @Transactional
    public SourceVersionDto ingestUploadedDocument(String sourceId, MultipartFile file, String publicationDate, String userId) throws IOException {
        ResearchSource source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("ResearchSource not found: " + sourceId));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot ingest empty uploaded file.");
        }

        byte[] bytes = file.getBytes();
        String contentHash = computeSha256(bytes);
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        SourceVersion version = processSourceBytes(source, null, bytes, contentType, filename, publicationDate, contentHash);
        log.info("Ingested uploaded document as version '{}' for source '{}' (hash: {})", version.getId(), sourceId, contentHash);
        return mapVersionToDto(version);
    }

    @Transactional
    public SourceVersionDto ingestDirectText(String sourceId, String textContent, String filename, String publicationDate, String userId) {
        ResearchSource source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("ResearchSource not found: " + sourceId));

        if (textContent == null || textContent.isBlank()) {
            throw new IllegalArgumentException("Cannot ingest empty text content.");
        }

        byte[] bytes = textContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String contentHash = computeSha256(bytes);
        String actualFilename = (filename != null && !filename.isBlank()) ? filename : "research_document.txt";

        SourceVersion version = processSourceBytes(source, null, bytes, "text/plain", actualFilename, publicationDate, contentHash);
        log.info("Ingested direct text as version '{}' for source '{}' (hash: {})", version.getId(), sourceId, contentHash);
        return mapVersionToDto(version);
    }

    public List<String> getChunkIdsForVersion(String versionId) {
        return documentRepository.findBySourceVersionId(versionId)
                .map(doc -> chunkRepository.findByExtractedDocumentIdOrderBySequenceNumberAsc(doc.getId())
                        .stream().map(EvidenceChunk::getId).toList())
                .orElse(Collections.emptyList());
    }

    /**
     * Common processing pipeline: deduplicate content hash -> store raw bytes -> create version -> extract document -> create chunks.
     */
    private SourceVersion processSourceBytes(ResearchSource source, String retrievalId, byte[] bytes,
                                            String contentType, String filename, String publicationDate,
                                            String contentHash) {
        // 1. Content Hash Deduplication
        Optional<SourceVersion> existingVersion = versionRepository.findBySourceIdAndContentHash(source.getId(), contentHash);
        if (existingVersion.isPresent()) {
            log.info("Content hash '{}' already ingested for source '{}'. Skipping duplicate version creation.", contentHash, source.getId());
            return existingVersion.get();
        }

        // 2. Persistent Storage
        String storageRef;
        try {
            storageRef = storageService.storeDocument(new ByteArrayInputStream(bytes), filename, contentHash);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store document file to disk: " + e.getMessage(), e);
        }

        // 3. Create Source Version
        long versionCount = versionRepository.countBySourceId(source.getId()) + 1;
        String versionIdentifier = "v" + versionCount;
        String versionId = "srcv-" + UUID.randomUUID();

        SourceVersion version = new SourceVersion(
                versionId,
                source.getId(),
                retrievalId,
                contentHash,
                versionIdentifier,
                publicationDate,
                filename,
                contentType != null ? contentType : "application/octet-stream",
                (long) bytes.length,
                storageRef
        );
        SourceVersion savedVersion = versionRepository.save(version);

        // 4. Extract Document
        DocumentExtractionService.ExtractionResult extResult = extractionService.extract(bytes, contentType, filename);

        String docId = "doc-" + UUID.randomUUID();
        ExtractedDocument document = new ExtractedDocument(
                docId,
                savedVersion.getId(),
                extResult.status(),
                extResult.extractorName(),
                extResult.extractorVersion(),
                extResult.documentTitle(),
                extResult.detectedLanguage(),
                extResult.structuredMetadataJson(),
                extResult.totalPages(),
                extResult.totalParagraphs(),
                extResult.totalTables(),
                extResult.totalCharacters(),
                extResult.errorDetails()
        );
        extractedDocumentRepositorySave(document);

        // 5. Create Evidence Chunks with Citation Provenance
        if (extResult.status() == ExtractionStatus.COMPLETED && !extResult.chunks().isEmpty()) {
            List<EvidenceChunk> chunksToSave = new ArrayList<>();
            for (DocumentExtractionService.ExtractedChunk c : extResult.chunks()) {
                String chunkId = "chk-" + UUID.randomUUID();
                String stableChunkId = savedVersion.getId() + "-c" + c.sequenceNumber();
                String chunkHash = computeSha256(c.textContent().getBytes());

                // Build provenance citation metadata
                ProvenanceDto provenance = new ProvenanceDto(
                        source.getId(),
                        source.getCanonicalUrl(),
                        source.getSourceType(),
                        source.getPublisher(),
                        source.getTitle(),
                        savedVersion.getOriginalPublicationDate(),
                        savedVersion.getRetrievedTimestamp().toString(),
                        savedVersion.getVersionIdentifier(),
                        contentHash,
                        document.getDocumentTitle(),
                        c.pageNumber(),
                        c.sectionHeading()
                );

                String citationJson;
                try {
                    citationJson = objectMapper.writeValueAsString(provenance);
                } catch (Exception e) {
                    citationJson = "{}";
                }

                EvidenceChunk chunk = new EvidenceChunk(
                        chunkId,
                        document.getId(),
                        c.sequenceNumber(),
                        stableChunkId,
                        c.textContent(),
                        c.pageNumber(),
                        c.sectionHeading(),
                        c.charOffsetStart(),
                        c.charOffsetEnd(),
                        "PARAGRAPH_AND_TABLE",
                        "1.0",
                        chunkHash,
                        citationJson
                );
                chunksToSave.add(chunk);
            }
            chunkRepository.saveAll(chunksToSave);
            log.info("Saved {} evidence chunks for extracted document '{}'", chunksToSave.size(), docId);
            embeddingService.enqueueEmbeddingsForDocument(document, chunksToSave);
        } else {
            document.setEmbeddingStatus(EmbeddingStatus.COMPLETED);
            documentRepository.save(document);
        }

        return savedVersion;
    }

    private void extractedDocumentRepositorySave(ExtractedDocument document) {
        documentRepository.save(document);
    }

    public Page<SourceRetrievalDto> listRetrievals(String sourceId, Pageable pageable) {
        return retrievalRepository.findBySourceId(sourceId, pageable).map(this::mapRetrievalToDto);
    }

    public Page<SourceVersionDto> listVersions(String sourceId, Pageable pageable) {
        return versionRepository.findBySourceId(sourceId, pageable).map(this::mapVersionToDto);
    }

    public ExtractedDocumentDto getExtractedDocumentByVersionId(String versionId) {
        ExtractedDocument doc = documentRepository.findBySourceVersionId(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Extracted document not found for version: " + versionId));
        return mapDocumentToDto(doc);
    }

    public EvidenceChunkDto getChunk(String chunkId) {
        EvidenceChunk chunk = chunkRepository.findById(chunkId)
                .orElseThrow(() -> new ResourceNotFoundException("EvidenceChunk not found: " + chunkId));
        return mapChunkToDto(chunk);
    }

    public EvidenceChunkDto mapChunkToDto(EvidenceChunk c) {
        ProvenanceDto prov = null;
        if (c.getCitationMetadataJson() != null) {
            try {
                prov = objectMapper.readValue(c.getCitationMetadataJson(), ProvenanceDto.class);
            } catch (Exception ignored) {
            }
        }

        return new EvidenceChunkDto(
                c.getId(),
                c.getExtractedDocumentId(),
                c.getSequenceNumber(),
                c.getStableChunkId(),
                c.getTextContent(),
                c.getPageNumber(),
                c.getSectionHeading(),
                c.getChunkingStrategy(),
                c.getContentHash(),
                prov
        );
    }

    private String extractFilenameFromUrl(String url) {
        if (url == null) return "source.bin";
        try {
            URI uri = URI.create(url);
            String path = uri.getPath();
            if (path != null && path.contains("/")) {
                String sub = path.substring(path.lastIndexOf('/') + 1);
                if (!sub.isBlank()) return sub;
            }
        } catch (Exception ignored) {
        }
        return "source.bin";
    }

    private String computeSha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private ResearchSourceDto mapSourceToDto(ResearchSource s) {
        return new ResearchSourceDto(
                s.getId(),
                s.getCanonicalUrl(),
                s.getSourceType(),
                s.getPublisher(),
                s.getTitle(),
                s.getDomain(),
                s.getSourceClassification(),
                s.getInstitutionId(),
                s.getDepartmentId(),
                s.getProgrammeId(),
                s.getActive(),
                s.getCreatedBy(),
                s.getCreatedAt().toString(),
                s.getUpdatedAt().toString()
        );
    }

    private SourceRetrievalDto mapRetrievalToDto(SourceRetrieval r) {
        return new SourceRetrievalDto(
                r.getId(),
                r.getSourceId(),
                r.getRetrievalTimestamp().toString(),
                r.getHttpStatus(),
                r.getFinalUrl(),
                r.getContentType(),
                r.getRetrievalOutcome(),
                r.getFailureReason(),
                r.getContentHash(),
                r.getEtag(),
                r.getElapsedMillis(),
                r.getInitiatedBy()
        );
    }

    private SourceVersionDto mapVersionToDto(SourceVersion v) {
        return new SourceVersionDto(
                v.getId(),
                v.getSourceId(),
                v.getRetrievalId(),
                v.getContentHash(),
                v.getVersionIdentifier(),
                v.getOriginalPublicationDate(),
                v.getRetrievedTimestamp().toString(),
                v.getOriginalFilename(),
                v.getMimeType(),
                v.getFileSizeBytes(),
                v.getStorageReference()
        );
    }

    private ExtractedDocumentDto mapDocumentToDto(ExtractedDocument d) {
        return new ExtractedDocumentDto(
                d.getId(),
                d.getSourceVersionId(),
                d.getExtractionStatus(),
                d.getExtractorName(),
                d.getExtractorVersion(),
                d.getExtractionTimestamp().toString(),
                d.getDocumentTitle(),
                d.getDetectedLanguage(),
                d.getTotalPages(),
                d.getTotalParagraphs(),
                d.getTotalTables(),
                d.getTotalCharacters(),
                d.getErrorDetails()
        );
    }
}

