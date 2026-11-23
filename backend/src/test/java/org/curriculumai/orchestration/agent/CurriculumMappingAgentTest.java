package org.curriculumai.orchestration.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.academic.domain.*;
import org.curriculumai.orchestration.agent.curriculum.CurriculumMappingAgent;
import org.curriculumai.orchestration.agent.curriculum.CurriculumMappingResult;
import org.curriculumai.orchestration.agent.curriculum.GovernedAcademicCatalogRetriever;
import org.curriculumai.orchestration.domain.AgentStatus;
import org.curriculumai.orchestration.llm.LlmExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CurriculumMappingAgentTest {

    @Mock
    private GovernedAcademicCatalogRetriever retriever;

    @Mock
    private LlmExecutionService llmExecutionService;

    private ObjectMapper objectMapper;
    private CurriculumMappingAgent agent;

    private Institution institution;
    private Department department;
    private Programme programme;
    private CurriculumVersion approvedVersion;
    private CurriculumVersion draftVersion;
    private Semester semester;
    private Course course;
    private CurriculumCourse curriculumCourse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        agent = new CurriculumMappingAgent(retriever, llmExecutionService, objectMapper);

        institution = new Institution("inst-1", "KLE-TECH", "KLE Technological University", "Belagavi", "India");
        department = new Department("dept-1", institution, "CSE", "Computer Science & Engineering");
        programme = new Programme("prog-1", department, "BTECH-CSE", "B.Tech Computer Science & Engineering", "UNDERGRADUATE", 4, 160);

        approvedVersion = new CurriculumVersion("cv-app-1", programme, "2024-REG", 2024);
        approvedVersion.setLifecycleStatus(CurriculumLifecycleStatus.APPROVED);

        draftVersion = new CurriculumVersion("cv-dft-1", programme, "2026-DRAFT", 2026);
        draftVersion.setLifecycleStatus(CurriculumLifecycleStatus.DRAFT);

        semester = new Semester("sem-5", approvedVersion, 5, "Semester 5 (Fall 2024)", 22);

        course = new Course("crs-101", department, "CS401", "Edge AI Systems", 4, 3, 0, 2);
        course.setCourseType("CORE");
        course.setDescription("Design and deployment of edge inference and TinyML architectures.");

        curriculumCourse = new CurriculumCourse("cc-101", approvedVersion, semester, course, 4, 3, 0, 2, false, null);
    }

    @Test
    @DisplayName("Agent reports stable identifier and IMPLEMENTED status")
    void agentIdentityAndStatus() {
        assertThat(agent.getAgentId()).isEqualTo("curriculum-mapping-agent");
        assertThat(agent.getDisplayName()).isEqualTo("Curriculum Mapping Agent");
        assertThat(agent.getStatus()).isEqualTo(AgentStatus.IMPLEMENTED);
    }

    @Test
    @DisplayName("Successfully maps existing course by code with offerings, prerequisites, outcomes, and snapshots")
    void mapCourseByCodeSuccessfullyWithSnapshots() throws Exception {
        TaskExecutionContext ctx = new TaskExecutionContext(
                "run-101", "task-201", "task_curriculum_mapping",
                "CS401", "Analyze course CS401",
                Map.of("courseCode", "CS401"),
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), "usr-fac-1"
        );

        when(retriever.findCourseByCode("run-101", "task-201", "CS401", null)).thenReturn(Optional.of(course));
        when(retriever.findOfferingsForCourse("run-101", "task-201", "crs-101")).thenReturn(List.of(curriculumCourse));

        // Prerequisites
        Course prereqCourse = new Course("crs-prq-1", department, "CS201", "Data Structures", 4, 3, 0, 2);
        CoursePrerequisite cp = new CoursePrerequisite("cp-1", course, prereqCourse, "PREREQUISITE", "C");
        when(retriever.findPrerequisites("run-101", "task-201", "crs-101")).thenReturn(List.of(cp));

        // Snapshot availability
        when(retriever.hasSnapshotsForVersion("cv-app-1")).thenReturn(true);

        CurriculumOutcomeSnapshot coSnap = new CurriculumOutcomeSnapshot();
        // Use reflection or constructor for snapshot
        var snapFieldId = CurriculumOutcomeSnapshot.class.getDeclaredField("id");
        snapFieldId.setAccessible(true);
        snapFieldId.set(coSnap, "snap-co-1");
        var snapFieldType = CurriculumOutcomeSnapshot.class.getDeclaredField("snapshotType");
        snapFieldType.setAccessible(true);
        snapFieldType.set(coSnap, "COURSE_OUTCOME");
        var snapFieldCode = CurriculumOutcomeSnapshot.class.getDeclaredField("outcomeCode");
        snapFieldCode.setAccessible(true);
        snapFieldCode.set(coSnap, "CO1");
        var snapFieldDesc = CurriculumOutcomeSnapshot.class.getDeclaredField("description");
        snapFieldDesc.setAccessible(true);
        snapFieldDesc.set(coSnap, "Formulate TinyML quantization algorithms");
        var snapFieldBloom = CurriculumOutcomeSnapshot.class.getDeclaredField("bloomLevel");
        snapFieldBloom.setAccessible(true);
        snapFieldBloom.set(coSnap, "ANALYZE");
        var snapFieldSrc = CurriculumOutcomeSnapshot.class.getDeclaredField("sourceEntityId");
        snapFieldSrc.setAccessible(true);
        snapFieldSrc.set(coSnap, "co-orig-1");

        when(retriever.findOutcomeSnapshotsForCourse("run-101", "task-201", "cv-app-1", "crs-101"))
                .thenReturn(List.of(coSnap));
        when(retriever.findProgrammeOutcomeSnapshots("run-101", "task-201", "cv-app-1"))
                .thenReturn(Collections.emptyList());

        StructuredAgentResult result = agent.execute(ctx);

        assertThat(result.executionStatus()).isEqualTo("SUCCESS");
        assertThat(result.agentId()).isEqualTo("curriculum-mapping-agent");

        Map<String, Object> findings = result.findings();
        assertThat(findings.get("resolutionStatus")).isEqualTo("RESOLVED");
        assertThat(findings.get("historicalSnapshotUsed")).isEqualTo(true);
        assertThat(findings.get("snapshotSource")).isEqualTo("curriculum_outcome_snapshots");

        @SuppressWarnings("unchecked")
        Map<String, Object> canonicalCourse = (Map<String, Object>) findings.get("canonicalCourse");
        assertThat(canonicalCourse.get("code")).isEqualTo("CS401");
        assertThat(canonicalCourse.get("title")).isEqualTo("Edge AI Systems");
        assertThat(canonicalCourse.get("defaultCredits")).isEqualTo(4);

        @SuppressWarnings("unchecked")
        Map<String, Object> offering = (Map<String, Object>) findings.get("offering");
        assertThat(offering.get("versionCode")).isEqualTo("2024-REG");
        assertThat(offering.get("semesterNumber")).isEqualTo(5);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> prereqs = (List<Map<String, Object>>) findings.get("prerequisites");
        assertThat(prereqs).hasSize(1);
        assertThat(prereqs.get(0).get("courseCode")).isEqualTo("CS201");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cos = (List<Map<String, Object>>) findings.get("courseOutcomes");
        assertThat(cos).hasSize(1);
        assertThat(cos.get(0).get("code")).isEqualTo("CO1");
        assertThat(cos.get(0).get("bloomLevel")).isEqualTo("ANALYZE");

        // Explicit missing data check
        @SuppressWarnings("unchecked")
        Map<String, String> dataPresence = (Map<String, String>) findings.get("dataPresence");
        assertThat(dataPresence.get("syllabusUnits")).isEqualTo("NOT_RECORDED");
    }

    @Test
    @DisplayName("Successfully maps course using live catalog when curriculum version is DRAFT (no snapshots)")
    void mapCourseUnderDraftCurriculumUsesLiveCatalog() throws Exception {
        CurriculumCourse draftOffering = new CurriculumCourse("cc-dft-1", draftVersion, semester, course, 4, 3, 0, 2, false, null);

        TaskExecutionContext ctx = new TaskExecutionContext(
                "run-102", "task-202", "task_curriculum_mapping",
                "Edge AI Systems", "Analyze course Edge AI Systems",
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), "usr-fac-1"
        );

        when(retriever.findCoursesByTitle("run-102", "task-202", "Edge AI Systems", null))
                .thenReturn(List.of(course));
        when(retriever.findOfferingsForCourse("run-102", "task-202", "crs-101"))
                .thenReturn(List.of(draftOffering));
        when(retriever.hasSnapshotsForVersion("cv-dft-1")).thenReturn(false);

        CourseOutcome liveCo = new CourseOutcome("co-live-1", course, "CO1", "Apply quantization", "APPLY");
        when(retriever.findCourseOutcomes("run-102", "task-202", "crs-101")).thenReturn(List.of(liveCo));
        when(retriever.findPrerequisites("run-102", "task-202", "crs-101")).thenReturn(Collections.emptyList());

        StructuredAgentResult result = agent.execute(ctx);

        assertThat(result.executionStatus()).isEqualTo("SUCCESS");
        Map<String, Object> findings = result.findings();
        assertThat(findings.get("historicalSnapshotUsed")).isEqualTo(false);
        assertThat(findings.get("snapshotSource")).isEqualTo("live_catalog");
    }

    @Test
    @DisplayName("Returns NOT_FOUND with related courses when no canonical course exists without fabricating content")
    void missingCourseReturnsNotFoundWithHonestExplanation() throws Exception {
        TaskExecutionContext ctx = new TaskExecutionContext(
                "run-103", "task-203", "task_curriculum_mapping",
                "Quantum Neural Computing", "Analyze Quantum Neural Computing",
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), "usr-fac-1"
        );

        when(retriever.findCoursesByTitle("run-103", "task-203", "Quantum Neural Computing", null))
                .thenReturn(Collections.emptyList());

        Course related = new Course("crs-rel-1", department, "CS305", "Neural Networks", 3, 3, 0, 0);
        when(retriever.findRelatedCourses("run-103", "task-203", "Quantum Neural Computing", null))
                .thenReturn(List.of(related));

        StructuredAgentResult result = agent.execute(ctx);

        assertThat(result.executionStatus()).isEqualTo("SUCCESS");
        Map<String, Object> findings = result.findings();
        assertThat(findings.get("resolutionStatus")).isEqualTo("NOT_FOUND");
        assertThat(findings.get("canonicalCourse")).isNull();
        assertThat(findings.get("summary").toString()).contains("No course matching");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> relatedCourses = (List<Map<String, Object>>) findings.get("relatedCourses");
        assertThat(relatedCourses).hasSize(1);
        assertThat(relatedCourses.get(0).get("code")).isEqualTo("CS305");

        @SuppressWarnings("unchecked")
        List<String> limitations = (List<String>) findings.get("limitations");
        assertThat(limitations).anyMatch(l -> l.contains("No artificial course offering"));
    }

    @Test
    @DisplayName("Returns AMBIGUOUS when subject matches multiple canonical courses across departments")
    void ambiguousSubjectReturnsAmbiguityOptionList() throws Exception {
        TaskExecutionContext ctx = new TaskExecutionContext(
                "run-104", "task-204", "task_curriculum_mapping",
                "Networks", "Analyze Networks",
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), "usr-fac-1"
        );

        Department eceDept = new Department("dept-2", institution, "ECE", "Electronics & Communication");
        Course c1 = new Course("crs-net-1", department, "CS302", "Computer Networks", 4, 3, 0, 2);
        Course c2 = new Course("crs-net-2", eceDept, "EC405", "Communication Networks", 3, 3, 0, 0);

        when(retriever.findCoursesByTitle("run-104", "task-204", "Networks", null))
                .thenReturn(List.of(c1, c2));

        StructuredAgentResult result = agent.execute(ctx);

        assertThat(result.executionStatus()).isEqualTo("SUCCESS");
        Map<String, Object> findings = result.findings();
        assertThat(findings.get("resolutionStatus")).isEqualTo("AMBIGUOUS");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ambiguousOptions = (List<Map<String, Object>>) findings.get("ambiguousOptions");
        assertThat(ambiguousOptions).hasSize(2);
        assertThat(ambiguousOptions).extracting(m -> m.get("code")).containsExactlyInAnyOrder("CS302", "EC405");
    }

    @Test
    @DisplayName("Returns RESOLVED_CANONICAL_ONLY when course exists in catalog but has no curriculum version offering")
    void courseWithoutCurriculumOfferingReturnsCanonicalOnly() throws Exception {
        TaskExecutionContext ctx = new TaskExecutionContext(
                "run-105", "task-205", "task_curriculum_mapping",
                "CS401", "Analyze CS401",
                Map.of("courseCode", "CS401"),
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), "usr-fac-1"
        );

        when(retriever.findCourseByCode("run-105", "task-205", "CS401", null)).thenReturn(Optional.of(course));
        when(retriever.findOfferingsForCourse("run-105", "task-205", "crs-101")).thenReturn(Collections.emptyList());
        when(retriever.findPrerequisites("run-105", "task-205", "crs-101")).thenReturn(Collections.emptyList());
        when(retriever.findCourseOutcomes("run-105", "task-205", "crs-101")).thenReturn(Collections.emptyList());

        StructuredAgentResult result = agent.execute(ctx);

        assertThat(result.executionStatus()).isEqualTo("SUCCESS");
        Map<String, Object> findings = result.findings();
        assertThat(findings.get("resolutionStatus")).isEqualTo("RESOLVED_CANONICAL_ONLY");
        assertThat(findings.get("offering")).isNull();

        @SuppressWarnings("unchecked")
        Map<String, String> dataPresence = (Map<String, String>) findings.get("dataPresence");
        assertThat(dataPresence.get("offering")).isEqualTo("NOT_RECORDED");
        assertThat(dataPresence.get("semester")).isEqualTo("NOT_RECORDED");
    }

    @Test
    @DisplayName("Preserves null values for credits and hours without converting to zero")
    void preservesNullCreditsAndHours() throws Exception {
        Course courseNullCredits = new Course("crs-null", department, "CS999", "Special Topics", null, null, null, null);

        TaskExecutionContext ctx = new TaskExecutionContext(
                "run-106", "task-206", "task_curriculum_mapping",
                "CS999", "Analyze CS999",
                Map.of("courseCode", "CS999"),
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), "usr-fac-1"
        );

        when(retriever.findCourseByCode("run-106", "task-206", "CS999", null)).thenReturn(Optional.of(courseNullCredits));
        when(retriever.findOfferingsForCourse("run-106", "task-206", "crs-null")).thenReturn(Collections.emptyList());
        when(retriever.findPrerequisites("run-106", "task-206", "crs-null")).thenReturn(Collections.emptyList());
        when(retriever.findCourseOutcomes("run-106", "task-206", "crs-null")).thenReturn(Collections.emptyList());

        StructuredAgentResult result = agent.execute(ctx);

        assertThat(result.executionStatus()).isEqualTo("SUCCESS");
        Map<String, Object> findings = result.findings();

        @SuppressWarnings("unchecked")
        Map<String, Object> canonical = (Map<String, Object>) findings.get("canonicalCourse");
        assertThat(canonical.get("defaultCredits")).isNull();
        assertThat(canonical.get("lectureHours")).isNull();
        assertThat(canonical.get("tutorialHours")).isNull();
        assertThat(canonical.get("practicalHours")).isNull();

        @SuppressWarnings("unchecked")
        Map<String, String> dataPresence = (Map<String, String>) findings.get("dataPresence");
        assertThat(dataPresence.get("credits")).isEqualTo("NOT_RECORDED");
        assertThat(dataPresence.get("teachingHours")).isEqualTo("NOT_RECORDED");
    }

    @Test
    @DisplayName("Null task context throws AgentExecutionException")
    void nullContextThrowsException() {
        assertThatThrownBy(() -> agent.execute(null))
                .isInstanceOf(AgentExecutionException.class);
    }
}
