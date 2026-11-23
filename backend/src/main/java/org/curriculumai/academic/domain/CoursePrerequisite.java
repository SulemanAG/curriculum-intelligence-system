package org.curriculumai.academic.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "course_prerequisites", uniqueConstraints = {
        @UniqueConstraint(name = "uk_course_prereq", columnNames = {"course_id", "prerequisite_course_id"})
})
public class CoursePrerequisite {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_course_id", nullable = false)
    private Course prerequisiteCourse;

    @Column(name = "requirement_type", nullable = false, length = 50)
    private String requirementType = "HARD_PREREQUISITE"; // HARD_PREREQUISITE, RECOMMENDED, COREQUISITE

    @Column(name = "minimum_grade", length = 10)
    private String minimumGrade;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public CoursePrerequisite() {}

    public CoursePrerequisite(String id, Course course, Course prerequisiteCourse, String requirementType, String minimumGrade) {
        if (course != null && prerequisiteCourse != null && course.getId().equals(prerequisiteCourse.getId())) {
            throw new IllegalArgumentException("A course cannot have itself as a prerequisite.");
        }
        this.id = id;
        this.course = course;
        this.prerequisiteCourse = prerequisiteCourse;
        this.requirementType = requirementType != null ? requirementType : "HARD_PREREQUISITE";
        this.minimumGrade = minimumGrade;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Course getPrerequisiteCourse() { return prerequisiteCourse; }
    public void setPrerequisiteCourse(Course prerequisiteCourse) { this.prerequisiteCourse = prerequisiteCourse; }

    public String getRequirementType() { return requirementType; }
    public void setRequirementType(String requirementType) { this.requirementType = requirementType; }

    public String getMinimumGrade() { return minimumGrade; }
    public void setMinimumGrade(String minimumGrade) { this.minimumGrade = minimumGrade; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

