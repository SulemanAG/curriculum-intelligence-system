package org.curriculumai.academic.domain;

import jakarta.persistence.*;
import org.curriculumai.users.User;

import java.time.Instant;

@Entity
@Table(name = "academic_import_batches")
public class AcademicImportBatch {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    private Institution institution;

    @Column(nullable = false, length = 20)
    private String format; // JSON, CSV

    @Column(nullable = false, length = 50)
    private String status = "PREVIEW"; // PREVIEW, COMMITTED, REJECTED, FAILED

    @Column(name = "total_records", nullable = false)
    private int totalRecords = 0;

    @Column(name = "valid_records", nullable = false)
    private int validRecords = 0;

    @Column(name = "error_records", nullable = false)
    private int errorRecords = 0;

    @Column(name = "preview_data_json", columnDefinition = "TEXT")
    private String previewDataJson;

    @Column(name = "validation_report_json", columnDefinition = "TEXT")
    private String validationReportJson;

    @Column(name = "committed_at")
    private Instant committedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AcademicImportBatch() {}

    public AcademicImportBatch(String id, User user, Institution institution, String format) {
        this.id = id;
        this.user = user;
        this.institution = institution;
        this.format = format;
        this.status = "PREVIEW";
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public int getValidRecords() { return validRecords; }
    public void setValidRecords(int validRecords) { this.validRecords = validRecords; }

    public int getErrorRecords() { return errorRecords; }
    public void setErrorRecords(int errorRecords) { this.errorRecords = errorRecords; }

    public String getPreviewDataJson() { return previewDataJson; }
    public void setPreviewDataJson(String previewDataJson) { this.previewDataJson = previewDataJson; }

    public String getValidationReportJson() { return validationReportJson; }
    public void setValidationReportJson(String validationReportJson) { this.validationReportJson = validationReportJson; }

    public Instant getCommittedAt() { return committedAt; }
    public void setCommittedAt(Instant committedAt) { this.committedAt = committedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

