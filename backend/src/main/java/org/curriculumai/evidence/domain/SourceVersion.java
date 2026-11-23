package org.curriculumai.evidence.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "source_versions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_source_content_hash", columnNames = {"source_id", "content_hash"})
})
public class SourceVersion {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "source_id", nullable = false, length = 64)
    private String sourceId;

    @Column(name = "retrieval_id", length = 64)
    private String retrievalId;

    @Column(name = "content_hash", nullable = false, length = 128)
    private String contentHash;

    @Column(name = "version_identifier", nullable = false, length = 64)
    private String versionIdentifier;

    @Column(name = "original_publication_date", length = 50)
    private String originalPublicationDate;

    @Column(name = "retrieved_timestamp", nullable = false)
    private Instant retrievedTimestamp = Instant.now();

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "storage_reference", nullable = false, length = 500)
    private String storageReference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SourceVersion() {
    }

    public SourceVersion(String id, String sourceId, String retrievalId, String contentHash,
                         String versionIdentifier, String originalPublicationDate,
                         String originalFilename, String mimeType, Long fileSizeBytes,
                         String storageReference) {
        this.id = id;
        this.sourceId = sourceId;
        this.retrievalId = retrievalId;
        this.contentHash = contentHash;
        this.versionIdentifier = versionIdentifier;
        this.originalPublicationDate = originalPublicationDate;
        this.retrievedTimestamp = Instant.now();
        this.originalFilename = originalFilename;
        this.mimeType = mimeType;
        this.fileSizeBytes = fileSizeBytes;
        this.storageReference = storageReference;
        this.createdAt = Instant.now();
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

    public String getRetrievalId() {
        return retrievalId;
    }

    public void setRetrievalId(String retrievalId) {
        this.retrievalId = retrievalId;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public String getVersionIdentifier() {
        return versionIdentifier;
    }

    public void setVersionIdentifier(String versionIdentifier) {
        this.versionIdentifier = versionIdentifier;
    }

    public String getOriginalPublicationDate() {
        return originalPublicationDate;
    }

    public void setOriginalPublicationDate(String originalPublicationDate) {
        this.originalPublicationDate = originalPublicationDate;
    }

    public Instant getRetrievedTimestamp() {
        return retrievedTimestamp;
    }

    public void setRetrievedTimestamp(Instant retrievedTimestamp) {
        this.retrievedTimestamp = retrievedTimestamp;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getStorageReference() {
        return storageReference;
    }

    public void setStorageReference(String storageReference) {
        this.storageReference = storageReference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

