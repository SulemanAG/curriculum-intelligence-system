package org.curriculumai.evidence.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "source_retrievals")
public class SourceRetrieval {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "source_id", nullable = false, length = 64)
    private String sourceId;

    @Column(name = "retrieval_timestamp", nullable = false)
    private Instant retrievalTimestamp = Instant.now();

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "final_url", columnDefinition = "TEXT")
    private String finalUrl;

    @Column(name = "content_type", length = 255)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "retrieval_outcome", nullable = false, length = 50)
    private RetrievalOutcome retrievalOutcome;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "content_hash", length = 128)
    private String contentHash;

    @Column(length = 255)
    private String etag;

    @Column(name = "last_modified_header", length = 255)
    private String lastModifiedHeader;

    @Column(name = "elapsed_millis", nullable = false)
    private Long elapsedMillis = 0L;

    @Column(name = "initiated_by", length = 64)
    private String initiatedBy;

    public SourceRetrieval() {
    }

    public SourceRetrieval(String id, String sourceId, Integer httpStatus, String finalUrl,
                           String contentType, RetrievalOutcome retrievalOutcome,
                           String failureReason, String contentHash, String etag,
                           String lastModifiedHeader, Long elapsedMillis, String initiatedBy) {
        this.id = id;
        this.sourceId = sourceId;
        this.retrievalTimestamp = Instant.now();
        this.httpStatus = httpStatus;
        this.finalUrl = finalUrl;
        this.contentType = contentType;
        this.retrievalOutcome = retrievalOutcome;
        this.failureReason = failureReason;
        this.contentHash = contentHash;
        this.etag = etag;
        this.lastModifiedHeader = lastModifiedHeader;
        this.elapsedMillis = elapsedMillis != null ? elapsedMillis : 0L;
        this.initiatedBy = initiatedBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public Instant getRetrievalTimestamp() {
        return retrievalTimestamp;
    }

    public void setRetrievalTimestamp(Instant retrievalTimestamp) {
        this.retrievalTimestamp = retrievalTimestamp;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getFinalUrl() {
        return finalUrl;
    }

    public void setFinalUrl(String finalUrl) {
        this.finalUrl = finalUrl;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public RetrievalOutcome getRetrievalOutcome() {
        return retrievalOutcome;
    }

    public void setRetrievalOutcome(RetrievalOutcome retrievalOutcome) {
        this.retrievalOutcome = retrievalOutcome;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getLastModifiedHeader() {
        return lastModifiedHeader;
    }

    public void setLastModifiedHeader(String lastModifiedHeader) {
        this.lastModifiedHeader = lastModifiedHeader;
    }

    public Long getElapsedMillis() {
        return elapsedMillis;
    }

    public void setElapsedMillis(Long elapsedMillis) {
        this.elapsedMillis = elapsedMillis;
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }
}

