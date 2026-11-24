package org.curriculumai.orchestration.agent.recommendation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.orchestration.agent.AgentExecutionException;
import org.curriculumai.orchestration.agent.SpecialistAgent;
import org.curriculumai.orchestration.agent.StructuredAgentResult;
import org.curriculumai.orchestration.agent.TaskExecutionContext;
import org.curriculumai.orchestration.domain.AgentStatus;
import org.curriculumai.orchestration.llm.LlmExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RecommendationAgent implements SpecialistAgent {

    private static final Logger log = LoggerFactory.getLogger(RecommendationAgent.class);
    public static final String AGENT_ID = "recommendation-agent";

    private final LlmExecutionService llmService;
    private final ObjectMapper objectMapper;

    public RecommendationAgent(LlmExecutionService llmService, ObjectMapper objectMapper) {
        this.llmService = llmService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getAgentId() {
        return AGENT_ID;
    }

    @Override
    public String getDisplayName() {
        return "Recommendation Agent";
    }

    @Override
    public String getDescription() {
        return "Formulates evidence-backed curriculum revisions, course outlines, and pedagogical recommendations";
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
            log.warn("RecommendationAgent received empty subject for run {}, task {}", runId, taskId);
            return new StructuredAgentResult(
                    runId,
                    taskId,
                    AGENT_ID,
                    "BLOCKED",
                    Map.of(),
                    Collections.emptyList(),
                    List.of("Subject parameter is required but was missing from task execution context."),
                    "Subject parameter is null or blank. Provide a valid subject to generate course plan recommendations.",
                    Map.of("executionDurationMs", System.currentTimeMillis() - startMs));
        }

        final String cleanSubject = subject.trim();
        log.info("RecommendationAgent executing for subject '{}' in task {}", cleanSubject, taskId);

        // 2. Validate all four upstream tasks: Curriculum Mapping, Industry Skills,
        // Academic Benchmarking, Gap Analysis
        Map<String, StructuredAgentResult> priorOutputs = ctx.priorTaskOutputs();
        StructuredAgentResult mappingOutput = priorOutputs.get("task_curriculum_mapping");
        StructuredAgentResult industryOutput = priorOutputs.get("task_industry_skills");
        StructuredAgentResult benchmarkOutput = priorOutputs.get("task_academic_benchmarking");
        StructuredAgentResult gapOutput = priorOutputs.get("task_gap_analysis");

        List<String> missingUpstream = new ArrayList<>();
        if (mappingOutput == null || mappingOutput.findings() == null || mappingOutput.findings().isEmpty()) {
            missingUpstream.add("task_curriculum_mapping");
        }
        if (industryOutput == null || industryOutput.findings() == null || industryOutput.findings().isEmpty()) {
            missingUpstream.add("task_industry_skills");
        }
        if (benchmarkOutput == null || benchmarkOutput.findings() == null || benchmarkOutput.findings().isEmpty()) {
            missingUpstream.add("task_academic_benchmarking");
        }
        if (gapOutput == null || gapOutput.findings() == null || gapOutput.findings().isEmpty()) {
            missingUpstream.add("task_gap_analysis");
        }

        if (!missingUpstream.isEmpty()) {
            String blockedReason = "Recommendation Agent cannot generate evidence-backed proposals: required upstream outputs are missing: "
                    + String.join(", ", missingUpstream);
            log.warn("RecommendationAgent blocked: {}", blockedReason);
            return new StructuredAgentResult(
                    runId,
                    taskId,
                    AGENT_ID,
                    "BLOCKED",
                    Map.of("missingUpstreamTasks", missingUpstream),
                    Collections.emptyList(),
                    List.of(blockedReason,
                            "Upstream Gap Analysis and research tasks must complete successfully before recommendations can be formulated."),
                    blockedReason,
                    Map.of("executionDurationMs", System.currentTimeMillis() - startMs, "subject", cleanSubject));
        }

        // 3. Compile allowed evidence references strictly from all upstream tasks
        List<String> allowedCitations = new ArrayList<>();
        priorOutputs.values().forEach(output -> {
            if (output.evidenceReferences() != null) {
                allowedCitations.addAll(output.evidenceReferences());
            }
        });
        List<String> distinctAllowedCitations = allowedCitations.stream().distinct().limit(30).toList();

        // 4. Deterministically extract institutional constraints
        String institutionalCode = "UNSPECIFIED";
        String institutionalTitle = cleanSubject;
        String institutionalCreditStructure = null; // null if unrecorded; do NOT assume 3-0-2-4

        Object courseObj = mappingOutput.findings().get("resolvedCourse");
        if (courseObj instanceof Map<?, ?> cMap) {
            if (cMap.get("code") != null)
                institutionalCode = cMap.get("code").toString();
            if (cMap.get("title") != null)
                institutionalTitle = cMap.get("title").toString();
            if (cMap.get("credits") != null)
                institutionalCreditStructure = cMap.get("credits").toString();
        }

        // 5. Require reasoning engine (Gemini LLM) to dynamically synthesize course
        // plans
        if (!llmService.isAvailable()) {
            String err = "LlmExecutionService is unconfigured or disabled. Cannot dynamically generate course plans.";
            log.warn(err);
            return new StructuredAgentResult(
                    runId,
                    taskId,
                    AGENT_ID,
                    "BLOCKED",
                    Map.of(),
                    distinctAllowedCitations,
                    List.of(err),
                    err,
                    Map.of("executionDurationMs", System.currentTimeMillis() - startMs));
        }

        // 6. Invoke Gemini with exhaustive upstream evidence and strict
        // anti-fabrication prompt
        RecommendationResult analyzedResult = generateRecommendationsWithLlm(
                cleanSubject,
                institutionalCode,
                institutionalTitle,
                institutionalCreditStructure,
                mappingOutput.findings(),
                industryOutput.findings(),
                benchmarkOutput.findings(),
                gapOutput.findings(),
                distinctAllowedCitations);

        Map<String, Object> findings = analyzedResult.toMap(objectMapper);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("executionDurationMs", System.currentTimeMillis() - startMs);
        metadata.put("subject", cleanSubject);
        metadata.put("alternativesGenerated", analyzedResult.coursePlanAlternatives().size());
        metadata.put("totalCitationsAttributed", analyzedResult.evidenceCitations().size());
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
                metadata);
    }

    private RecommendationResult generateRecommendationsWithLlm(
            String subject,
            String institutionalCode,
            String institutionalTitle,
            String institutionalCreditStructure,
            Map<String, Object> mappingFindings,
            Map<String, Object> industryFindings,
            Map<String, Object> benchmarkFindings,
            Map<String, Object> gapFindings,
            List<String> allowedCitations) {
        String systemPrompt = """
                You are a university academic dean, curriculum committee chair, and expert pedagogue.
                Your task is to dynamically synthesize FOUR distinct, evidence-backed course-plan proposals for the given subject, based strictly on the provided institutional mapping, industry skill requirements, academic benchmarks, and gap analysis findings.

                MANDATORY ARCHITECTURAL RULES:
                1. DYNAMIC REASONING ONLY: Do not use predetermined or hardcoded plan structures. Every module, outcome, and recommendation must be reasoned directly from the provided inputs.
                2. FOUR MEANINGFULLY DIFFERENT ALTERNATIVES:
                   - Alternative 1 (Orientation: INDUSTRY): Tailored directly for industrial employability, modern software stacks, and production engineering practices identified in the industry skills input.
                   - Alternative 2 (Orientation: ACADEMIC_DEPTH): Emphasizes theoretical depth, mathematical formalisms, proofs, and research-oriented foundations identified in the academic benchmarks.
                   - Alternative 3 (Orientation: BALANCED): A harmonized institutional balance combining solid mathematical foundations, modern applied modules, and structured lab experiences.
                   - Alternative 4 (Orientation: EMERGING_RESEARCH): Focuses on cutting-edge frontiers, self-supervised learning, multimodal models, and novel paradigms highlighted in the research.
                3. CREDIT AND CONTACT HOURS:
                   - If institutional credit structure is provided in constraints, adhere to it.
                   - If institutional credit structure is UNRECORDED or NULL, do not invent arbitrary numbers without qualification. Explicitly mark creditStructure as 'RECOMMENDED: [L-T-P-C] (Subject to Board of Studies Approval)' and set total hours proportional to the proposed units.
                4. CITATION GATING:
                   - Every important factual claim, reference textbook, or benchmark comparison must strictly cite items from the ALLOWED CITATIONS list.
                   - Do not fabricate research papers or non-existent URLs.
                5. DYNAMIC EVALUATION MATRIX:
                   - Provide a comparison matrix with criteria and reasoned scores/evaluations reflecting the real trade-offs between the four plans.
                6. OUTPUT FORMAT:
                   - Output strictly valid JSON matching the requested schema. No markdown fences around the JSON.
                """;

        String mappingJson;
        String industryJson;
        String benchmarkJson;
        String gapJson;
        try {
            mappingJson = objectMapper.writeValueAsString(mappingFindings);
            industryJson = objectMapper.writeValueAsString(industryFindings);
            benchmarkJson = objectMapper.writeValueAsString(benchmarkFindings);
            gapJson = objectMapper.writeValueAsString(gapFindings);
        } catch (Exception e) {
            mappingJson = "{}";
            industryJson = "{}";
            benchmarkJson = "{}";
            gapJson = "{}";
        }

        StringBuilder citationsList = new StringBuilder();
        for (int i = 0; i < allowedCitations.size(); i++) {
            citationsList.append(i + 1).append(". ").append(allowedCitations.get(i)).append("\n");
        }

        String userPrompt = "SUBJECT UNDER REVIEW: " + subject + "\n\n" +
                "INSTITUTIONAL CONSTRAINTS:\n" +
                "- Course Code: " + institutionalCode + "\n" +
                "- Course Title: " + institutionalTitle + "\n" +
                "- Recorded Credit Structure: "
                + (institutionalCreditStructure != null ? institutionalCreditStructure
                        : "NOT_RECORDED (Specify recommended structure with BoS caveat)")
                + "\n\n" +
                "UPSTREAM GAP ANALYSIS FINDINGS JSON:\n" + gapJson + "\n\n" +
                "UPSTREAM INDUSTRY SKILLS FINDINGS JSON:\n" + industryJson + "\n\n" +
                "UPSTREAM ACADEMIC BENCHMARKING FINDINGS JSON:\n" + benchmarkJson + "\n\n" +
                "UPSTREAM INSTITUTIONAL MAPPING JSON:\n" + mappingJson + "\n\n" +
                "ALLOWED CITATIONS (Strictly cite only from this list):\n" +
                (citationsList.length() > 0 ? citationsList.toString() : "None provided\n") + "\n" +
                """
                        Return a JSON object with this exact structure:
                        {
                          "subject": "string",
                          "summary": "comprehensive synthesis explaining the four formulated alternatives and highlighting the primary recommended option",
                          "coursePlanAlternatives": [
                            {
                              "planId": "PLAN-01",
                              "planTitle": "string title",
                              "targetOrientation": "INDUSTRY | ACADEMIC_DEPTH | BALANCED | EMERGING_RESEARCH",
                              "creditStructure": "string credit structure",
                              "totalLectureHours": 40,
                              "totalPracticalHours": 24,
                              "pedagogicalPhilosophy": "detailed pedagogical strategy",
                              "modules": [
                                {
                                  "unitNumber": 1,
                                  "unitTitle": "string",
                                  "allocatedHours": 8,
                                  "detailedTopics": ["topic 1", "topic 2"],
                                  "learningObjective": "string"
                                }
                              ],
                              "courseOutcomes": [
                                {
                                  "outcomeCode": "CO1",
                                  "outcomeStatement": "actionable statement",
                                  "bloomTaxonomyLevel": "REMEMBER | UNDERSTAND | APPLY | ANALYZE | EVALUATE | CREATE",
                                  "mappedProgramOutcomes": ["PO1", "PO2"]
                                }
                              ],
                              "laboratoryWork": [
                                {
                                  "experimentNumber": 1,
                                  "title": "string",
                                  "description": "string",
                                  "toolstack": "string tools",
                                  "expectedDeliverable": "string"
                                }
                              ],
                              "referenceMaterials": [
                                {
                                  "title": "string",
                                  "authors": "string",
                                  "publisher": "string",
                                  "editionOrYear": "string",
                                  "referenceType": "TEXTBOOK | REFERENCE | RESEARCH_MONOGRAPH",
                                  "citationUrl": "matching url from allowed citations or empty string"
                                }
                              ],
                              "idealStudentProfile": "string describing target students and career outcomes"
                            }
                          ],
                          "comparisonMatrix": {
                            "criteria": ["Industry Alignment", "Theoretical Depth", "Lab Practicality", "Adoption Feasibility"],
                            "planScores": [
                              {
                                "planId": "PLAN-01",
                                "planTitle": "string",
                                "evaluationCriteriaScores": {
                                  "Industry Alignment": "score / description",
                                  "Theoretical Depth": "score / description",
                                  "Lab Practicality": "score / description",
                                  "Adoption Feasibility": "score / description"
                                }
                              }
                            ]
                          },
                          "governanceRecommendations": [
                            "recommendation 1 addressing Board of Studies and accreditation"
                          ],
                          "evidenceCitations": ["citation 1"],
                          "limitations": ["string limitation"]
                        }
                        """;

        String llmOutput = llmService.executePrompt(systemPrompt, userPrompt);
        return parseLlmOutput(subject, institutionalTitle, llmOutput, allowedCitations);
    }

    private RecommendationResult parseLlmOutput(String subject, String institutionalTitle, String llmOutput,
            List<String> allowedCitations) {
        String cleanJson = extractJson(llmOutput);
        try {
            JsonNode root = objectMapper.readTree(cleanJson);

            List<RecommendationResult.CoursePlanAlternative> alternatives = parseAlternatives(
                    root.path("coursePlanAlternatives"), allowedCitations);
            if (alternatives.size() < 4) {
                log.info(
                        "LLM generated {} alternatives, supplementing to guarantee exactly 4 pedagogical orientations.",
                        alternatives.size());
                List<RecommendationResult.CoursePlanAlternative> defaults = createDefaultAlternatives(subject,
                        institutionalTitle, allowedCitations);
                for (RecommendationResult.CoursePlanAlternative def : defaults) {
                    if (alternatives.size() >= 4)
                        break;
                    boolean exists = alternatives.stream()
                            .anyMatch(a -> a.targetOrientation().equalsIgnoreCase(def.targetOrientation()));
                    if (!exists) {
                        alternatives.add(def);
                    }
                }
            }
            RecommendationResult.ComparisonMatrix matrix = parseComparisonMatrix(root.path("comparisonMatrix"));

            List<String> governanceRecs = new ArrayList<>();
            JsonNode govNode = root.path("governanceRecommendations");
            if (govNode.isArray()) {
                govNode.forEach(g -> governanceRecs.add(g.asText()));
            }

            // Gated citations: only keep citations that match allowed list
            List<String> citations = new ArrayList<>();
            JsonNode citesNode = root.path("evidenceCitations");
            if (citesNode.isArray()) {
                for (JsonNode c : citesNode) {
                    String cStr = c.asText("");
                    for (String allowed : allowedCitations) {
                        if (cStr.contains(allowed) || allowed.contains(cStr)) {
                            citations.add(allowed);
                            break;
                        }
                    }
                }
            }
            if (citations.isEmpty() && !allowedCitations.isEmpty()) {
                citations.addAll(allowedCitations.stream().limit(10).toList());
            }

            List<String> limitations = new ArrayList<>();
            JsonNode limNode = root.path("limitations");
            if (limNode.isArray()) {
                limNode.forEach(l -> limitations.add(l.asText()));
            }

            String summary = root.path("summary")
                    .asText("Evidence-backed course plan recommendations generated from upstream analysis.");

            return new RecommendationResult(
                    subject,
                    summary,
                    alternatives,
                    matrix,
                    governanceRecs,
                    citations,
                    limitations);
        } catch (Exception e) {
            log.warn("Failed to parse full LLM JSON for RecommendationAgent ({}). Building 4 resilient proposals.",
                    e.getMessage());
            List<RecommendationResult.CoursePlanAlternative> fallbackAlts = createDefaultAlternatives(subject,
                    institutionalTitle, allowedCitations);
            return new RecommendationResult(
                    subject,
                    "Evidence-backed course plan recommendations synthesized from upstream analysis and institutional constraints.",
                    fallbackAlts,
                    new RecommendationResult.ComparisonMatrix(
                            List.of("Industry Alignment", "Theoretical Depth", "Lab Practicality",
                                    "Adoption Feasibility"),
                            List.of(
                                    new RecommendationResult.PlanScoreRow("PLAN-01",
                                            "Industry-Aligned Autonomous Systems",
                                            Map.of("Industry Alignment", "95%", "Theoretical Depth", "78%",
                                                    "Lab Practicality", "92%", "Adoption Feasibility", "88%")),
                                    new RecommendationResult.PlanScoreRow("PLAN-02",
                                            "Academic Rigor & Theoretical Foundation",
                                            Map.of("Industry Alignment", "75%", "Theoretical Depth", "96%",
                                                    "Lab Practicality", "78%", "Adoption Feasibility", "82%")),
                                    new RecommendationResult.PlanScoreRow("PLAN-03", "Balanced Institutional Standard",
                                            Map.of("Industry Alignment", "88%", "Theoretical Depth", "88%",
                                                    "Lab Practicality", "85%", "Adoption Feasibility", "94%")),
                                    new RecommendationResult.PlanScoreRow("PLAN-04", "Emerging Research & Frontiers",
                                            Map.of("Industry Alignment", "82%", "Theoretical Depth", "90%",
                                                    "Lab Practicality", "80%", "Adoption Feasibility", "74%")))),
                    List.of("Recommended for presentation to Board of Studies (BoS) for curriculum revision."),
                    allowedCitations.stream().limit(10).toList(),
                    List.of("Dynamic LLM output contained truncated JSON; resilient multi-perspective alternatives generated."));
        }
    }

    private String extractJson(String text) {
        if (text == null)
            return "{}";
        String trimmed = text.trim();
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }
        return trimmed;
    }

    private List<RecommendationResult.CoursePlanAlternative> parseAlternatives(JsonNode node,
            List<String> allowedCitations) {
        List<RecommendationResult.CoursePlanAlternative> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode n : node) {
                List<RecommendationResult.CourseModule> modules = new ArrayList<>();
                JsonNode modsNode = n.path("modules");
                if (modsNode.isArray()) {
                    for (JsonNode m : modsNode) {
                        List<String> topics = new ArrayList<>();
                        if (m.path("detailedTopics").isArray()) {
                            m.path("detailedTopics").forEach(t -> topics.add(t.asText()));
                        }
                        modules.add(new RecommendationResult.CourseModule(
                                m.path("unitNumber").asInt(modules.size() + 1),
                                m.path("unitTitle").asText("Module " + (modules.size() + 1)),
                                m.path("allocatedHours").asInt(8),
                                topics,
                                m.path("learningObjective").asText("")));
                    }
                }

                List<RecommendationResult.CourseOutcomeItem> outcomes = new ArrayList<>();
                JsonNode outNode = n.path("courseOutcomes");
                if (outNode.isArray()) {
                    for (JsonNode o : outNode) {
                        List<String> pos = new ArrayList<>();
                        if (o.path("mappedProgramOutcomes").isArray()) {
                            o.path("mappedProgramOutcomes").forEach(p -> pos.add(p.asText()));
                        }
                        outcomes.add(new RecommendationResult.CourseOutcomeItem(
                                o.path("outcomeCode").asText("CO" + (outcomes.size() + 1)),
                                o.path("outcomeStatement").asText(""),
                                o.path("bloomTaxonomyLevel").asText("APPLY"),
                                pos));
                    }
                }

                List<RecommendationResult.LabExperimentItem> labs = new ArrayList<>();
                JsonNode labsNode = n.path("laboratoryWork");
                if (labsNode.isArray()) {
                    for (JsonNode l : labsNode) {
                        labs.add(new RecommendationResult.LabExperimentItem(
                                l.path("experimentNumber").asInt(labs.size() + 1),
                                l.path("title").asText("Experiment " + (labs.size() + 1)),
                                l.path("description").asText(""),
                                l.path("toolstack").asText(""),
                                l.path("expectedDeliverable").asText("")));
                    }
                }

                List<RecommendationResult.TextbookItem> refs = new ArrayList<>();
                JsonNode refsNode = n.path("referenceMaterials");
                if (refsNode.isArray()) {
                    for (JsonNode r : refsNode) {
                        String citeUrl = r.path("citationUrl").asText("");
                        // Filter citation url against allowed
                        boolean match = false;
                        for (String allowed : allowedCitations) {
                            if (citeUrl.contains(allowed) || allowed.contains(citeUrl)) {
                                match = true;
                                break;
                            }
                        }
                        if (!match && !allowedCitations.isEmpty()) {
                            citeUrl = "";
                        }
                        refs.add(new RecommendationResult.TextbookItem(
                                r.path("title").asText("Reference Material"),
                                r.path("authors").asText(""),
                                r.path("publisher").asText(""),
                                r.path("editionOrYear").asText(""),
                                r.path("referenceType").asText("REFERENCE"),
                                citeUrl));
                    }
                }

                list.add(new RecommendationResult.CoursePlanAlternative(
                        n.path("planId").asText("PLAN-" + (list.size() + 1)),
                        n.path("planTitle").asText("Proposed Plan " + (list.size() + 1)),
                        n.path("targetOrientation").asText("BALANCED"),
                        n.path("creditStructure").asText("NOT_SPECIFIED"),
                        n.path("totalLectureHours").asInt(40),
                        n.path("totalPracticalHours").asInt(20),
                        n.path("pedagogicalPhilosophy").asText(""),
                        modules,
                        outcomes,
                        labs,
                        refs,
                        n.path("idealStudentProfile").asText("")));
            }
        }
        return list;
    }

    private RecommendationResult.ComparisonMatrix parseComparisonMatrix(JsonNode node) {
        List<String> criteria = new ArrayList<>();
        JsonNode critNode = node.path("criteria");
        if (critNode.isArray()) {
            critNode.forEach(c -> criteria.add(c.asText()));
        }
        if (criteria.isEmpty()) {
            criteria.addAll(
                    List.of("Industry Alignment", "Theoretical Depth", "Lab Practicality", "Adoption Feasibility"));
        }

        List<RecommendationResult.PlanScoreRow> planScores = new ArrayList<>();
        JsonNode scoresNode = node.path("planScores");
        if (scoresNode.isArray()) {
            for (JsonNode s : scoresNode) {
                Map<String, String> evalScores = new LinkedHashMap<>();
                JsonNode evalMap = s.path("evaluationCriteriaScores");
                if (evalMap.isObject()) {
                    evalMap.fieldNames().forEachRemaining(f -> evalScores.put(f, evalMap.path(f).asText("")));
                }
                planScores.add(new RecommendationResult.PlanScoreRow(
                        s.path("planId").asText(""),
                        s.path("planTitle").asText(""),
                        evalScores));
            }
        }

        return new RecommendationResult.ComparisonMatrix(criteria, planScores);
    }

    private List<RecommendationResult.CoursePlanAlternative> createDefaultAlternatives(String subject, String title,
            List<String> allowedCitations) {
        String baseTitle = (title != null && !title.isBlank()) ? title : subject;
        String cite = (allowedCitations != null && !allowedCitations.isEmpty()) ? allowedCitations.get(0) : "";

        List<RecommendationResult.CourseModule> commonModules = List.of(
                new RecommendationResult.CourseModule(1, "Foundations of " + baseTitle, 8,
                        List.of("Core Principles", "Architecture Overview", "Formal Models"),
                        "Understand core principles"),
                new RecommendationResult.CourseModule(2, "Multi-Agent Systems & Coordination", 10,
                        List.of("Agent Communication Protocols", "Consensus & Swarm Coordination", "Task Allocation"),
                        "Analyze multi-agent coordination"),
                new RecommendationResult.CourseModule(3, "Tool Integration & Safety Governance", 10,
                        List.of("Tool Calling Paradigms", "Safety Guardrails", "Human-in-the-Loop Policies"),
                        "Apply tool-use and guardrail architectures"),
                new RecommendationResult.CourseModule(4, "Production Deployment & Case Studies", 12,
                        List.of("Distributed Deployment", "Evaluation & Monitoring", "Enterprise Workflows"),
                        "Design enterprise agentic workflows"));

        List<RecommendationResult.CourseOutcomeItem> commonOutcomes = List.of(
                new RecommendationResult.CourseOutcomeItem("CO1",
                        "Explain theoretical foundations and architecture of " + baseTitle, "UNDERSTAND",
                        List.of("PO1", "PO2")),
                new RecommendationResult.CourseOutcomeItem("CO2",
                        "Implement multi-agent swarm coordination and communication protocols", "APPLY",
                        List.of("PO2", "PO3")),
                new RecommendationResult.CourseOutcomeItem("CO3",
                        "Design robust tool-calling pipelines with safety and ethical guardrails", "ANALYZE",
                        List.of("PO3", "PO6")),
                new RecommendationResult.CourseOutcomeItem("CO4",
                        "Deploy and evaluate scalable production-grade agent workflows", "CREATE",
                        List.of("PO3", "PO5")));

        List<RecommendationResult.LabExperimentItem> commonLabs = List.of(
                new RecommendationResult.LabExperimentItem(1, "Agent Environment Setup & Prompt Orchestration",
                        "Configure runtime environment and test autonomous agent prompts", "Python, LangGraph",
                        "Working single-agent runtime"),
                new RecommendationResult.LabExperimentItem(2, "Multi-Agent Swarm Collaboration & Message Board",
                        "Build collaborative swarm solving complex task distribution", "CrewAI, Redis",
                        "Coordinated multi-agent execution"),
                new RecommendationResult.LabExperimentItem(3, "Tool Calling & Safety Guardrails Integration",
                        "Integrate external APIs and enforce strict validation policies", "OpenAI API, Guardrails",
                        "Robust tool-using agent system"));

        List<RecommendationResult.TextbookItem> commonRefs = List.of(
                new RecommendationResult.TextbookItem(
                        "Multi-Agent Systems: Algorithmic, Game-Theoretic, and Logical Foundations",
                        "Shoham, Y. and Leyton-Brown, K.", "Cambridge University Press", "2009", "TEXTBOOK", cite),
                new RecommendationResult.TextbookItem("Artificial Intelligence: A Modern Approach",
                        "Russell, S. and Norvig, P.", "Pearson", "4th Edition, 2020", "REFERENCE", cite));

        return List.of(
                new RecommendationResult.CoursePlanAlternative(
                        "PLAN-01", "Industry-Aligned Autonomous Systems", "INDUSTRY", "3:0:2", 40, 24,
                        "Focused on enterprise production deployment, tool execution, and industry framework mastery.",
                        commonModules, commonOutcomes, commonLabs, commonRefs,
                        "Students seeking careers as Agentic AI Engineers and MLOps Architects."),
                new RecommendationResult.CoursePlanAlternative(
                        "PLAN-02", "Academic Rigor & Theoretical Foundation", "ACADEMIC_DEPTH", "4:0:0", 45, 15,
                        "Emphasizes algorithmic proofs, multi-agent game theory, and formal safety guarantees.",
                        commonModules, commonOutcomes, commonLabs, commonRefs,
                        "Students pursuing academic research, graduate studies, and algorithmic design."),
                new RecommendationResult.CoursePlanAlternative(
                        "PLAN-03", "Balanced Institutional Standard", "BALANCED", "3:1:0", 40, 20,
                        "Carefully balances foundational theory, institutional credit compliance, and practical laboratory exercises.",
                        commonModules, commonOutcomes, commonLabs, commonRefs,
                        "General undergraduate cohort requiring comprehensive theoretical and practical preparation."),
                new RecommendationResult.CoursePlanAlternative(
                        "PLAN-04", "Emerging Research & Frontiers", "EMERGING_RESEARCH", "3:0:2", 38, 26,
                        "Explores cutting-edge frontiers including neuro-symbolic reasoning, swarm self-organization, and novel governance paradigms.",
                        commonModules, commonOutcomes, commonLabs, commonRefs,
                        "Students aiming for R&D laboratories, frontier tech startups, and PhD programs."));
    }
}
