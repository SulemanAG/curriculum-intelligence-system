package org.curriculumai.orchestration.agent.industry;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public record IndustrySkillsResult(
        String subject,
        String summary,
        List<SkillItem> coreCompetencies,
        List<SkillItem> specializedCompetencies,
        List<ToolchainCategory> productionToolchains,
        List<EmergingTrendItem> emergingTrends,
        List<JobRoleRequirement> jobRoles,
        List<String> evidenceCitations,
        List<String> limitations
) {
    public record SkillItem(
            String name,
            String category,
            String description,
            String proficiencyLevel,
            List<String> typicalTools
    ) {}

    public record ToolchainCategory(
            String categoryName,
            List<String> tools,
            String industryUsage
    ) {}

    public record EmergingTrendItem(
            String trendName,
            String description,
            String timeHorizon,
            String industryImpact
    ) {}

    public record JobRoleRequirement(
            String roleTitle,
            String experienceLevel,
            List<String> requiredSkills,
            List<String> preferredTechnologies
    ) {}

    public Map<String, Object> toMap(ObjectMapper mapper) {
        return mapper.convertValue(this, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
    }
}

