package org.curriculumai.orchestration.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "orchestration_runs")
public class OrchestrationRun {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "conversation_id", length = 64)
    private String conversationId;

    @Column(name = "message_id", length = 64)
    private String messageId;

    @Column(name = "raw_prompt", nullable = false, columnDefinition = "TEXT")
    private String rawPrompt;

    @Column(nullable = false, length = 500)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_type", nullable = false, length = 100)
    private WorkflowType workflowType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RunStatus status = RunStatus.CREATED;

    @Column(name = "plan_definition_json", columnDefinition = "TEXT")
    private String planDefinitionJson;

    @Column(name = "shared_context_json", columnDefinition = "TEXT")
    private String sharedContextJson;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "current_checkpoint", length = 100)
    private String currentCheckpoint;

    @Column(name = "total_tasks", nullable = false)
    private int totalTasks = 0;

    @Column(name = "completed_tasks", nullable = false)
    private int completedTasks = 0;

    @Column(name = "failed_tasks", nullable = false)
    private int failedTasks = 0;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public OrchestrationRun() {
    }

    public OrchestrationRun(String id, String userId, String conversationId, String messageId,
                            String rawPrompt, String subject, WorkflowType workflowType) {
        this.id = id;
        this.userId = userId;
        this.conversationId = conversationId;
        this.messageId = messageId;
        this.rawPrompt = rawPrompt;
        this.subject = subject;
        this.workflowType = workflowType;
        this.status = RunStatus.CREATED;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRawPrompt() {
        return rawPrompt;
    }

    public void setRawPrompt(String rawPrompt) {
        this.rawPrompt = rawPrompt;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public WorkflowType getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(WorkflowType workflowType) {
        this.workflowType = workflowType;
    }

    public RunStatus getStatus() {
        return status;
    }

    public void setStatus(RunStatus status) {
        this.status = status;
    }

    public String getPlanDefinitionJson() {
        return planDefinitionJson;
    }

    public void setPlanDefinitionJson(String planDefinitionJson) {
        this.planDefinitionJson = planDefinitionJson;
    }

    public String getSharedContextJson() {
        return sharedContextJson;
    }

    public void setSharedContextJson(String sharedContextJson) {
        this.sharedContextJson = sharedContextJson;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCurrentCheckpoint() {
        return currentCheckpoint;
    }

    public void setCurrentCheckpoint(String currentCheckpoint) {
        this.currentCheckpoint = currentCheckpoint;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    public int getFailedTasks() {
        return failedTasks;
    }

    public void setFailedTasks(int failedTasks) {
        this.failedTasks = failedTasks;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
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

