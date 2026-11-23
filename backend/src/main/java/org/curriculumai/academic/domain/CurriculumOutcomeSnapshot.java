package org.curriculumai.academic.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "curriculum_outcome_snapshots")
public class CurriculumOutcomeSnapshot {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_version_id", nullable = false)
    private CurriculumVersion curriculumVersion;

    @Column(name = "snapshot_type", nullable = false, length = 50)
    private String snapshotType; // COURSE_OUTCOME, PROGRAMME_OUTCOME, CO_PO_MAPPING

    @Column(name = "source_entity_id", nullable = false, length = 64)
    private String sourceEntityId;

    @Column(name = "course_id", length = 64)
    private String courseId;

    @Column(name = "course_code", length = 50)
    private String courseCode;

    @Column(name = "outcome_code", nullable = false, length = 20)
    private String outcomeCode;

    @Column(length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "bloom_level", length = 50)
    private String bloomLevel;

    @Column(name = "target_outcome_code", length = 20)
    private String targetOutcomeCode;

    @Column(name = "mapping_level")
    private Integer mappingLevel;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public CurriculumOutcomeSnapshot() {}

    @PreUpdate
    public void preUpdate() {
        throw new UnsupportedOperationException("Curriculum outcome snapshots are immutable and cannot be updated.");
    }

    @PreRemove
    public void preRemove() {
        throw new UnsupportedOperationException("Curriculum outcome snapshots are immutable and cannot be deleted.");
    }

    public static CurriculumOutcomeSnapshot ofProgrammeOutcome(String id, CurriculumVersion cv, ProgrammeOutcome po) {
        CurriculumOutcomeSnapshot s = new CurriculumOutcomeSnapshot();
        s.id = id;
        s.curriculumVersion = cv;
        s.snapshotType = "PROGRAMME_OUTCOME";
        s.sourceEntityId = po.getId();
        s.outcomeCode = po.getCode();
        s.title = po.getTitle();
        s.description = po.getDescription();
        s.createdAt = Instant.now();
        return s;
    }

    public static CurriculumOutcomeSnapshot ofCourseOutcome(String id, CurriculumVersion cv, Course course, CourseOutcome co) {
        CurriculumOutcomeSnapshot s = new CurriculumOutcomeSnapshot();
        s.id = id;
        s.curriculumVersion = cv;
        s.snapshotType = "COURSE_OUTCOME";
        s.sourceEntityId = co.getId();
        s.courseId = course.getId();
        s.courseCode = course.getCode();
        s.outcomeCode = co.getCode();
        s.description = co.getDescription();
        s.bloomLevel = co.getBloomLevel();
        s.createdAt = Instant.now();
        return s;
    }

    public static CurriculumOutcomeSnapshot ofCoPoMapping(String id, CurriculumVersion cv, Course course, CoPoMapping map) {
        CurriculumOutcomeSnapshot s = new CurriculumOutcomeSnapshot();
        s.id = id;
        s.curriculumVersion = cv;
        s.snapshotType = "CO_PO_MAPPING";
        s.sourceEntityId = map.getId();
        s.courseId = course.getId();
        s.courseCode = course.getCode();
        s.outcomeCode = map.getCourseOutcome().getCode();
        s.targetOutcomeCode = map.getProgrammeOutcome().getCode();
        s.mappingLevel = map.getMappingLevel();
        s.description = "Mapping: " + s.outcomeCode + " -> " + s.targetOutcomeCode;
        s.justification = map.getJustification();
        s.createdAt = Instant.now();
        return s;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public CurriculumVersion getCurriculumVersion() { return curriculumVersion; }
    public void setCurriculumVersion(CurriculumVersion curriculumVersion) { this.curriculumVersion = curriculumVersion; }

    public String getSnapshotType() { return snapshotType; }
    public void setSnapshotType(String snapshotType) { this.snapshotType = snapshotType; }

    public String getSourceEntityId() { return sourceEntityId; }
    public void setSourceEntityId(String sourceEntityId) { this.sourceEntityId = sourceEntityId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getOutcomeCode() { return outcomeCode; }
    public void setOutcomeCode(String outcomeCode) { this.outcomeCode = outcomeCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBloomLevel() { return bloomLevel; }
    public void setBloomLevel(String bloomLevel) { this.bloomLevel = bloomLevel; }

    public String getTargetOutcomeCode() { return targetOutcomeCode; }
    public void setTargetOutcomeCode(String targetOutcomeCode) { this.targetOutcomeCode = targetOutcomeCode; }

    public Integer getMappingLevel() { return mappingLevel; }
    public void setMappingLevel(Integer mappingLevel) { this.mappingLevel = mappingLevel; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

