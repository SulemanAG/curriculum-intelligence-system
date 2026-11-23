package org.curriculumai.orchestration.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "workflow_event_history")
public class WorkflowEvent {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "run_id", nullable = false, length = 64)
    private String runId;

    @Column(name = "task_id", length = 64)
    private String taskId;

    @Column(name = "agent_id", length = 100)
    private String agentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 100)
    private WorkflowEventType eventType;

    @Column(name = "event_summary", nullable = false, columnDefinition = "TEXT")
    private String eventSummary;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public WorkflowEvent() {
    }

    public WorkflowEvent(String id, String runId, String taskId, String agentId,
                         WorkflowEventType eventType, String eventSummary, String metadataJson) {
        this.id = id;
        this.runId = runId;
        this.taskId = taskId;
        this.agentId = agentId;
        this.eventType = eventType;
        this.eventSummary = eventSummary;
        this.metadataJson = metadataJson;
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

    public WorkflowEventType getEventType() {
        return eventType;
    }

    public void setEventType(WorkflowEventType eventType) {
        this.eventType = eventType;
    }

    public String getEventSummary() {
        return eventSummary;
    }

    public void setEventSummary(String eventSummary) {
        this.eventSummary = eventSummary;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

