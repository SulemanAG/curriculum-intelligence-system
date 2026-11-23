package org.curriculumai.academic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.curriculumai.academic.domain.CoPoMapping;
import org.curriculumai.academic.domain.CourseOutcome;
import org.curriculumai.academic.domain.ProgrammeOutcome;

import java.time.Instant;

public class OutcomeDto {

    public record CreateProgrammeOutcomeRequest(
            @NotBlank(message = "Programme ID is mandatory")
            String programmeId,
            @NotBlank(message = "Outcome code is mandatory (e.g. PO1, PSO1)")
            String code,
            @NotBlank(message = "Title is mandatory")
            String title,
            @NotBlank(message = "Description is mandatory")
            String description,
            String outcomeType
    ) {}

    public record ProgrammeOutcomeResponse(
            String id,
            String programmeId,
            String code,
            String title,
            String description,
            String outcomeType,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static ProgrammeOutcomeResponse fromEntity(ProgrammeOutcome po) {
            return new ProgrammeOutcomeResponse(
                    po.getId(),
                    po.getProgramme().getId(),
                    po.getCode(),
                    po.getTitle(),
                    po.getDescription(),
                    po.getOutcomeType(),
                    po.getCreatedAt(),
                    po.getUpdatedAt()
            );
        }
    }

    public record CreateCourseOutcomeRequest(
            @NotBlank(message = "Course ID is mandatory")
            String courseId,
            @NotBlank(message = "CO code is mandatory (e.g. CO1)")
            String code,
            @NotBlank(message = "Description is mandatory")
            String description,
            String bloomLevel
    ) {}

    public record CourseOutcomeResponse(
            String id,
            String courseId,
            String code,
            String description,
            String bloomLevel,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static CourseOutcomeResponse fromEntity(CourseOutcome co) {
            return new CourseOutcomeResponse(
                    co.getId(),
                    co.getCourse().getId(),
                    co.getCode(),
                    co.getDescription(),
                    co.getBloomLevel(),
                    co.getCreatedAt(),
                    co.getUpdatedAt()
            );
        }
    }

    public record CreateCoPoMappingRequest(
            @NotBlank(message = "Course Outcome ID is mandatory")
            String courseOutcomeId,
            @NotBlank(message = "Programme Outcome ID is mandatory")
            String programmeOutcomeId,
            @NotNull(message = "Mapping level is mandatory")
            @Min(value = 1, message = "Mapping level must be 1, 2, or 3")
            @Max(value = 3, message = "Mapping level must be 1, 2, or 3")
            Integer mappingLevel,
            String justification
    ) {}

    public record CoPoMappingResponse(
            String id,
            String courseOutcomeId,
            String courseOutcomeCode,
            String programmeOutcomeId,
            String programmeOutcomeCode,
            int mappingLevel,
            String justification,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static CoPoMappingResponse fromEntity(CoPoMapping map) {
            return new CoPoMappingResponse(
                    map.getId(),
                    map.getCourseOutcome().getId(),
                    map.getCourseOutcome().getCode(),
                    map.getProgrammeOutcome().getId(),
                    map.getProgrammeOutcome().getCode(),
                    map.getMappingLevel(),
                    map.getJustification(),
                    map.getCreatedAt(),
                    map.getUpdatedAt()
            );
        }
    }

    public record UpdateCourseOutcomeRequest(
            String description,
            String bloomLevel
    ) {}

    public record UpdateProgrammeOutcomeRequest(
            String title,
            String description,
            String outcomeType
    ) {}

    public record CurriculumOutcomeSnapshotResponse(
            String id,
            String curriculumVersionId,
            String snapshotType,
            String sourceEntityId,
            String courseId,
            String courseCode,
            String outcomeCode,
            String title,
            String description,
            String bloomLevel,
            String targetOutcomeCode,
            Integer mappingLevel,
            String justification,
            Instant createdAt
    ) {
        public static CurriculumOutcomeSnapshotResponse fromEntity(org.curriculumai.academic.domain.CurriculumOutcomeSnapshot s) {
            return new CurriculumOutcomeSnapshotResponse(
                    s.getId(),
                    s.getCurriculumVersion().getId(),
                    s.getSnapshotType(),
                    s.getSourceEntityId(),
                    s.getCourseId(),
                    s.getCourseCode(),
                    s.getOutcomeCode(),
                    s.getTitle(),
                    s.getDescription(),
                    s.getBloomLevel(),
                    s.getTargetOutcomeCode(),
                    s.getMappingLevel(),
                    s.getJustification(),
                    s.getCreatedAt()
            );
        }
    }
}

