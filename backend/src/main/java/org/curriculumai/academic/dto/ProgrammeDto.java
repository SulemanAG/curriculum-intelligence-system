package org.curriculumai.academic.dto;

import jakarta.validation.constraints.NotBlank;
import org.curriculumai.academic.domain.Programme;

import java.time.Instant;

public class ProgrammeDto {

    public record CreateProgrammeRequest(
            @NotBlank(message = "Department ID is mandatory")
            String departmentId,
            @NotBlank(message = "Programme code is mandatory")
            String code,
            @NotBlank(message = "Programme name is mandatory")
            String name,
            String degreeLevel,
            Integer durationYears,
            Integer totalCreditsRequired
    ) {}

    public record ProgrammeResponse(
            String id,
            String departmentId,
            String departmentName,
            String code,
            String name,
            String degreeLevel,
            Integer durationYears,
            Integer totalCreditsRequired,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static ProgrammeResponse fromEntity(Programme prog) {
            return new ProgrammeResponse(
                    prog.getId(),
                    prog.getDepartment().getId(),
                    prog.getDepartment().getName(),
                    prog.getCode(),
                    prog.getName(),
                    prog.getDegreeLevel(),
                    prog.getDurationYears(),
                    prog.getTotalCreditsRequired(),
                    prog.getCreatedAt(),
                    prog.getUpdatedAt()
            );
        }
    }
}

