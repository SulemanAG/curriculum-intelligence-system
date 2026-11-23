package org.curriculumai.academic.dto;

import jakarta.validation.constraints.NotBlank;
import org.curriculumai.academic.domain.Department;

import java.time.Instant;

public class DepartmentDto {

    public record CreateDepartmentRequest(
            @NotBlank(message = "Institution ID is mandatory")
            String institutionId,
            @NotBlank(message = "Department code is mandatory")
            String code,
            @NotBlank(message = "Department name is mandatory")
            String name
    ) {}

    public record DepartmentResponse(
            String id,
            String institutionId,
            String institutionName,
            String code,
            String name,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static DepartmentResponse fromEntity(Department dept) {
            return new DepartmentResponse(
                    dept.getId(),
                    dept.getInstitution().getId(),
                    dept.getInstitution().getName(),
                    dept.getCode(),
                    dept.getName(),
                    dept.getCreatedAt(),
                    dept.getUpdatedAt()
            );
        }
    }
}

