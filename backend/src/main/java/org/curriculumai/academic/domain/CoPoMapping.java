package org.curriculumai.academic.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "co_po_mappings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_co_po_mapping", columnNames = {"course_outcome_id", "programme_outcome_id"})
})
public class CoPoMapping {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_outcome_id", nullable = false)
    private CourseOutcome courseOutcome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_outcome_id", nullable = false)
    private ProgrammeOutcome programmeOutcome;

    @Column(name = "mapping_level", nullable = false)
    private int mappingLevel; // 1: Low, 2: Medium, 3: High

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public CoPoMapping() {}

    public CoPoMapping(String id, CourseOutcome courseOutcome, ProgrammeOutcome programmeOutcome, int mappingLevel, String justification) {
        if (mappingLevel < 1 || mappingLevel > 3) {
            throw new IllegalArgumentException("CO-PO mapping level must be 1 (Low), 2 (Medium), or 3 (High). Provided: " + mappingLevel);
        }
        this.id = id;
        this.courseOutcome = courseOutcome;
        this.programmeOutcome = programmeOutcome;
        this.mappingLevel = mappingLevel;
        this.justification = justification;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public CourseOutcome getCourseOutcome() { return courseOutcome; }
    public void setCourseOutcome(CourseOutcome courseOutcome) { this.courseOutcome = courseOutcome; }

    public ProgrammeOutcome getProgrammeOutcome() { return programmeOutcome; }
    public void setProgrammeOutcome(ProgrammeOutcome programmeOutcome) { this.programmeOutcome = programmeOutcome; }

    public int getMappingLevel() { return mappingLevel; }
    public void setMappingLevel(int mappingLevel) {
        if (mappingLevel < 1 || mappingLevel > 3) {
            throw new IllegalArgumentException("CO-PO mapping level must be 1 (Low), 2 (Medium), or 3 (High). Provided: " + mappingLevel);
        }
        this.mappingLevel = mappingLevel;
    }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

