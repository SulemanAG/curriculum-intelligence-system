package org.curriculumai.evidence.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "research_sources")
public class ResearchSource {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "canonical_url", nullable = false, unique = true, columnDefinition = "TEXT")
    private String canonicalUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 50)
    private SourceType sourceType;

    @Column(length = 255)
    private String publisher;

    @Column(length = 500)
    private String title;

    @Column(length = 255)
    private String domain;

    @Column(name = "source_classification", length = 100)
    private String sourceClassification;

    @Column(name = "institution_id", length = 64)
    private String institutionId;

    @Column(name = "department_id", length = 64)
    private String departmentId;

    @Column(name = "programme_id", length = 64)
    private String programmeId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ResearchSource() {
    }

    public ResearchSource(String id, String canonicalUrl, SourceType sourceType, String publisher,
                          String title, String domain, String sourceClassification,
                          String institutionId, String departmentId, String programmeId,
                          String createdBy) {
        this.id = id;
        this.canonicalUrl = canonicalUrl;
        this.sourceType = sourceType;
        this.publisher = publisher;
        this.title = title;
        this.domain = domain;
        this.sourceClassification = sourceClassification;
        this.institutionId = institutionId;
        this.departmentId = departmentId;
        this.programmeId = programmeId;
        this.createdBy = createdBy;
        this.isActive = true;
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

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public void setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSourceClassification() {
        return sourceClassification;
    }

    public void setSourceClassification(String sourceClassification) {
        this.sourceClassification = sourceClassification;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(String programmeId) {
        this.programmeId = programmeId;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

