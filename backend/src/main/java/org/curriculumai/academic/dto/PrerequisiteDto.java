package org.curriculumai.academic.dto;

import jakarta.validation.constraints.NotBlank;
import org.curriculumai.academic.domain.CoursePrerequisite;

import java.time.Instant;

public class PrerequisiteDto {

    public record CreatePrerequisiteRequest(
            @NotBlank(message = "Course ID is mandatory")
            String courseId,
            @NotBlank(message = "Prerequisite Course ID is mandatory")
            String prerequisiteCourseId,
            String requirementType,
            String minimumGrade
    ) {}

    public record CoursePrerequisiteResponse(
            String id,
            String courseId,
            String courseCode,
            String courseTitle,
            String prerequisiteCourseId,
            String prerequisiteCourseCode,
            String prerequisiteCourseTitle,
            String requirementType,
            String minimumGrade,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static CoursePrerequisiteResponse fromEntity(CoursePrerequisite cp) {
            return new CoursePrerequisiteResponse(
                    cp.getId(),
                    cp.getCourse().getId(),
                    cp.getCourse().getCode(),
                    cp.getCourse().getTitle(),
                    cp.getPrerequisiteCourse().getId(),
                    cp.getPrerequisiteCourse().getCode(),
                    cp.getPrerequisiteCourse().getTitle(),
                    cp.getRequirementType(),
                    cp.getMinimumGrade(),
                    cp.getCreatedAt(),
                    cp.getUpdatedAt()
            );
        }
    }
}

