package org.curriculumai.orchestration.agent.gap;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public record GapAnalysisResult(
        String subject,
        String summary,
        InstitutionalBaselineSummary institutionalBaseline,
        List<CurricularGap> identifiedGaps,
        double overallCurriculumHealthScore,
        Map<String, Integer> gapSeverityCounts,
        List<String> prerequisiteDeficits,
        List<String> evidenceCitations,
        List<String> limitations
) {
    public record InstitutionalBaselineSummary(
            String courseTitle,
            String courseCode,
            String credits,
            int totalOutcomesRecorded,
            List<String> mappedProgramOutcomes,
            String curriculumStatus
    ) {}

    public record CurricularGap(
            String gapId,
            String category, // e.g., MISSING_TOPIC, OBSOLETE_CONTENT, DEPTH_DEFICIT, LAB_PRACTICAL, PREREQUISITE
            String title,
            String description,
            String severity, // CRITICAL, HIGH, MEDIUM, LOW
            String industryImpact,
            String academicBenchmarkReference,
            String recommendedRemediation
    ) {}

    public Map<String, Object> toMap(ObjectMapper mapper) {
        return mapper.convertValue(this, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
    }
}

