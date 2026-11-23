package org.curriculumai.academic.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "curriculum_courses", uniqueConstraints = {
        @UniqueConstraint(name = "uk_curr_sem_course", columnNames = {"curriculum_version_id", "semester_id", "course_id"})
})
public class CurriculumCourse {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_version_id", nullable = false)
    private CurriculumVersion curriculumVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "course_code", nullable = false, length = 50)
    private String courseCode;

    @Column(name = "course_title", nullable = false, length = 255)
    private String courseTitle;

    @Column(name = "course_type", length = 50)
    private String courseType;

    @Column
    private Integer credits;

    @Column(name = "lecture_hours")
    private Integer lectureHours;

    @Column(name = "tutorial_hours")
    private Integer tutorialHours;

    @Column(name = "practical_hours")
    private Integer practicalHours;

    @Column(name = "is_elective", nullable = false)
    private boolean elective = false;

    @Column(name = "elective_group", length = 100)
    private String electiveGroup;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public CurriculumCourse() {}

    public CurriculumCourse(String id, CurriculumVersion curriculumVersion, Semester semester, Course course,
                            Integer credits, Integer lectureHours, Integer tutorialHours, Integer practicalHours,
                            boolean elective, String electiveGroup) {
        this.id = id;
        this.curriculumVersion = curriculumVersion;
        this.semester = semester;
        this.course = course;
        this.courseCode = course.getCode();
        this.courseTitle = course.getTitle();
        this.courseType = course.getCourseType();
        this.credits = credits;
        this.lectureHours = lectureHours;
        this.tutorialHours = tutorialHours;
        this.practicalHours = practicalHours;
        this.elective = elective;
        this.electiveGroup = electiveGroup;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public CurriculumVersion getCurriculumVersion() { return curriculumVersion; }
    public void setCurriculumVersion(CurriculumVersion curriculumVersion) { this.curriculumVersion = curriculumVersion; }

    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) { this.semester = semester; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public Integer getLectureHours() { return lectureHours; }
    public void setLectureHours(Integer lectureHours) { this.lectureHours = lectureHours; }

    public Integer getTutorialHours() { return tutorialHours; }
    public void setTutorialHours(Integer tutorialHours) { this.tutorialHours = tutorialHours; }

    public Integer getPracticalHours() { return practicalHours; }
    public void setPracticalHours(Integer practicalHours) { this.practicalHours = practicalHours; }

    public boolean isElective() { return elective; }
    public void setElective(boolean elective) { this.elective = elective; }

    public String getElectiveGroup() { return electiveGroup; }
    public void setElectiveGroup(String electiveGroup) { this.electiveGroup = electiveGroup; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

