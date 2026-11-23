package org.curriculumai.coordinator;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "parallel_agent_executions")
public class ParallelAgentExecution {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_stage_id", nullable = false)
    private RunStage runStage;

    @Column(name = "agent_name", nullable = false, length = 100)
    private String agentName;

    @Column(nullable = false, length = 255)
    private String role;

    @Column(nullable = false, length = 50)
    private String status; // 'pending', 'running', 'completed', 'failed'

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ParallelAgentExecution() {
    }

    public ParallelAgentExecution(String id, RunStage runStage, String agentName, String role, String status, String message) {
        this.id = id;
        this.runStage = runStage;
        this.agentName = agentName;
        this.role = role;
        this.status = status;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RunStage getRunStage() {
        return runStage;
    }

    public void setRunStage(RunStage runStage) {
        this.runStage = runStage;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

