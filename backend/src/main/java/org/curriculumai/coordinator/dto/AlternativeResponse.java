package org.curriculumai.coordinator.dto;

import java.util.List;

public record AlternativeResponse(
        String id,
        String code,
        String title,
        String strategyName,
        String tagline,
        String curriculumPhilosophy,
        int credits,
        int lectureHours,
        int tutorialHours,
        int practicalHours,
        int totalHours,
        List<CourseOutcomeDto> courseOutcomes,
        List<CourseModuleDto> modules,
        List<String> keyHighlights,
        int evidenceCoveragePercent,
        List<EvidenceReferenceDto> evidenceReferences,
        Boolean isRecommendedStrategy
) {
    public record CourseOutcomeDto(
            String code,
            String statement,
            String bloomsTaxonomyLevel,
            List<String> poMapping
    ) {}

    public record CourseModuleDto(
            int moduleNumber,
            String title,
            int suggestedHours,
            List<String> topics,
            List<String> learningOutcomes,
            List<String> supportingEvidenceIds
    ) {}

    public record EvidenceReferenceDto(
            String id,
            String title,
            String publisher,
            Integer publicationYear,
            String clauseOrSection,
            String url,
            String verifiedClaim,
            String retrievalTimestamp
    ) {}
}

