package org.curriculumai.coordinator;

import org.curriculumai.audit.AuditEvent;
import org.curriculumai.audit.AuditRepository;
import org.curriculumai.coordinator.dto.*;
import org.curriculumai.planning.LockedPlan;
import org.curriculumai.planning.LockedPlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class AuraCoordinatorServiceTest {

    @Autowired
    private AuraCoordinatorService coordinatorService;

    @Autowired
    private LockedPlanRepository planRepository;

    @Autowired
    private AuditRepository auditRepository;

    private final String userId = "usr-test-coordinator";

    @Test
    @DisplayName("Initiate analysis run establishes stages, locked plan and audit trail")
    void testInitiateRun() {
        CourseRequestInput input = new CourseRequestInput(
                "I need to design the course plan for Computer Vision",
                "Computer Science & Engineering (AI)",
                "UG",
                null
        );

        RunResponse response = coordinatorService.initiateRun(userId, input);

        assertNotNull(response.id());
        assertEquals("running", response.status());
        assertEquals("parallel_view_building", response.currentStageId());
        assertEquals(5, response.stages().size());

        // Verify Locked Plan was persisted
        Optional<LockedPlan> planOpt = planRepository.findByRunId(response.id());
        assertTrue(planOpt.isPresent());
        LockedPlan plan = planOpt.get();
        assertEquals("Computer Vision", plan.getCourseName());
        assertTrue(plan.isLocked());

        // Verify Audit Event was recorded
        List<AuditEvent> auditEvents = auditRepository.findByRunIdOrderByCreatedAtDesc(response.id());
        assertFalse(auditEvents.isEmpty());
        assertEquals("COORDINATOR_INITIATE_RUN", auditEvents.get(0).getAction());
    }

    @Test
    @DisplayName("Four distinct alternatives are generated conforming to Chapter 5")
    void testGetFourAlternatives() {
        CourseRequestInput input = new CourseRequestInput(
                "Create a curriculum for Machine Learning",
                "Computer Science & Engineering",
                "UG",
                null
        );

        RunResponse run = coordinatorService.initiateRun(userId, input);
        List<AlternativeResponse> alternatives = coordinatorService.getCourseAlternatives(run.id());

        assertEquals(4, alternatives.size());
        assertEquals("ALT-1", alternatives.get(0).code());
        assertEquals("ALT-2", alternatives.get(1).code());
        assertEquals("ALT-3", alternatives.get(2).code());
        assertEquals("ALT-4", alternatives.get(3).code());

        // Verify each alternative has non-empty outcomes, modules, highlights
        for (AlternativeResponse alt : alternatives) {
            assertNotNull(alt.title());
            assertFalse(alt.courseOutcomes().isEmpty());
            assertFalse(alt.modules().isEmpty());
            assertFalse(alt.keyHighlights().isEmpty());
            assertTrue(alt.evidenceCoveragePercent() > 0);
        }
    }

    @Test
    @DisplayName("Faculty feedback submission commits preference and generates final plan ID")
    void testSubmitFeedback() {
        CourseRequestInput input = new CourseRequestInput("Deep Learning", "CSE", "UG", null);
        RunResponse run = coordinatorService.initiateRun(userId, input);

        FeedbackInput feedback = new FeedbackInput(
                run.id(),
                "ALT-2",
                List.of("ALT-1", "ALT-3", "ALT-4"),
                "Dr. Evaluator",
                "FACULTY",
                "Selected for applied industrial systems focus.",
                "Add more hours on ONNX runtime",
                null
        );

        Map<String, String> result = coordinatorService.submitSelectionFeedback(userId, feedback);
        assertNotNull(result.get("finalPlanId"));

        FinalPlanResponse finalPlan = coordinatorService.getFinalCoursePlan(result.get("finalPlanId"));
        assertNotNull(finalPlan);
        assertEquals(50, finalPlan.assessmentScheme().ciePercentage());
        assertEquals(50, finalPlan.assessmentScheme().seePercentage());
        assertEquals("Approved", finalPlan.governanceSignOffs().bosApprovalStatus());
    }
}
