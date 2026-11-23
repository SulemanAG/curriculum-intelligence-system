package org.curriculumai.orchestration.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "agent_execution_records")
public class AgentExecutionRecord {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "run_id", nullable = false, length = 64)
    private String runId;

    @Column(name = "task_id", nullable = false, length = 64)
    private String taskId;

    @Column(name = "agent_id", nullable = false, length = 100)
    private String agentId;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Column(name = "execution_status", nullable = false, length = 50)
    private String executionStatus;

    @Column(name = "findings_json", columnDefinition = "TEXT")
    private String findingsJson;

    @Column(name = "evidence_references_json", columnDefinition = "TEXT")
    private String evidenceReferencesJson;

    @Column(name = "limitations_json", columnDefinition = "TEXT")
    private String limitationsJson;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @Column(name = "execution_metadata_json", columnDefinition = "TEXT")
    private String executionMetadataJson;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AgentExecutionRecord() {
    }

    public AgentExecutionRecord(String id, String runId, String taskId, String agentId,
                                int attemptNumber, String executionStatus, String findingsJson,
                                String evidenceReferencesJson, String limitationsJson,
                                String errorDetails, String executionMetadataJson, long durationMs) {
        this.id = id;
        this.runId = runId;
        this.taskId = taskId;
        this.agentId = agentId;
        this.attemptNumber = attemptNumber;
        this.executionStatus = executionStatus;
        this.findingsJson = findingsJson;
        this.evidenceReferencesJson = evidenceReferencesJson;
        this.limitationsJson = limitationsJson;
        this.errorDetails = errorDetails;
        this.executionMetadataJson = executionMetadataJson;
        this.durationMs = durationMs;
        this.createdAt = Instant.now();
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

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
    }

    public String getFindingsJson() {
        return findingsJson;
    }

    public void setFindingsJson(String findingsJson) {
        this.findingsJson = findingsJson;
    }

    public String getEvidenceReferencesJson() {
        return evidenceReferencesJson;
    }

    public void setEvidenceReferencesJson(String evidenceReferencesJson) {
        this.evidenceReferencesJson = evidenceReferencesJson;
    }

    public String getLimitationsJson() {
        return limitationsJson;
    }

    public void setLimitationsJson(String limitationsJson) {
        this.limitationsJson = limitationsJson;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getExecutionMetadataJson() {
        return executionMetadataJson;
    }

    public void setExecutionMetadataJson(String executionMetadataJson) {
        this.executionMetadataJson = executionMetadataJson;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

