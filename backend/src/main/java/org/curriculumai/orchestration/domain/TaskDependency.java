package org.curriculumai.orchestration.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "task_dependencies", uniqueConstraints = {
        @UniqueConstraint(name = "uk_task_dependency", columnNames = {"task_id", "depends_on_task_id"})
})
public class TaskDependency {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "run_id", nullable = false, length = 64)
    private String runId;

    @Column(name = "task_id", nullable = false, length = 64)
    private String taskId;

    @Column(name = "depends_on_task_id", nullable = false, length = 64)
    private String dependsOnTaskId;

    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public TaskDependency() {
    }

    public TaskDependency(String id, String runId, String taskId, String dependsOnTaskId, boolean required) {
        this.id = id;
        this.runId = runId;
        this.taskId = taskId;
        this.dependsOnTaskId = dependsOnTaskId;
        this.required = required;
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

    public String getDependsOnTaskId() {
        return dependsOnTaskId;
    }

    public void setDependsOnTaskId(String dependsOnTaskId) {
        this.dependsOnTaskId = dependsOnTaskId;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

