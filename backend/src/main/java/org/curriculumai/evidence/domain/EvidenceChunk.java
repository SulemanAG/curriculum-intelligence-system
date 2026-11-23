package org.curriculumai.evidence.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "evidence_chunks", uniqueConstraints = {
        @UniqueConstraint(name = "uk_doc_seq", columnNames = {"extracted_document_id", "sequence_number"})
})
public class EvidenceChunk {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "extracted_document_id", nullable = false, length = 64)
    private String extractedDocumentId;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "stable_chunk_id", nullable = false, length = 100)
    private String stableChunkId;

    @Column(name = "text_content", nullable = false, columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "section_heading", length = 500)
    private String sectionHeading;

    @Column(name = "char_offset_start")
    private Integer charOffsetStart;

    @Column(name = "char_offset_end")
    private Integer charOffsetEnd;

    @Column(name = "chunking_strategy", nullable = false, length = 50)
    private String chunkingStrategy;

    @Column(name = "strategy_version", nullable = false, length = 20)
    private String strategyVersion;

    @Column(name = "content_hash", nullable = false, length = 128)
    private String contentHash;

    @Column(name = "citation_metadata_json", columnDefinition = "TEXT")
    private String citationMetadataJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public EvidenceChunk() {
    }

    public EvidenceChunk(String id, String extractedDocumentId, Integer sequenceNumber,
                         String stableChunkId, String textContent, Integer pageNumber,
                         String sectionHeading, Integer charOffsetStart, Integer charOffsetEnd,
                         String chunkingStrategy, String strategyVersion, String contentHash,
                         String citationMetadataJson) {
        this.id = id;
        this.extractedDocumentId = extractedDocumentId;
        this.sequenceNumber = sequenceNumber;
        this.stableChunkId = stableChunkId;
        this.textContent = textContent;
        this.pageNumber = pageNumber;
        this.sectionHeading = sectionHeading;
        this.charOffsetStart = charOffsetStart;
        this.charOffsetEnd = charOffsetEnd;
        this.chunkingStrategy = chunkingStrategy;
        this.strategyVersion = strategyVersion;
        this.contentHash = contentHash;
        this.citationMetadataJson = citationMetadataJson;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExtractedDocumentId() {
        return extractedDocumentId;
    }

    public void setExtractedDocumentId(String extractedDocumentId) {
        this.extractedDocumentId = extractedDocumentId;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getStableChunkId() {
        return stableChunkId;
    }

    public void setStableChunkId(String stableChunkId) {
        this.stableChunkId = stableChunkId;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getSectionHeading() {
        return sectionHeading;
    }

    public void setSectionHeading(String sectionHeading) {
        this.sectionHeading = sectionHeading;
    }

    public Integer getCharOffsetStart() {
        return charOffsetStart;
    }

    public void setCharOffsetStart(Integer charOffsetStart) {
        this.charOffsetStart = charOffsetStart;
    }

    public Integer getCharOffsetEnd() {
        return charOffsetEnd;
    }

    public void setCharOffsetEnd(Integer charOffsetEnd) {
        this.charOffsetEnd = charOffsetEnd;
    }

    public String getChunkingStrategy() {
        return chunkingStrategy;
    }

    public void setChunkingStrategy(String chunkingStrategy) {
        this.chunkingStrategy = chunkingStrategy;
    }

    public String getStrategyVersion() {
        return strategyVersion;
    }

    public void setStrategyVersion(String strategyVersion) {
        this.strategyVersion = strategyVersion;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public String getCitationMetadataJson() {
        return citationMetadataJson;
    }

    public void setCitationMetadataJson(String citationMetadataJson) {
        this.citationMetadataJson = citationMetadataJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

