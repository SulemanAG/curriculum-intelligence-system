package org.curriculumai.evidence.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "evidence_chunk_embeddings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_chunk_model", columnNames = {"chunk_id", "model_name"})
})
public class EvidenceChunkEmbedding {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "chunk_id", nullable = false, length = 64)
    private String chunkId;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(nullable = false)
    private Integer dimension;

    @Column(name = "embedding_json", columnDefinition = "TEXT")
    private String embeddingJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EmbeddingStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public EvidenceChunkEmbedding() {
    }

    public EvidenceChunkEmbedding(String id, String chunkId, String modelName, Integer dimension,
                                  String embeddingJson, EmbeddingStatus status, String errorMessage,
                                  Integer retryCount) {
        this.id = id;
        this.chunkId = chunkId;
        this.modelName = modelName;
        this.dimension = dimension;
        this.embeddingJson = embeddingJson;
        this.status = status;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount != null ? retryCount : 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChunkId() {
        return chunkId;
    }

    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getDimension() {
        return dimension;
    }

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    public String getEmbeddingJson() {
        return embeddingJson;
    }

    public void setEmbeddingJson(String embeddingJson) {
        this.embeddingJson = embeddingJson;
    }

    public EmbeddingStatus getStatus() {
        return status;
    }

    public void setStatus(EmbeddingStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

