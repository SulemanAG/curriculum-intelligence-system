package org.curriculumai.evidence.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "extracted_documents")
public class ExtractedDocument {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "source_version_id", nullable = false, unique = true, length = 64)
    private String sourceVersionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "extraction_status", nullable = false, length = 50)
    private ExtractionStatus extractionStatus;

    @Column(name = "extractor_name", nullable = false, length = 100)
    private String extractorName;

    @Column(name = "extractor_version", nullable = false, length = 50)
    private String extractorVersion;

    @Column(name = "extraction_timestamp", nullable = false)
    private Instant extractionTimestamp = Instant.now();

    @Column(name = "document_title", length = 500)
    private String documentTitle;

    @Column(name = "detected_language", length = 50)
    private String detectedLanguage;

    @Column(name = "structured_metadata_json", columnDefinition = "TEXT")
    private String structuredMetadataJson;

    @Column(name = "total_pages")
    private Integer totalPages;

    @Column(name = "total_paragraphs")
    private Integer totalParagraphs;

    @Column(name = "total_tables")
    private Integer totalTables;

    @Column(name = "total_characters")
    private Integer totalCharacters;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "embedding_status", nullable = false, length = 50)
    private EmbeddingStatus embeddingStatus = EmbeddingStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ExtractedDocument() {
    }

    public ExtractedDocument(String id, String sourceVersionId, ExtractionStatus extractionStatus,
                             String extractorName, String extractorVersion, String documentTitle,
                             String detectedLanguage, String structuredMetadataJson,
                             Integer totalPages, Integer totalParagraphs, Integer totalTables,
                             Integer totalCharacters, String errorDetails) {
        this.id = id;
        this.sourceVersionId = sourceVersionId;
        this.extractionStatus = extractionStatus;
        this.extractorName = extractorName;
        this.extractorVersion = extractorVersion;
        this.extractionTimestamp = Instant.now();
        this.documentTitle = documentTitle;
        this.detectedLanguage = detectedLanguage;
        this.structuredMetadataJson = structuredMetadataJson;
        this.totalPages = totalPages;
        this.totalParagraphs = totalParagraphs;
        this.totalTables = totalTables;
        this.totalCharacters = totalCharacters;
        this.errorDetails = errorDetails;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceVersionId() {
        return sourceVersionId;
    }

    public void setSourceVersionId(String sourceVersionId) {
        this.sourceVersionId = sourceVersionId;
    }

    public ExtractionStatus getExtractionStatus() {
        return extractionStatus;
    }

    public void setExtractionStatus(ExtractionStatus extractionStatus) {
        this.extractionStatus = extractionStatus;
    }

    public String getExtractorName() {
        return extractorName;
    }

    public void setExtractorName(String extractorName) {
        this.extractorName = extractorName;
    }

    public String getExtractorVersion() {
        return extractorVersion;
    }

    public void setExtractorVersion(String extractorVersion) {
        this.extractorVersion = extractorVersion;
    }

    public Instant getExtractionTimestamp() {
        return extractionTimestamp;
    }

    public void setExtractionTimestamp(Instant extractionTimestamp) {
        this.extractionTimestamp = extractionTimestamp;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getDetectedLanguage() {
        return detectedLanguage;
    }

    public void setDetectedLanguage(String detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }

    public String getStructuredMetadataJson() {
        return structuredMetadataJson;
    }

    public void setStructuredMetadataJson(String structuredMetadataJson) {
        this.structuredMetadataJson = structuredMetadataJson;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getTotalParagraphs() {
        return totalParagraphs;
    }

    public void setTotalParagraphs(Integer totalParagraphs) {
        this.totalParagraphs = totalParagraphs;
    }

    public Integer getTotalTables() {
        return totalTables;
    }

    public void setTotalTables(Integer totalTables) {
        this.totalTables = totalTables;
    }

    public Integer getTotalCharacters() {
        return totalCharacters;
    }

    public void setTotalCharacters(Integer totalCharacters) {
        this.totalCharacters = totalCharacters;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public EmbeddingStatus getEmbeddingStatus() {
        return embeddingStatus;
    }

    public void setEmbeddingStatus(EmbeddingStatus embeddingStatus) {
        this.embeddingStatus = embeddingStatus;
    }
}

