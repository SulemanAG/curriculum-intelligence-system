package org.curriculumai.coordinator.dto;

import java.util.List;

public record FinalPlanResponse(
        String id,
        String runId,
        String courseCode,
        String courseTitle,
        String department,
        String institution,
        int semester,
        int credits,
        ContactHoursDto contactHours,
        List<AlternativeResponse.CourseOutcomeDto> courseOutcomes,
        List<AlternativeResponse.CourseModuleDto> modules,
        AssessmentSchemeDto assessmentScheme,
        int version,
        String auditTrailId,
        String generatedDate,
        List<AlternativeResponse.EvidenceReferenceDto> evidenceReferences,
        GovernanceSignOffsDto governanceSignOffs
) {
    public record ContactHoursDto(
            int lecture,
            int tutorial,
            int practical,
            int total
    ) {}

    public record AssessmentSchemeDto(
            Integer ciePercentage,
            Integer seePercentage,
            String evaluationFrameworkName,
            List<ComponentDto> components
    ) {}

    public record ComponentDto(
            String name,
            int weightage,
            String description
    ) {}

    public record GovernanceSignOffsDto(
            String facultyPreparedBy,
            String hodApprovedBy,
            String deanConcurredBy,
            String bosApprovalStatus
    ) {}
}

