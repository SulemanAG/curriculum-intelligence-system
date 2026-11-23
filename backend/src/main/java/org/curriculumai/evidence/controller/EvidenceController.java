package org.curriculumai.evidence.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.curriculumai.common.ApiResponse;
import org.curriculumai.evidence.domain.SourceType;
import org.curriculumai.evidence.dto.*;
import org.curriculumai.evidence.service.EvidenceIngestionService;
import org.curriculumai.evidence.service.EvidenceSearchService;
import org.curriculumai.evidence.service.embedding.EvidenceEmbeddingService;
import org.curriculumai.evidence.service.retrieval.HybridRetrievalService;
import org.curriculumai.security.UserIdentityResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/evidence")
@Tag(name = "Evidence Repository", description = "Provenance-first research source ingestion, retrieval, and evidence extraction")
public class EvidenceController {

    private final EvidenceIngestionService ingestionService;
    private final EvidenceSearchService searchService;
    private final HybridRetrievalService hybridRetrievalService;
    private final EvidenceEmbeddingService embeddingService;
    private final UserIdentityResolver identityResolver;

    public EvidenceController(EvidenceIngestionService ingestionService,
                              EvidenceSearchService searchService,
                              HybridRetrievalService hybridRetrievalService,
                              EvidenceEmbeddingService embeddingService,
                              UserIdentityResolver identityResolver) {
        this.ingestionService = ingestionService;
        this.searchService = searchService;
        this.hybridRetrievalService = hybridRetrievalService;
        this.embeddingService = embeddingService;
        this.identityResolver = identityResolver;
    }

    @PostMapping("/sources")
    @Operation(summary = "Register a research source identity with canonical URL")
    public ResponseEntity<ApiResponse<ResearchSourceDto>> registerSource(
            @Valid @RequestBody RegisterSourceRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String devHeader) {
        String userId = identityResolver.resolveUserId(devHeader);
        ResearchSourceDto source = ingestionService.registerSource(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(source, "Research source registered successfully."));
    }

    @GetMapping("/sources")
    @Operation(summary = "List registered research sources with pagination")
    public ResponseEntity<ApiResponse<Page<ResearchSourceDto>>> listSources(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(ingestionService.listSources(pageable)));
    }

    @GetMapping("/sources/{id}")
    @Operation(summary = "Get a registered research source by ID")
    public ResponseEntity<ApiResponse<ResearchSourceDto>> getSource(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(ingestionService.getSource(id)));
    }

    @PostMapping("/sources/{id}/retrieve")
    @Operation(summary = "Initiate safe URL retrieval and document ingestion for a permitted source")
    public ResponseEntity<ApiResponse<SourceRetrievalDto>> retrieveSource(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String devHeader) {
        String userId = identityResolver.resolveUserId(devHeader);
        SourceRetrievalDto retrieval = ingestionService.retrieveSourceUrl(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(retrieval, "URL retrieval completed. Outcome: " + retrieval.outcome()));
    }

    @PostMapping(value = "/sources/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and ingest a document file for a registered source")
    public ResponseEntity<ApiResponse<SourceVersionDto>> uploadDocument(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "publicationDate", required = false) String publicationDate,
            @RequestHeader(value = "X-User-Id", required = false) String devHeader) throws IOException {
        String userId = identityResolver.resolveUserId(devHeader);
        SourceVersionDto version = ingestionService.ingestUploadedDocument(id, file, publicationDate, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(version, "Document ingested successfully as version " + version.versionIdentifier()));
    }

    @GetMapping("/sources/{id}/retrievals")
    @Operation(summary = "List retrieval history for a research source")
    public ResponseEntity<ApiResponse<Page<SourceRetrievalDto>>> listRetrievals(
            @PathVariable String id,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(ingestionService.listRetrievals(id, pageable)));
    }

    @GetMapping("/sources/{id}/versions")
    @Operation(summary = "List version history for a research source")
    public ResponseEntity<ApiResponse<Page<SourceVersionDto>>> listVersions(
            @PathVariable String id,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(ingestionService.listVersions(id, pageable)));
    }

    @GetMapping("/versions/{versionId}/extracted-document")
    @Operation(summary = "Get extracted document metadata for a source version")
    public ResponseEntity<ApiResponse<ExtractedDocumentDto>> getExtractedDocument(@PathVariable String versionId) {
        return ResponseEntity.ok(ApiResponse.ok(ingestionService.getExtractedDocumentByVersionId(versionId)));
    }

    @GetMapping("/chunks/search")
    @Operation(summary = "Search evidence passages with multi-criteria metadata filters")
    public ResponseEntity<ApiResponse<Page<EvidenceChunkDto>>> searchChunks(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "sourceType", required = false) SourceType sourceType,
            @RequestParam(value = "publisher", required = false) String publisher,
            @RequestParam(value = "institutionId", required = false) String institutionId,
            @RequestParam(value = "departmentId", required = false) String departmentId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<EvidenceChunkDto> results = searchService.searchEvidence(
                query, sourceType, publisher, institutionId, departmentId, pageable
        );
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/chunks/{chunkId}")
    @Operation(summary = "Get an individual evidence passage with its complete citation provenance")
    public ResponseEntity<ApiResponse<EvidenceChunkDto>> getChunk(@PathVariable String chunkId) {
        return ResponseEntity.ok(ApiResponse.ok(ingestionService.getChunk(chunkId)));
    }

    @PostMapping("/search/hybrid")
    @Operation(summary = "Execute hybrid semantic + keyword search with Reciprocal Rank Fusion (RRF)")
    public ResponseEntity<ApiResponse<HybridSearchResponse>> searchHybrid(
            @Valid @RequestBody HybridSearchRequest request) {
        HybridSearchResponse response = hybridRetrievalService.searchHybrid(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/search/hybrid")
    @Operation(summary = "Execute hybrid semantic + keyword search with GET query parameters")
    public ResponseEntity<ApiResponse<HybridSearchResponse>> searchHybridGet(
            @RequestParam("query") String query,
            @RequestParam(value = "sourceType", required = false) SourceType sourceType,
            @RequestParam(value = "publisher", required = false) String publisher,
            @RequestParam(value = "institutionId", required = false) String institutionId,
            @RequestParam(value = "departmentId", required = false) String departmentId,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        HybridSearchRequest request = new HybridSearchRequest(query, sourceType, publisher, institutionId, departmentId, limit);
        HybridSearchResponse response = hybridRetrievalService.searchHybrid(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/embeddings/status/{documentId}")
    @Operation(summary = "Inspect embedding generation status for an extracted document")
    public ResponseEntity<ApiResponse<EmbeddingStatusSummaryDto>> getEmbeddingStatus(
            @PathVariable String documentId) {
        EmbeddingStatusSummaryDto status = embeddingService.getDocumentEmbeddingStatus(documentId);
        return ResponseEntity.ok(ApiResponse.ok(status));
    }

    @PostMapping("/embeddings/retry/{documentId}")
    @Operation(summary = "Retry failed embedding generations for an extracted document")
    public ResponseEntity<ApiResponse<EmbeddingStatusSummaryDto>> retryEmbeddings(
            @PathVariable String documentId) {
        EmbeddingStatusSummaryDto status = embeddingService.retryFailedEmbeddings(documentId);
        return ResponseEntity.ok(ApiResponse.ok(status, "Retried embedding generation for document " + documentId));
    }
}

