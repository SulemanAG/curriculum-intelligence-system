package org.curriculumai.coordinator;

import jakarta.persistence.*;
import org.curriculumai.conversations.Conversation;
import org.curriculumai.planning.LockedPlan;
import org.curriculumai.users.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analysis_runs")
public class AnalysisRun {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "raw_prompt", nullable = false, columnDefinition = "TEXT")
    private String rawPrompt;

    @Column(nullable = false, length = 255)
    private String department;

    @Column(name = "degree_level", nullable = false, length = 20)
    private String degreeLevel = "UG";

    @Column(nullable = false, length = 50)
    private String status = "initialized"; // 'running', 'completed', 'awaiting_human_input', 'failed'

    @Column(name = "current_stage_id", nullable = false, length = 100)
    private String currentStageId = "intake_and_clarification";

    @Column(name = "elapsed_seconds", nullable = false)
    private int elapsedSeconds = 0;

    @Column(name = "has_alternatives", nullable = false)
    private boolean hasAlternatives = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToOne(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    private LockedPlan lockedPlan;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stageOrder ASC")
    private List<RunStage> stages = new ArrayList<>();

    public AnalysisRun() {
    }

    public AnalysisRun(String id, User user, String rawPrompt, String department, String degreeLevel) {
        this.id = id;
        this.user = user;
        this.rawPrompt = rawPrompt;
        this.department = department;
        this.degreeLevel = degreeLevel;
        this.status = "running";
        this.currentStageId = "intake_and_clarification";
        this.elapsedSeconds = 0;
        this.hasAlternatives = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRawPrompt() {
        return rawPrompt;
    }

    public void setRawPrompt(String rawPrompt) {
        this.rawPrompt = rawPrompt;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDegreeLevel() {
        return degreeLevel;
    }

    public void setDegreeLevel(String degreeLevel) {
        this.degreeLevel = degreeLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentStageId() {
        return currentStageId;
    }

    public void setCurrentStageId(String currentStageId) {
        this.currentStageId = currentStageId;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(int elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public boolean isHasAlternatives() {
        return hasAlternatives;
    }

    public void setHasAlternatives(boolean hasAlternatives) {
        this.hasAlternatives = hasAlternatives;
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

    public LockedPlan getLockedPlan() {
        return lockedPlan;
    }

    public void setLockedPlan(LockedPlan lockedPlan) {
        this.lockedPlan = lockedPlan;
    }

    public List<RunStage> getStages() {
        return stages;
    }

    public void setStages(List<RunStage> stages) {
        this.stages = stages;
    }
}

