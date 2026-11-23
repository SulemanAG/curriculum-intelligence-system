package org.curriculumai.feedback;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "feedback_records")
public class FeedbackRecord {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "run_id", nullable = false, length = 64)
    private String runId;

    @Column(name = "selected_alternative_id", nullable = false, length = 64)
    private String selectedAlternativeId;

    @Column(name = "rejected_alternative_ids_json", columnDefinition = "TEXT")
    private String rejectedAlternativeIdsJson;

    @Column(name = "faculty_name")
    private String facultyName;

    @Column(name = "faculty_id")
    private String facultyId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rationale;

    @Column(name = "suggested_modifications", columnDefinition = "TEXT")
    private String suggestedModifications;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt = Instant.now();

    public FeedbackRecord() {
    }

    public FeedbackRecord(String id, String runId, String selectedAlternativeId, String rejectedAlternativeIdsJson, String facultyName, String facultyId, String rationale, String suggestedModifications) {
        this.id = id;
        this.runId = runId;
        this.selectedAlternativeId = selectedAlternativeId;
        this.rejectedAlternativeIdsJson = rejectedAlternativeIdsJson;
        this.facultyName = facultyName;
        this.facultyId = facultyId;
        this.rationale = rationale;
        this.suggestedModifications = suggestedModifications;
        this.submittedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getSelectedAlternativeId() {
        return selectedAlternativeId;
    }

    public void setSelectedAlternativeId(String selectedAlternativeId) {
        this.selectedAlternativeId = selectedAlternativeId;
    }

    public String getRejectedAlternativeIdsJson() {
        return rejectedAlternativeIdsJson;
    }

    public void setRejectedAlternativeIdsJson(String rejectedAlternativeIdsJson) {
        this.rejectedAlternativeIdsJson = rejectedAlternativeIdsJson;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public String getSuggestedModifications() {
        return suggestedModifications;
    }

    public void setSuggestedModifications(String suggestedModifications) {
        this.suggestedModifications = suggestedModifications;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }
}

