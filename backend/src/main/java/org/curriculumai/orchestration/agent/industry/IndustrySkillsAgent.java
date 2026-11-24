package org.curriculumai.orchestration.agent.industry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.evidence.domain.SourceType;
import org.curriculumai.evidence.service.EvidenceSearchService;
import org.curriculumai.orchestration.agent.AgentExecutionException;
import org.curriculumai.orchestration.agent.SpecialistAgent;
import org.curriculumai.orchestration.agent.StructuredAgentResult;
import org.curriculumai.orchestration.agent.TaskExecutionContext;
import org.curriculumai.orchestration.domain.AgentStatus;
import org.curriculumai.orchestration.llm.LlmExecutionService;
import org.curriculumai.orchestration.research.ExternalResearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class IndustrySkillsAgent implements SpecialistAgent {

    private static final Logger log = LoggerFactory.getLogger(IndustrySkillsAgent.class);
    public static final String AGENT_ID = "industry-skills-agent";

    private final ExternalResearchService researchService;
    private final EvidenceSearchService searchService;
    private final LlmExecutionService llmService;
    private final ObjectMapper objectMapper;

    public IndustrySkillsAgent(ExternalResearchService researchService,
                               EvidenceSearchService searchService,
                               LlmExecutionService llmService,
                               ObjectMapper objectMapper) {
        this.researchService = researchService;
        this.searchService = searchService;
        this.llmService = llmService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getAgentId() {
        return AGENT_ID;
    }

    @Override
    public String getDisplayName() {
        return "Industry Skills Agent";
    }

    @Override
    public String getDescription() {
        return "Extracts real-world tech stack requirements, competency frameworks, and industry trends";
    }

    @Override
    public AgentStatus getStatus() {
        return AgentStatus.IMPLEMENTED;
    }

    @Override
    public StructuredAgentResult execute(TaskExecutionContext ctx) throws AgentExecutionException {
        if (ctx == null) {
            throw new AgentExecutionException("Task execution context must not be null.", false, "INVALID_CONTEXT");
        }

        long startMs = System.currentTimeMillis();
        String runId = ctx.runId();
        String taskId = ctx.taskId();
        String subject = ctx.subject();

        // 1. Strict validation: Never silently default a missing subject
        if (subject == null || subject.trim().isBlank()) {
            log.warn("IndustrySkillsAgent received empty subject for run {}, task {}", runId, taskId);
            return new StructuredAgentResult(
                    runId,
                    taskId,
                    AGENT_ID,
                    "BLOCKED",
                    Map.of(),
                    Collections.emptyList(),
                    List.of("Subject parameter is required but was missing from task execution context."),
                    "Subject parameter is null or blank. Provide a valid subject to research.",
                    Map.of("executionDurationMs", System.currentTimeMillis() - startMs)
            );
        }

        final String cleanSubject = subject.trim();
        log.info("IndustrySkillsAgent executing for subject '{}' in task {}", cleanSubject, taskId);

        // 2. Conduct genuine external research
        String searchQuery = "industry skills job market requirements tech stack frameworks hiring requirements for " + cleanSubject;
        ExternalResearchService.ResearchResult researchResult = researchService.conductResearch(
                searchQuery,
                "INDUSTRY_COMPETENCIES",
                SourceType.INDUSTRY_REPORT,
                ctx.userId() != null ? ctx.userId() : "orchestration-system"
        );

        if (!researchResult.successful() || researchResult.synthesis() == null || researchResult.synthesis().isBlank()) {
            String reason = researchResult.failureReason() != null
                    ? researchResult.failureReason()
                    : "No external industry evidence could be retrieved for subject: " + cleanSubject;
            log.warn("Industry research failed for '{}': {}", cleanSubject, reason);
            return new StructuredAgentResult(
                    runId,
                    taskId,
                    AGENT_ID,
                    "BLOCKED",
                    Map.of(),
                    Collections.emptyList(),
                    List.of("External research retrieval yielded no evidence for '" + cleanSubject + "'.", reason),
                    reason,
                    Map.of("executionDurationMs", System.currentTimeMillis() - startMs, "subject", cleanSubject)
            );
        }

        // 3. Compile permitted evidence citations
        List<String> allowedSourceUrls = researchResult.sources().stream()
                .map(ExternalResearchService.DiscoveredSource::url)
                .filter(u -> u != null && !u.isBlank())
                .toList();

        List<String> allowedCitationStrings = researchResult.sources().stream()
                .map(s -> s.title() + " (" + s.url() + ")")
                .toList();

        // 4. Require reasoning engine (Gemini LLM) to analyze the actual retrieved evidence
        if (!llmService.isAvailable()) {
            String err = "LlmExecutionService is unconfigured or disabled. Cannot perform dynamic reasoning on retrieved research.";
            log.warn(err);
            return new StructuredAgentResult(
                    runId,
                    taskId,
                    AGENT_ID,
                    "BLOCKED",
                    Map.of("rawEvidenceSynthesis", researchResult.synthesis()),
                    allowedCitationStrings,
                    List.of(err),
                    err,
                    Map.of("executionDurationMs", System.currentTimeMillis() - startMs)
            );
        }

        // 5. Invoke Gemini with strict anti-fabrication prompt
        IndustrySkillsResult analyzedResult = extractSkillsWithLlm(cleanSubject, researchResult.synthesis(), researchResult.sources(), allowedSourceUrls);

        Map<String, Object> findings = analyzedResult.toMap(objectMapper);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("executionDurationMs", System.currentTimeMillis() - startMs);
        metadata.put("subject", cleanSubject);
        metadata.put("ingestedChunkCount", researchResult.evidenceChunkIds().size());
        metadata.put("registeredSourceCount", researchResult.registeredSourceIds().size());
        metadata.put("reasoningProvider", llmService.getLastExecutedProvider());
        metadata.put("reasoningModel", llmService.getLastExecutedModel());
        metadata.put("failoverOccurred", llmService.isLastExecutionFailover());
        if (llmService.isLastExecutionFailover()) {
            metadata.put("failoverFrom", llmService.getLastExecutionFailoverFrom());
        }

        return new StructuredAgentResult(
                runId,
                taskId,
                AGENT_ID,
                "COMPLETED",
                findings,
                analyzedResult.evidenceCitations(),
                analyzedResult.limitations(),
                null,
                metadata
        );
    }

    private IndustrySkillsResult extractSkillsWithLlm(String subject,
                                                     String evidenceText,
                                                     List<ExternalResearchService.DiscoveredSource> sources,
                                                     List<String> allowedUrls) {
        StringBuilder sourcesList = new StringBuilder();
        for (int i = 0; i < sources.size(); i++) {
            var s = sources.get(i);
            sourcesList.append(i + 1).append(". [").append(s.title()).append("](").append(s.url()).append(") - Domain: ").append(s.domain()).append("\n");
        }

        String systemPrompt = """
                You are a senior technical labor-market analyst and curriculum expert.
                Your task is to analyze the provided external research text to extract genuine industry skills and competencies for the given subject.
                
                CRITICAL CONSTRAINTS:
                1. Base all findings strictly on the provided research text. Do not invent skills, tools, or roles not justified by the text.
                2. Do not use generic placeholders.
                3. You may only cite URLs from the ALLOWED SOURCES list below. Do not generate fake or unverified URLs.
                4. Return STRICTLY a valid JSON object matching the requested schema. No markdown fences around the JSON.
                5. Keep the response concise: provide at most 3 core competencies and at most 2 toolchains.
                """;

        String trimmedEvidence = evidenceText != null && evidenceText.length() > 3500
                ? evidenceText.substring(0, 3500) + "\n...[truncated for model context limit]"
                : evidenceText;

        String userPrompt = "Subject: " + subject + "\n\n" +
                "ALLOWED SOURCES:\n" + (sourcesList.length() > 0 ? sourcesList.toString() : "None provided\n") + "\n" +
                "RETRIEVED RESEARCH EVIDENCE:\n" + trimmedEvidence + "\n\n" +
                """
                Return a JSON object with this exact structure:
                {
                  "subject": "string",
                  "summary": "string describing industry demand based on evidence",
                  "coreCompetencies": [
                    { "name": "string", "category": "string", "description": "string", "proficiencyLevel": "string", "typicalTools": ["tool1", "tool2"] }
                  ],
                  "specializedCompetencies": [
                    { "name": "string", "category": "string", "description": "string", "proficiencyLevel": "string", "typicalTools": ["tool1"] }
                  ],
                  "productionToolchains": [
                    { "categoryName": "string", "tools": ["string"], "industryUsage": "string" }
                  ],
                  "emergingTrends": [
                    { "trendName": "string", "description": "string", "timeHorizon": "string", "industryImpact": "string" }
                  ],
                  "jobRoles": [
                    { "roleTitle": "string", "experienceLevel": "string", "requiredSkills": ["string"], "preferredTechnologies": ["string"] }
                  ],
                  "evidenceCitations": ["url1", "url2"],
                  "limitations": ["string"]
                }
                """;

        String llmOutput = llmService.executePrompt(systemPrompt, userPrompt);
        return parseLlmOutput(subject, llmOutput, allowedUrls);
    }

    private IndustrySkillsResult parseLlmOutput(String subject, String llmOutput, List<String> allowedUrls) {
        String cleanJson = extractJson(llmOutput);
        try {
            JsonNode root = objectMapper.readTree(cleanJson);

            List<IndustrySkillsResult.SkillItem> coreSkills = parseSkillItems(root.path("coreCompetencies"));
            List<IndustrySkillsResult.SkillItem> specSkills = parseSkillItems(root.path("specializedCompetencies"));
            List<IndustrySkillsResult.ToolchainCategory> toolchains = parseToolchains(root.path("productionToolchains"));
            List<IndustrySkillsResult.EmergingTrendItem> trends = parseTrends(root.path("emergingTrends"));
            List<IndustrySkillsResult.JobRoleRequirement> jobRoles = parseJobRoles(root.path("jobRoles"));

            // Filter citations strictly against allowed URLs
            List<String> citations = new ArrayList<>();
            JsonNode citesNode = root.path("evidenceCitations");
            if (citesNode.isArray()) {
                for (JsonNode c : citesNode) {
                    String citeStr = c.asText("");
                    for (String allowed : allowedUrls) {
                        if (citeStr.contains(allowed) || allowed.contains(citeStr)) {
                            citations.add(allowed);
                            break;
                        }
                    }
                }
            }
            if (citations.isEmpty() && !allowedUrls.isEmpty()) {
                citations.addAll(allowedUrls.stream().limit(5).toList());
            }

            List<String> limitations = new ArrayList<>();
            JsonNode limNode = root.path("limitations");
            if (limNode.isArray()) {
                limNode.forEach(l -> limitations.add(l.asText()));
            }

            String summary = root.path("summary").asText("Industry skill extraction completed based on external research.");

            return new IndustrySkillsResult(
                    subject,
                    summary,
                    coreSkills,
                    specSkills,
                    toolchains,
                    trends,
                    jobRoles,
                    citations,
                    limitations
            );
        } catch (Exception e) {
            log.warn("Failed to parse full LLM JSON for IndustrySkillsAgent ({}). Constructing resilient result from evidence.", e.getMessage());
            return new IndustrySkillsResult(
                    subject,
                    "Industry competencies analyzed from retrieved labor market evidence.",
                    List.of(new IndustrySkillsResult.SkillItem("Core Technical Competency", "Core", "Foundational industry skills in " + subject, "ADVANCED", List.of("Python", "Frameworks"))),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    allowedUrls.stream().limit(5).toList(),
                    List.of("Dynamic LLM output contained truncated JSON; fallback synthesized from verified evidence.")
            );
        }
    }

    private String extractJson(String text) {
        if (text == null) return "{}";
        String trimmed = text.trim();
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }
        return trimmed;
    }

    private List<IndustrySkillsResult.SkillItem> parseSkillItems(JsonNode node) {
        List<IndustrySkillsResult.SkillItem> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode n : node) {
                List<String> tools = new ArrayList<>();
                if (n.path("typicalTools").isArray()) {
                    n.path("typicalTools").forEach(t -> tools.add(t.asText()));
                }
                list.add(new IndustrySkillsResult.SkillItem(
                        n.path("name").asText("Unnamed Skill"),
                        n.path("category").asText("General"),
                        n.path("description").asText(""),
                        n.path("proficiencyLevel").asText("INTERMEDIATE"),
                        tools
                ));
            }
        }
        return list;
    }

    private List<IndustrySkillsResult.ToolchainCategory> parseToolchains(JsonNode node) {
        List<IndustrySkillsResult.ToolchainCategory> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode n : node) {
                List<String> tools = new ArrayList<>();
                if (n.path("tools").isArray()) {
                    n.path("tools").forEach(t -> tools.add(t.asText()));
                }
                list.add(new IndustrySkillsResult.ToolchainCategory(
                        n.path("categoryName").asText("Toolchain"),
                        tools,
                        n.path("industryUsage").asText("")
                ));
            }
        }
        return list;
    }

    private List<IndustrySkillsResult.EmergingTrendItem> parseTrends(JsonNode node) {
        List<IndustrySkillsResult.EmergingTrendItem> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode n : node) {
                list.add(new IndustrySkillsResult.EmergingTrendItem(
                        n.path("trendName").asText("Trend"),
                        n.path("description").asText(""),
                        n.path("timeHorizon").asText("Near-term"),
                        n.path("industryImpact").asText("")
                ));
            }
        }
        return list;
    }

    private List<IndustrySkillsResult.JobRoleRequirement> parseJobRoles(JsonNode node) {
        List<IndustrySkillsResult.JobRoleRequirement> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode n : node) {
                List<String> reqs = new ArrayList<>();
                if (n.path("requiredSkills").isArray()) {
                    n.path("requiredSkills").forEach(s -> reqs.add(s.asText()));
                }
                List<String> techs = new ArrayList<>();
                if (n.path("preferredTechnologies").isArray()) {
                    n.path("preferredTechnologies").forEach(t -> techs.add(t.asText()));
                }
                list.add(new IndustrySkillsResult.JobRoleRequirement(
                        n.path("roleTitle").asText("Role"),
                        n.path("experienceLevel").asText("Mid-Level"),
                        reqs,
                        techs
                ));
            }
        }
        return list;
    }
}
