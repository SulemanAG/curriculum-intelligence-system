package org.curriculumai.academic.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "semesters", uniqueConstraints = {
        @UniqueConstraint(name = "uk_curriculum_semester", columnNames = {"curriculum_version_id", "semester_number"})
})
public class Semester {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_version_id", nullable = false)
    private CurriculumVersion curriculumVersion;

    @Column(name = "semester_number", nullable = false)
    private int semesterNumber;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(name = "credits_target")
    private Integer creditsTarget;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Semester() {}

    public Semester(String id, CurriculumVersion curriculumVersion, int semesterNumber, String label, Integer creditsTarget) {
        this.id = id;
        this.curriculumVersion = curriculumVersion;
        this.semesterNumber = semesterNumber;
        this.label = label;
        this.creditsTarget = creditsTarget;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public CurriculumVersion getCurriculumVersion() { return curriculumVersion; }
    public void setCurriculumVersion(CurriculumVersion curriculumVersion) { this.curriculumVersion = curriculumVersion; }

    public int getSemesterNumber() { return semesterNumber; }
    public void setSemesterNumber(int semesterNumber) { this.semesterNumber = semesterNumber; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Integer getCreditsTarget() { return creditsTarget; }
    public void setCreditsTarget(Integer creditsTarget) { this.creditsTarget = creditsTarget; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

