package org.curriculumai.academic.dto;

import jakarta.validation.constraints.NotBlank;
import org.curriculumai.academic.domain.Course;

import java.time.Instant;

public class CourseDto {

    public record CreateCourseRequest(
            @NotBlank(message = "Department ID is mandatory")
            String departmentId,
            @NotBlank(message = "Course code is mandatory")
            String code,
            @NotBlank(message = "Course title is mandatory")
            String title,
            String description,
            String courseType,
            Integer defaultCredits,
            Integer lectureHours,
            Integer tutorialHours,
            Integer practicalHours
    ) {}

    public record CourseResponse(
            String id,
            String departmentId,
            String departmentName,
            String code,
            String title,
            String description,
            String courseType,
            Integer defaultCredits,
            Integer lectureHours,
            Integer tutorialHours,
            Integer practicalHours,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static CourseResponse fromEntity(Course course) {
            return new CourseResponse(
                    course.getId(),
                    course.getDepartment().getId(),
                    course.getDepartment().getName(),
                    course.getCode(),
                    course.getTitle(),
                    course.getDescription(),
                    course.getCourseType(),
                    course.getDefaultCredits(),
                    course.getLectureHours(),
                    course.getTutorialHours(),
                    course.getPracticalHours(),
                    course.getCreatedAt(),
                    course.getUpdatedAt()
            );
        }
    }
}

