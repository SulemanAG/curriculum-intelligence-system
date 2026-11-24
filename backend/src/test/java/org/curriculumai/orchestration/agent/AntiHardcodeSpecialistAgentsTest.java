package org.curriculumai.orchestration.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.evidence.service.EvidenceSearchService;
import org.curriculumai.orchestration.agent.benchmarking.AcademicBenchmarkingAgent;
import org.curriculumai.orchestration.agent.gap.GapAnalysisAgent;
import org.curriculumai.orchestration.agent.gap.GapAnalysisResult;
import org.curriculumai.orchestration.agent.industry.IndustrySkillsAgent;
import org.curriculumai.orchestration.agent.recommendation.RecommendationAgent;
import org.curriculumai.orchestration.agent.recommendation.RecommendationResult;
import org.curriculumai.orchestration.llm.LlmExecutionService;
import org.curriculumai.orchestration.research.ExternalResearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class AntiHardcodeSpecialistAgentsTest {

    private ObjectMapper objectMapper;
    private LlmExecutionService llmService;
    private ExternalResearchService researchService;
    private EvidenceSearchService evidenceSearchService;

    private RecommendationAgent recommendationAgent;
    private GapAnalysisAgent gapAnalysisAgent;
    private IndustrySkillsAgent industrySkillsAgent;
    private AcademicBenchmarkingAgent academicBenchmarkingAgent;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        llmService = Mockito.mock(LlmExecutionService.class);
        researchService = Mockito.mock(ExternalResearchService.class);
        evidenceSearchService = Mockito.mock(EvidenceSearchService.class);

        when(llmService.isAvailable()).thenReturn(true);
        when(llmService.getProviderName()).thenReturn("mock-gemini");
        when(llmService.getModelName()).thenReturn("gemini-2.5-flash");

        recommendationAgent = new RecommendationAgent(llmService, objectMapper);
        gapAnalysisAgent = new GapAnalysisAgent(llmService, objectMapper);
        industrySkillsAgent = new IndustrySkillsAgent(researchService, evidenceSearchService, llmService, objectMapper);
        academicBenchmarkingAgent = new AcademicBenchmarkingAgent(researchService, evidenceSearchService, llmService, objectMapper);
    }

    @Test
    @DisplayName("RecommendationAgent: Rejects execution if required upstream tasks are missing")
    void testRecommendationAgentFailsWhenUpstreamTasksMissing() throws Exception {
        // Only provide task_curriculum_mapping; omit gap analysis, industry skills, and benchmarking
        Map<String, StructuredAgentResult> partialOutputs = Map.of(
                "task_curriculum_mapping", new StructuredAgentResult(
                        "run-1", "task-1", "curriculum-mapping-agent", "COMPLETED",
                        Map.of("status", "RESOLVED"), List.of(), List.of(), null, Map.of()
                )
        );

        TaskExecutionContext ctx = new TaskExecutionContext(
                "run-1", "task-5", "task_recommendations", "Distributed Systems",
                "Create course plan", Map.of(), Map.of(), partialOutputs, List.of(), "user-1"
        );

        StructuredAgentResult result = recommendationAgent.execute(ctx);

        assertEquals("BLOCKED", result.executionStatus());
        assertNotNull(result.errorDetails());
        assertTrue(result.errorDetails().contains("missing"));
        assertTrue(result.findings().containsKey("missingUpstreamTasks"));
    }

    @Test
    @DisplayName("RecommendationAgent: Rejects blank or null subject without defaulting to Computer Vision")
    void testRecommendationAgentRejectsBlankSubject() throws Exception {
        TaskExecutionContext ctx = new TaskExecutionContext(
                "run-1", "task-5", "task_recommendations", "   ",
                "Prompt", Map.of(), Map.of(), Map.of(), List.of(), "user-1"
        );

        StructuredAgentResult result = recommendationAgent.execute(ctx);

        assertEquals("BLOCKED", result.executionStatus());
        assertTrue(result.limitations().get(0).contains("Subject parameter is required"));
    }

    @Test
    @DisplayName("RecommendationAgent: Generates plan dynamically derived from upstream inputs and gates citations")
    void testRecommendationAgentDynamicGenerationAndCitationGating() throws Exception {
        String allowedCitation = "https://mit.edu/6.824/schedule.html";
        String fakeCitation = "https://fake-citation-fabricated.com/paper.pdf";

        Map<String, StructuredAgentResult> upstreamOutputs = createValidUpstreamOutputs(
                "Distributed Systems",
                List.of(allowedCitation)
        );

        String mockLlmJson = """
                {
                  "subject": "Distributed Systems",
                  "summary": "Dynamic course plan for Distributed Systems focusing on consensus and fault tolerance.",
                  "coursePlanAlternatives": [
                    {
                      "planId": "PLAN-DS-01",
                      "planTitle": "Cloud Infrastructure & Distributed Systems",
                      "targetOrientation": "INDUSTRY",
                      "creditStructure": "RECOMMENDED: 3-0-2-4 (Subject to BoS Approval)",
                      "totalLectureHours": 42,
                      "totalPracticalHours": 28,
                      "pedagogicalPhilosophy": "Hands-on Go implementation of Raft consensus.",
                      "modules": [
                        { "unitNumber": 1, "unitTitle": "RPC & Concurrency", "allocatedHours": 8, "detailedTopics": ["Go routines", "gRPC"], "learningObjective": "Build concurrent RPC servers." }
                      ],
                      "courseOutcomes": [
                        { "outcomeCode": "CO1", "outcomeStatement": "Implement Raft consensus algorithm.", "bloomTaxonomyLevel": "CREATE", "mappedProgramOutcomes": ["PO1", "PO3"] }
                      ],
                      "laboratoryWork": [
                        { "experimentNumber": 1, "title": "MapReduce in Go", "description": "Worker master coordination.", "toolstack": "Go 1.22", "expectedDeliverable": "Passing tests" }
                      ],
                      "referenceMaterials": [
                        { "title": "MIT 6.824 Schedule", "authors": "Morris et al.", "publisher": "MIT", "editionOrYear": "2024", "referenceType": "REFERENCE", "citationUrl": "%s" },
                        { "title": "Invented Book", "authors": "Ghost", "publisher": "None", "editionOrYear": "2024", "referenceType": "TEXTBOOK", "citationUrl": "%s" }
                      ],
                      "idealStudentProfile": "Students seeking backend systems roles."
                    }
                  ],
                  "comparisonMatrix": {
                    "criteria": ["Industry Alignment", "Theoretical Depth"],
                    "planScores": [
                      { "planId": "PLAN-DS-01", "planTitle": "Cloud Infrastructure", "evaluationCriteriaScores": { "Industry Alignment": "9.5", "Theoretical Depth": "7.0" } }
                    ]
                  },
                  "governanceRecommendations": [
                    "Formally adopt Raft consensus lab environment."
                  ],
                  "evidenceCitations": ["%s", "%s"],
                  "limitations": ["Requires Linux workstation lab."]
                }
                """.formatted(allowedCitation, fakeCitation, allowedCitation, fakeCitation);

        when(llmService.executePrompt(anyString(), anyString())).thenReturn(mockLlmJson);

        TaskExecutionContext ctx = new TaskExecutionContext(
                "run-1", "task-5", "task_recommendations", "Distributed Systems",
                "Create course plan", Map.of(), Map.of(), upstreamOutputs, List.of(), "user-1"
        );

        StructuredAgentResult result = recommendationAgent.execute(ctx);

        assertEquals("COMPLETED", result.executionStatus());
        RecommendationResult recResult = objectMapper.convertValue(result.findings(), RecommendationResult.class);

        // Verify subject and plan are derived from input
        assertEquals("Distributed Systems", recResult.subject());
        assertEquals(1, recResult.coursePlanAlternatives().size());
        assertEquals("PLAN-DS-01", recResult.coursePlanAlternatives().get(0).planId());
        assertEquals("Cloud Infrastructure & Distributed Systems", recResult.coursePlanAlternatives().get(0).planTitle());

        // Verify strict citation gating: fake citation was stripped, only allowed citation retained
        assertTrue(result.evidenceReferences().contains(allowedCitation));
        assertFalse(result.evidenceReferences().contains(fakeCitation));

        var refs = recResult.coursePlanAlternatives().get(0).referenceMaterials();
        assertEquals(allowedCitation, refs.get(0).citationUrl());
        assertEquals("", refs.get(1).citationUrl(), "Fabricated citation URL must be stripped by gating filter");
    }

    @Test
    @DisplayName("GapAnalysisAgent: Dynamically calculates health score and rejects missing upstream tasks")
    void testGapAnalysisAgentDynamicHealthCalculation() throws Exception {
        // 1. Missing upstream tasks fails
        TaskExecutionContext incompleteCtx = new TaskExecutionContext(
                "run-1", "task-4", "task_gap_analysis", "Robotics",
                "Prompt", Map.of(), Map.of(), Map.of(), List.of(), "user-1"
        );
        StructuredAgentResult incompleteResult = gapAnalysisAgent.execute(incompleteCtx);
        assertEquals("BLOCKED", incompleteResult.executionStatus());

        // 2. Dynamic scoring with 2 CRITICAL gaps vs 0 CRITICAL gaps
        Map<String, StructuredAgentResult> upstream = createValidUpstreamOutputs("Robotics", List.of("https://ieee.org/robotics"));

        String mockHighDeficitLlm = """
                {
                  "subject": "Robotics",
                  "summary": "Critical curricular deficit observed.",
                  "identifiedGaps": [
                    { "gapId": "GAP-01", "category": "MISSING_TOPIC", "title": "No ROS2", "description": "ROS2 missing", "severity": "CRITICAL", "industryImpact": "High", "academicBenchmarkReference": "IEEE", "recommendedRemediation": "Add ROS2" },
                    { "gapId": "GAP-02", "category": "DEPTH_DEFICIT", "title": "No Kinematics", "description": "Kinematics shallow", "severity": "CRITICAL", "industryImpact": "High", "academicBenchmarkReference": "IEEE", "recommendedRemediation": "Add Kinematics" }
                  ],
                  "prerequisiteDeficits": ["Linear Algebra"],
                  "evidenceCitations": ["https://ieee.org/robotics"],
                  "limitations": ["Limited lab hardware."]
                }
                """;

        when(llmService.executePrompt(anyString(), anyString())).thenReturn(mockHighDeficitLlm);

        TaskExecutionContext ctx = new TaskExecutionContext(
                "run-1", "task-4", "task_gap_analysis", "Robotics",
                "Analyze", Map.of(), Map.of(), upstream, List.of(), "user-1"
        );

        StructuredAgentResult result = gapAnalysisAgent.execute(ctx);
        assertEquals("COMPLETED", result.executionStatus());
        GapAnalysisResult gapResult = objectMapper.convertValue(result.findings(), GapAnalysisResult.class);

        assertEquals(2, gapResult.gapSeverityCounts().get("CRITICAL"));
        // 100 - (2 * 15) = 70.0
        assertEquals(70.0, gapResult.overallCurriculumHealthScore(), 0.1);
    }

    @Test
    @DisplayName("IndustrySkillsAgent: Rejects blank subject and reports honest failure when search returns empty")
    void testIndustrySkillsAgentHonestFailureOnEmptyResearch() throws Exception {
        // Blank subject check
        TaskExecutionContext blankCtx = new TaskExecutionContext(
                "run-1", "task-2", "task_industry_skills", "",
                "Prompt", Map.of(), Map.of(), Map.of(), List.of(), "user-1"
        );
        StructuredAgentResult blankResult = industrySkillsAgent.execute(blankCtx);
        assertEquals("BLOCKED", blankResult.executionStatus());

        // Empty research result
        when(researchService.conductResearch(anyString(), anyString(), any(), anyString()))
                .thenReturn(new ExternalResearchService.ResearchResult(
                        "query", "category", null, List.of(), List.of(), List.of(), false, "Search API timeout"
                ));

        TaskExecutionContext ctx = new TaskExecutionContext(
                "run-1", "task-2", "task_industry_skills", "Bioinformatics",
                "Prompt", Map.of(), Map.of(), Map.of(), List.of(), "user-1"
        );

        StructuredAgentResult result = industrySkillsAgent.execute(ctx);
        assertEquals("BLOCKED", result.executionStatus());
        assertTrue(result.limitations().get(0).contains("yielded no evidence"));
    }

    private Map<String, StructuredAgentResult> createValidUpstreamOutputs(String subject, List<String> citations) {
        StructuredAgentResult mapping = new StructuredAgentResult(
                "run-1", "task-1", "curriculum-mapping-agent", "COMPLETED",
                Map.of(
                        "resolutionStatus", "RESOLVED",
                        "resolvedCourse", Map.of("code", "CS-401", "title", subject, "credits", "NOT_RECORDED"),
                        "courseOutcomes", List.of(Map.of("code", "CO1", "statement", "Basic concepts"))
                ),
                citations, List.of(), null, Map.of()
        );

        StructuredAgentResult industry = new StructuredAgentResult(
                "run-1", "task-2", "industry-skills-agent", "COMPLETED",
                Map.of(
                        "summary", "Industry demand for " + subject,
                        "coreCompetencies", List.of(Map.of("name", "Core Tech", "category", "Foundational"))
                ),
                citations, List.of(), null, Map.of()
        );

        StructuredAgentResult benchmarking = new StructuredAgentResult(
                "run-1", "task-3", "academic-benchmarking-agent", "COMPLETED",
                Map.of(
                        "summary", "Academic benchmarking for " + subject,
                        "peerCourses", List.of(Map.of("institutionName", "Top University", "courseTitle", subject))
                ),
                citations, List.of(), null, Map.of()
        );

        StructuredAgentResult gap = new StructuredAgentResult(
                "run-1", "task-4", "gap-analysis-agent", "COMPLETED",
                Map.of(
                        "summary", "Gap analysis summary",
                        "identifiedGaps", List.of(Map.of("gapId", "GAP-01", "title", "Missing modern tools", "severity", "HIGH")),
                        "overallCurriculumHealthScore", 85.0
                ),
                citations, List.of(), null, Map.of()
        );

        return Map.of(
                "task_curriculum_mapping", mapping,
                "task_industry_skills", industry,
                "task_academic_benchmarking", benchmarking,
                "task_gap_analysis", gap
        );
    }
}

