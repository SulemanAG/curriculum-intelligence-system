package org.curriculumai.academic.domain;

import jakarta.persistence.*;
import org.curriculumai.users.User;

import java.time.Instant;

@Entity
@Table(name = "curriculum_versions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_curriculum_version", columnNames = {"programme_id", "version_code"})
})
public class CurriculumVersion {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @Column(name = "version_code", nullable = false, length = 50)
    private String versionCode;

    @Column(name = "effective_year", nullable = false)
    private int effectiveYear;

    @Column(name = "total_credits")
    private Integer totalCredits;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_status", nullable = false, length = 50)
    private CurriculumLifecycleStatus lifecycleStatus = CurriculumLifecycleStatus.DRAFT;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "approval_metadata_json", columnDefinition = "TEXT")
    private String approvalMetadataJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "source_provenance", length = 255)
    private String sourceProvenance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public CurriculumVersion() {}

    public CurriculumVersion(String id, Programme programme, String versionCode, int effectiveYear) {
        this.id = id;
        this.programme = programme;
        this.versionCode = versionCode;
        this.effectiveYear = effectiveYear;
        this.lifecycleStatus = CurriculumLifecycleStatus.DRAFT;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isImmutable() {
        return lifecycleStatus.isImmutable();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Programme getProgramme() { return programme; }
    public void setProgramme(Programme programme) { this.programme = programme; }

    public String getVersionCode() { return versionCode; }
    public void setVersionCode(String versionCode) { this.versionCode = versionCode; }

    public int getEffectiveYear() { return effectiveYear; }
    public void setEffectiveYear(int effectiveYear) { this.effectiveYear = effectiveYear; }

    public Integer getTotalCredits() { return totalCredits; }
    public void setTotalCredits(Integer totalCredits) { this.totalCredits = totalCredits; }

    public CurriculumLifecycleStatus getLifecycleStatus() { return lifecycleStatus; }
    public void setLifecycleStatus(CurriculumLifecycleStatus lifecycleStatus) { this.lifecycleStatus = lifecycleStatus; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getApprovalMetadataJson() { return approvalMetadataJson; }
    public void setApprovalMetadataJson(String approvalMetadataJson) { this.approvalMetadataJson = approvalMetadataJson; }

    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) { this.updatedBy = updatedBy; }

    public String getSourceProvenance() { return sourceProvenance; }
    public void setSourceProvenance(String sourceProvenance) { this.sourceProvenance = sourceProvenance; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

