package org.curriculumai.planning;

import jakarta.persistence.*;
import org.curriculumai.coordinator.AnalysisRun;

import java.time.Instant;

@Entity
@Table(name = "locked_plans")
public class LockedPlan {

    @Id
    @Column(length = 64)
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false, unique = true)
    private AnalysisRun run;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(nullable = false)
    private String department;

    @Column(name = "degree_level", nullable = false, length = 20)
    private String degreeLevel;

    @Column(name = "course_nature", nullable = false, length = 50)
    private String courseNature; // 'Core', 'Professional Elective', 'Open Elective'

    @Column(name = "target_semester", nullable = false)
    private int targetSemester;

    @Column(name = "contact_distribution", nullable = false, length = 50)
    private String contactDistribution; // e.g., '3:0:2'

    @Column(name = "source_scope", columnDefinition = "TEXT")
    private String sourceScope;

    @Column(columnDefinition = "TEXT")
    private String standards;

    @Column(name = "validation_constraints", columnDefinition = "TEXT")
    private String validationConstraints;

    @Column(name = "is_locked", nullable = false)
    private boolean locked = true;

    @Column(name = "locked_at", nullable = false)
    private Instant lockedAt = Instant.now();

    public LockedPlan() {
    }

    public LockedPlan(String id, AnalysisRun run, String courseName, String department, String degreeLevel, String courseNature, int targetSemester, String contactDistribution) {
        this.id = id;
        this.run = run;
        this.courseName = courseName;
        this.department = department;
        this.degreeLevel = degreeLevel;
        this.courseNature = courseNature;
        this.targetSemester = targetSemester;
        this.contactDistribution = contactDistribution;
        this.locked = true;
        this.lockedAt = Instant.now();
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

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
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

    public String getCourseNature() {
        return courseNature;
    }

    public void setCourseNature(String courseNature) {
        this.courseNature = courseNature;
    }

    public int getTargetSemester() {
        return targetSemester;
    }

    public void setTargetSemester(int targetSemester) {
        this.targetSemester = targetSemester;
    }

    public String getContactDistribution() {
        return contactDistribution;
    }

    public void setContactDistribution(String contactDistribution) {
        this.contactDistribution = contactDistribution;
    }

    public String getSourceScope() {
        return sourceScope;
    }

    public void setSourceScope(String sourceScope) {
        this.sourceScope = sourceScope;
    }

    public String getStandards() {
        return standards;
    }

    public void setStandards(String standards) {
        this.standards = standards;
    }

    public String getValidationConstraints() {
        return validationConstraints;
    }

    public void setValidationConstraints(String validationConstraints) {
        this.validationConstraints = validationConstraints;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Instant getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Instant lockedAt) {
        this.lockedAt = lockedAt;
    }
}

