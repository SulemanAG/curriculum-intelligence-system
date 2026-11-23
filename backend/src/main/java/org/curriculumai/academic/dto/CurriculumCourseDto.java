package org.curriculumai.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.curriculumai.academic.domain.CurriculumCourse;

import java.time.Instant;

public class CurriculumCourseDto {

    public record AddCourseToSemesterRequest(
            @NotBlank(message = "Curriculum Version ID is mandatory")
            String curriculumVersionId,
            @NotBlank(message = "Semester ID is mandatory")
            String semesterId,
            @NotBlank(message = "Course ID is mandatory")
            String courseId,
            Integer credits,
            Integer lectureHours,
            Integer tutorialHours,
            Integer practicalHours,
            Boolean isElective,
            String electiveGroup
    ) {}

    public record CurriculumCourseResponse(
            String id,
            String curriculumVersionId,
            String semesterId,
            int semesterNumber,
            String courseId,
            String courseCode,
            String courseTitle,
            String courseType,
            Integer credits,
            Integer lectureHours,
            Integer tutorialHours,
            Integer practicalHours,
            boolean isElective,
            String electiveGroup,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static CurriculumCourseResponse fromEntity(CurriculumCourse cc) {
            return new CurriculumCourseResponse(
                    cc.getId(),
                    cc.getCurriculumVersion().getId(),
                    cc.getSemester().getId(),
                    cc.getSemester().getSemesterNumber(),
                    cc.getCourse().getId(),
                    cc.getCourseCode(),
                    cc.getCourseTitle(),
                    cc.getCourseType(),
                    cc.getCredits(),
                    cc.getLectureHours(),
                    cc.getTutorialHours(),
                    cc.getPracticalHours(),
                    cc.isElective(),
                    cc.getElectiveGroup(),
                    cc.getCreatedAt(),
                    cc.getUpdatedAt()
            );
        }
    }
}

