package org.curriculumai.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.curriculumai.academic.domain.CurriculumVersion;

import java.time.Instant;

public class CurriculumVersionDto {

    public record CreateCurriculumVersionRequest(
            @NotBlank(message = "Programme ID is mandatory")
            String programmeId,
            @NotBlank(message = "Version code is mandatory (e.g. 2024-V1)")
            String versionCode,
            @NotNull(message = "Effective year is mandatory")
            Integer effectiveYear,
            Integer totalCredits,
            String sourceProvenance
    ) {}

    public record TransitionStatusRequest(
            @NotBlank(message = "Target status is mandatory (DRAFT, UNDER_REVIEW, APPROVED, ARCHIVED)")
            String targetStatus,
            String resolutionNumber,
            String approvalNotes
    ) {}

    public record CloneDraftRequest(
            @NotBlank(message = "New version code is mandatory")
            String newVersionCode,
            @NotNull(message = "New effective year is mandatory")
            Integer newEffectiveYear
    ) {}

    public record CurriculumVersionResponse(
            String id,
            String programmeId,
            String programmeName,
            String versionCode,
            int effectiveYear,
            Integer totalCredits,
            String lifecycleStatus,
            boolean isImmutable,
            boolean isActive,
            String approvalMetadataJson,
            String approvedBy,
            Instant approvedAt,
            String createdBy,
            String sourceProvenance,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static CurriculumVersionResponse fromEntity(CurriculumVersion cv) {
            return new CurriculumVersionResponse(
                    cv.getId(),
                    cv.getProgramme().getId(),
                    cv.getProgramme().getName(),
                    cv.getVersionCode(),
                    cv.getEffectiveYear(),
                    cv.getTotalCredits(),
                    cv.getLifecycleStatus().name(),
                    cv.isImmutable(),
                    cv.isActive(),
                    cv.getApprovalMetadataJson(),
                    cv.getApprovedBy() != null ? cv.getApprovedBy().getUsername() : null,
                    cv.getApprovedAt(),
                    cv.getCreatedBy() != null ? cv.getCreatedBy().getUsername() : null,
                    cv.getSourceProvenance(),
                    cv.getCreatedAt(),
                    cv.getUpdatedAt()
            );
        }
    }
}

