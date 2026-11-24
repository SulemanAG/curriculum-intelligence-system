package org.curriculumai.orchestration.agent.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public record RecommendationResult(
        String subject,
        String summary,
        List<CoursePlanAlternative> coursePlanAlternatives,
        ComparisonMatrix comparisonMatrix,
        List<String> governanceRecommendations,
        List<String> evidenceCitations,
        List<String> limitations
) {
    public record CoursePlanAlternative(
            String planId,
            String planTitle,
            String targetOrientation, // INDUSTRY, ACADEMIC_DEPTH, BALANCED, EMERGING_RESEARCH
            String creditStructure, // L-T-P-C
            int totalLectureHours,
            int totalPracticalHours,
            String pedagogicalPhilosophy,
            List<CourseModule> modules,
            List<CourseOutcomeItem> courseOutcomes,
            List<LabExperimentItem> laboratoryWork,
            List<TextbookItem> referenceMaterials,
            String idealStudentProfile
    ) {}

    public record CourseModule(
            int unitNumber,
            String unitTitle,
            int allocatedHours,
            List<String> detailedTopics,
            String learningObjective
    ) {}

    public record CourseOutcomeItem(
            String outcomeCode,
            String outcomeStatement,
            String bloomTaxonomyLevel, // REMEMBER, UNDERSTAND, APPLY, ANALYZE, EVALUATE, CREATE
            List<String> mappedProgramOutcomes
    ) {}

    public record LabExperimentItem(
            int experimentNumber,
            String title,
            String description,
            String toolstack,
            String expectedDeliverable
    ) {}

    public record TextbookItem(
            String title,
            String authors,
            String publisher,
            String editionOrYear,
            String referenceType, // TEXTBOOK, REFERENCE, RESEARCH_MONOGRAPH
            String citationUrl
    ) {}

    public record ComparisonMatrix(
            List<String> criteria,
            List<PlanScoreRow> planScores
    ) {}

    public record PlanScoreRow(
            String planId,
            String planTitle,
            Map<String, String> evaluationCriteriaScores
    ) {}

    public Map<String, Object> toMap(ObjectMapper mapper) {
        return mapper.convertValue(this, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
    }
}

