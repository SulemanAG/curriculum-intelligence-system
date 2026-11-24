package org.curriculumai.orchestration.agent.benchmarking;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public record AcademicBenchmarkingResult(
        String subject,
        String summary,
        List<PeerCourseBenchmark> peerCourses,
        List<CurriculumStandardGuideline> standardGuidelines,
        CommonCorePatterns commonCorePatterns,
        List<String> evidenceCitations,
        List<String> limitations
) {
    public record PeerCourseBenchmark(
            String institutionName,
            String courseCode,
            String courseTitle,
            String academicLevel,
            String creditStructure,
            List<String> syllabusModules,
            List<String> prerequisites,
            List<String> referenceTextbooks,
            String sourceUrl
    ) {}

    public record CurriculumStandardGuideline(
            String standardBody,
            String knowledgeArea,
            String guidelineSummary,
            List<String> recommendedCoreTopics,
            List<String> suggestedLabPractices
    ) {}

    public record CommonCorePatterns(
            List<String> universalFoundations,
            List<String> emergingModernAdditions,
            List<String> commonLabParadigms,
            String recommendedPrerequisiteSequence
    ) {}

    public Map<String, Object> toMap(ObjectMapper mapper) {
        return mapper.convertValue(this, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
    }
}

