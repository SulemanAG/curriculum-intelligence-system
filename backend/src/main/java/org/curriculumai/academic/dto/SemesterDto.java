package org.curriculumai.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.curriculumai.academic.domain.Semester;

import java.time.Instant;

public class SemesterDto {

    public record CreateSemesterRequest(
            @NotBlank(message = "Curriculum Version ID is mandatory")
            String curriculumVersionId,
            @NotNull(message = "Semester number is mandatory (e.g. 1..8)")
            Integer semesterNumber,
            @NotBlank(message = "Semester label is mandatory (e.g. Semester 1)")
            String label,
            Integer creditsTarget
    ) {}

    public record SemesterResponse(
            String id,
            String curriculumVersionId,
            int semesterNumber,
            String label,
            Integer creditsTarget,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static SemesterResponse fromEntity(Semester s) {
            return new SemesterResponse(
                    s.getId(),
                    s.getCurriculumVersion().getId(),
                    s.getSemesterNumber(),
                    s.getLabel(),
                    s.getCreditsTarget(),
                    s.getCreatedAt(),
                    s.getUpdatedAt()
            );
        }
    }
}

