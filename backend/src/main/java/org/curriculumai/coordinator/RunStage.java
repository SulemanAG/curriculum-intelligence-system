package org.curriculumai.coordinator;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "run_stages")
public class RunStage {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private AnalysisRun run;

    @Column(name = "stage_id", nullable = false, length = 100)
    private String stageId;

    @Column(name = "stage_order", nullable = false)
    private int stageOrder;

    @Column(nullable = false, length = 255)
    private String label;

    @Column(nullable = false, length = 50)
    private String status; // 'pending', 'running', 'completed', 'failed'

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "elapsed_seconds", nullable = false)
    private int elapsedSeconds = 0;

    @Column(name = "has_parallel_agents", nullable = false)
    private boolean hasParallelAgents = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "runStage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParallelAgentExecution> parallelAgents = new ArrayList<>();

    public RunStage() {
    }

    public RunStage(String id, AnalysisRun run, String stageId, int stageOrder, String label, String status, String message) {
        this.id = id;
        this.run = run;
        this.stageId = stageId;
        this.stageOrder = stageOrder;
        this.label = label;
        this.status = status;
        this.message = message;
        this.elapsedSeconds = 0;
        this.hasParallelAgents = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AnalysisRun getRun() {
        return run;
    }

    public void setRun(AnalysisRun run) {
        this.run = run;
    }

    public String getStageId() {
        return stageId;
    }

    public void setStageId(String stageId) {
        this.stageId = stageId;
    }

    public int getStageOrder() {
        return stageOrder;
    }

    public void setStageOrder(int stageOrder) {
        this.stageOrder = stageOrder;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(int elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public boolean isHasParallelAgents() {
        return hasParallelAgents;
    }

    public void setHasParallelAgents(boolean hasParallelAgents) {
        this.hasParallelAgents = hasParallelAgents;
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

    public List<ParallelAgentExecution> getParallelAgents() {
        return parallelAgents;
    }

    public void setParallelAgents(List<ParallelAgentExecution> parallelAgents) {
        this.parallelAgents = parallelAgents;
    }
}

