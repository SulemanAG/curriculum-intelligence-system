package org.curriculumai.orchestration.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "workflow_tasks", uniqueConstraints = {
        @UniqueConstraint(name = "uk_run_task", columnNames = {"run_id", "task_key"})
})
public class WorkflowTask {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "run_id", nullable = false, length = 64)
    private String runId;

    @Column(name = "task_key", nullable = false, length = 100)
    private String taskKey;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "assigned_agent_id", nullable = false, length = 100)
    private String assignedAgentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "sequence_order", nullable = false)
    private int sequenceOrder = 0;

    @Column(name = "is_parallel_eligible", nullable = false)
    private boolean parallelEligible = false;

    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries = 2;

    @Column(name = "timeout_seconds", nullable = false)
    private int timeoutSeconds = 120;

    @Column(name = "input_payload_json", columnDefinition = "TEXT")
    private String inputPayloadJson;

    @Column(name = "output_payload_json", columnDefinition = "TEXT")
    private String outputPayloadJson;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "error_type", length = 100)
    private String errorType;

    @Column(nullable = false)
    private boolean retryable = false;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public WorkflowTask() {
    }

    public WorkflowTask(String id, String runId, String taskKey, String title,
                        String assignedAgentId, int sequenceOrder, boolean parallelEligible, boolean required) {
        this.id = id;
        this.runId = runId;
        this.taskKey = taskKey;
        this.title = title;
        this.assignedAgentId = assignedAgentId;
        this.sequenceOrder = sequenceOrder;
        this.parallelEligible = parallelEligible;
        this.required = required;
        this.status = TaskStatus.PENDING;
        this.attemptCount = 0;
        this.maxRetries = 2;
        this.timeoutSeconds = 120;
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

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAssignedAgentId() {
        return assignedAgentId;
    }

    public void setAssignedAgentId(String assignedAgentId) {
        this.assignedAgentId = assignedAgentId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public int getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(int sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public boolean isParallelEligible() {
        return parallelEligible;
    }

    public void setParallelEligible(boolean parallelEligible) {
        this.parallelEligible = parallelEligible;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getInputPayloadJson() {
        return inputPayloadJson;
    }

    public void setInputPayloadJson(String inputPayloadJson) {
        this.inputPayloadJson = inputPayloadJson;
    }

    public String getOutputPayloadJson() {
        return outputPayloadJson;
    }

    public void setOutputPayloadJson(String outputPayloadJson) {
        this.outputPayloadJson = outputPayloadJson;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
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

    public Instant getStartedAt() {
        return startTime;
    }

    public void setStartedAt(Instant startedAt) {
        this.startTime = startedAt;
    }

    public Instant getCompletedAt() {
        return endTime;
    }

    public void setCompletedAt(Instant completedAt) {
        this.endTime = completedAt;
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
