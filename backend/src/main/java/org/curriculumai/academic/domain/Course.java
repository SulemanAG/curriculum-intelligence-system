package org.curriculumai.academic.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "courses", uniqueConstraints = {
        @UniqueConstraint(name = "uk_course_dept_code", columnNames = {"department_id", "code"})
})
public class Course {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "course_type", length = 50)
    private String courseType; // CORE, PROFESSIONAL_ELECTIVE, OPEN_ELECTIVE, LAB, PROJECT, MANDATORY_AUDIT

    @Column(name = "default_credits")
    private Integer defaultCredits;

    @Column(name = "lecture_hours")
    private Integer lectureHours;

    @Column(name = "tutorial_hours")
    private Integer tutorialHours;

    @Column(name = "practical_hours")
    private Integer practicalHours;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Course() {}

    public Course(String id, Department department, String code, String title, Integer defaultCredits, Integer lectureHours, Integer tutorialHours, Integer practicalHours) {
        this.id = id;
        this.department = department;
        this.code = code;
        this.title = title;
        this.defaultCredits = defaultCredits;
        this.lectureHours = lectureHours;
        this.tutorialHours = tutorialHours;
        this.practicalHours = practicalHours;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }

    public Integer getDefaultCredits() { return defaultCredits; }
    public void setDefaultCredits(Integer defaultCredits) { this.defaultCredits = defaultCredits; }

    public Integer getLectureHours() { return lectureHours; }
    public void setLectureHours(Integer lectureHours) { this.lectureHours = lectureHours; }

    public Integer getTutorialHours() { return tutorialHours; }
    public void setTutorialHours(Integer tutorialHours) { this.tutorialHours = tutorialHours; }

    public Integer getPracticalHours() { return practicalHours; }
    public void setPracticalHours(Integer practicalHours) { this.practicalHours = practicalHours; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

