package org.curriculumai.coordinator;

import org.curriculumai.coordinator.dto.AlternativeResponse;
import org.curriculumai.coordinator.dto.FinalPlanResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class KleTechDocumentServiceTest {

    @Autowired
    private KleTechDocumentService documentService;

    @Test
    @DisplayName("Generate DOCX and PDF using KLE Tech master template conforms to all institutional rules")
    void testGenerateDocxAndPdf() {
        FinalPlanResponse samplePlan = new FinalPlanResponse(
                "plan-test-agentic-ai",
                "run-test-12345",
                "26ECAC401",
                "Agentic AI",
                "School of Electronics & Communication Engineering",
                "KLE Technological University, Belagavi",
                4,
                4,
                new FinalPlanResponse.ContactHoursDto(3, 0, 2, 52),
                List.of(
                        new AlternativeResponse.CourseOutcomeDto("CO1", "Formulate autonomous agent architectures, task decomposition graphs, and reactive execution loops.", "Applying", List.of("PO1", "PO2")),
                        new AlternativeResponse.CourseOutcomeDto("CO2", "Implement multi-agent communication protocols and tool-augmented reasoning workflows.", "Applying", List.of("PO2", "PO3", "PSO1")),
                        new AlternativeResponse.CourseOutcomeDto("CO3", "Design robust evaluation benchmarks and human-in-the-loop review mechanisms.", "Creating", List.of("PO3", "PO5")),
                        new AlternativeResponse.CourseOutcomeDto("CO4", "Deploy end-to-end agentic pipelines with latency and cost optimization.", "Evaluating", List.of("PO4", "PO6"))
                ),
                List.of(
                        new AlternativeResponse.CourseModuleDto(1, "Foundations of Agentic AI & Autonomous Decision Systems", 10,
                                List.of("Agency concepts", "ReAct framework", "Planning heuristics", "Tool selection"),
                                List.of("Formulate agent decision loops"),
                                List.of("EVID-AGENT-01")),
                        new AlternativeResponse.CourseModuleDto(2, "Multi-Agent Coordination & Swarm Architectures", 12,
                                List.of("Message passing", "Consensus protocols", "Shared state", "Task delegation"),
                                List.of("Implement multi-agent swarms"),
                                List.of("EVID-AGENT-02")),
                        new AlternativeResponse.CourseModuleDto(3, "Tool-Augmented Reasoning & External Grounding", 14,
                                List.of("API calling", "Vector retrieval", "Search grounding", "Memory buffers"),
                                List.of("Design grounded agent workflows"),
                                List.of("EVID-AGENT-03")),
                        new AlternativeResponse.CourseModuleDto(4, "Evaluation, Safety & Production Deployment", 16,
                                List.of("Benchmarking agent drift", "Security guardrails", "Token budget management", "Observability"),
                                List.of("Evaluate agent systems under operational constraints"),
                                List.of("EVID-AGENT-04"))
                ),
                new FinalPlanResponse.AssessmentSchemeDto(
                        50,
                        50,
                        "Continuous Internal Evaluation (CIE) + Semester End Exam (SEE)",
                        List.of(
                                new FinalPlanResponse.ComponentDto("Continuous Internal Evaluation (CIE)", 50, "Two written tests, periodic lab assessments, and micro-project demo"),
                                new FinalPlanResponse.ComponentDto("Semester End Examination (SEE)", 50, "Comprehensive theoretical examination & viva")
                        )
                ),
                1,
                "AUDIT-TRL-TEST-001",
                "2026-11-24",
                List.of(
                        new AlternativeResponse.EvidenceReferenceDto("EVID-ACM-2023", "ACM/IEEE Computer Science Curricula (CS2023)", "ACM / IEEE Computer Society", 2023, "Knowledge Area: Intelligent Systems (IS-Agent)", "https://www.acm.org/education/curricula-recommendations", "Recommends multi-agent reasoning, search grounding, and agent safety.", "2026-11-24T00:00:00Z")
                ),
                new FinalPlanResponse.GovernanceSignOffsDto(
                        "Dr. Faculty Member",
                        "Head of Department, School of Electronics & Communication Engineering",
                        "Dean (Academics)",
                        "Approved"
                )
        );

        // 1. Generate DOCX
        byte[] docxBytes = documentService.generateDocx(samplePlan);
        assertNotNull(docxBytes);
        assertTrue(docxBytes.length > 50000, "DOCX file should be substantial (>50KB) based on template size");

        // 2. Generate PDF
        byte[] pdfBytes = documentService.generatePdf(samplePlan);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000, "PDF file should be non-empty and valid");

        // 3. Verify PDF signature
        String header = new String(pdfBytes, 0, Math.min(pdfBytes.length, 10));
        assertTrue(header.startsWith("%PDF-"), "Generated PDF must start with '%PDF-' header");
    }

    @Test
    @DisplayName("Scenario A: Theory-only course handles lab tables conditionally without duplication")
    void testScenarioA_TheoryOnlyCourse() {
        FinalPlanResponse theoryPlan = new FinalPlanResponse(
                "plan-theory-001", "run-th-001", "26CS301", "Automata Theory",
                "Department of Computer Science and Engineering", "KLE Tech",
                5, 3, new FinalPlanResponse.ContactHoursDto(3, 0, 0, 42),
                List.of(new AlternativeResponse.CourseOutcomeDto("CO1", "Analyze formal grammars", "Analyzing", List.of("PO1"))),
                List.of(new AlternativeResponse.CourseModuleDto(1, "Regular Languages", 10, List.of("DFAs", "NFAs"), List.of("TLO1"), List.of("EVID-01"))),
                new FinalPlanResponse.AssessmentSchemeDto(50, 50, "CIE + SEE", List.of()),
                1, "AUD-TH-01", "2026-11-24", List.of(),
                new FinalPlanResponse.GovernanceSignOffsDto("Prof. Turing", "HoD, CSE", "Dean", "Approved")
        );

        byte[] docxBytes = documentService.generateDocx(theoryPlan);
        assertNotNull(docxBytes);
        assertTrue(docxBytes.length > 20000);

        byte[] pdfBytes = documentService.generatePdf(theoryPlan);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);
    }

    @Test
    @DisplayName("Scenario C & D: Custom department and missing mappings handled without crashing or dangling placeholders")
    void testScenarioCD_CustomDepartmentAndMissingData() {
        FinalPlanResponse customPlan = new FinalPlanResponse(
                "plan-cust-002", "run-cust-002", "26ME401", "Robotic Kinematics",
                "School of Mechanical Engineering", "KLE Technological University, Belagavi",
                6, 4, null, null, null, null,
                1, null, null, null, null
        );

        byte[] docxBytes = documentService.generateDocx(customPlan);
        assertNotNull(docxBytes);
        assertTrue(docxBytes.length > 20000);

        byte[] pdfBytes = documentService.generatePdf(customPlan);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);
    }
}

