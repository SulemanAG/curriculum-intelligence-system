package org.curriculumai.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;

public class ImportDto {

    public record CourseImportItem(
            @NotBlank(message = "Department code is mandatory")
            String departmentCode,
            @NotBlank(message = "Course code is mandatory")
            String courseCode,
            @NotBlank(message = "Title is mandatory")
            String title,
            String description,
            String courseType,
            Integer credits,
            Integer lectureHours,
            Integer tutorialHours,
            Integer practicalHours
    ) {}

    public record AcademicImportRequest(
            @NotBlank(message = "Format must be JSON or CSV")
            String format,
            @NotBlank(message = "Institution code is mandatory")
            String institutionCode,
            @NotEmpty(message = "Courses list must not be empty")
            List<CourseImportItem> courses
    ) {}

    public record ImportPreviewResponse(
            String batchId,
            String status,
            int totalRecords,
            int validRecords,
            int errorRecords,
            List<String> validationErrors,
            List<CourseImportItem> validItems
    ) {}

    public record CommitImportResponse(
            String batchId,
            String status,
            int committedRecords,
            Instant committedAt,
            String message
    ) {}
}

