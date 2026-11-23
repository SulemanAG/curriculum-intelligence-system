package org.curriculumai.academic.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "course_outcomes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_course_co_code", columnNames = {"course_id", "code"})
})
public class CourseOutcome {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 20)
    private String code; // CO1, CO2, etc.

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "bloom_level", length = 50)
    private String bloomLevel; // L1_REMEMBER, L2_UNDERSTAND, L3_APPLY, L4_ANALYZE, L5_EVALUATE, L6_CREATE

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public CourseOutcome() {}

    public CourseOutcome(String id, Course course, String code, String description, String bloomLevel) {
        this.id = id;
        this.course = course;
        this.code = code;
        this.description = description;
        this.bloomLevel = bloomLevel;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBloomLevel() { return bloomLevel; }
    public void setBloomLevel(String bloomLevel) { this.bloomLevel = bloomLevel; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

