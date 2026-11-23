package org.curriculumai.orchestration.agent.curriculum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.academic.domain.*;
import org.curriculumai.academic.repository.*;
import org.curriculumai.orchestration.agent.AgentRegistry;
import org.curriculumai.orchestration.domain.WorkflowEvent;
import org.curriculumai.orchestration.domain.WorkflowEventType;
import org.curriculumai.orchestration.repository.WorkflowEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@Transactional
public class GovernedAcademicCatalogRetriever {

    private static final Logger log = LoggerFactory.getLogger(GovernedAcademicCatalogRetriever.class);
    private static final int MAX_QUERY_RESULTS = 50;
    private static final String AGENT_ID = "curriculum-mapping-agent";

    private final CourseRepository courseRepository;
    private final CurriculumCourseRepository curriculumCourseRepository;
    private final CurriculumVersionRepository versionRepository;
    private final SemesterRepository semesterRepository;
    private final CoursePrerequisiteRepository prerequisiteRepository;
    private final CourseOutcomeRepository courseOutcomeRepository;
    private final ProgrammeOutcomeRepository programmeOutcomeRepository;
    private final CoPoMappingRepository coPoMappingRepository;
    private final CurriculumOutcomeSnapshotRepository snapshotRepository;
    private final WorkflowEventRepository eventRepository;
    private final AgentRegistry agentRegistry;
    private final ObjectMapper objectMapper;

    public GovernedAcademicCatalogRetriever(
            CourseRepository courseRepository,
            CurriculumCourseRepository curriculumCourseRepository,
            CurriculumVersionRepository versionRepository,
            SemesterRepository semesterRepository,
            CoursePrerequisiteRepository prerequisiteRepository,
            CourseOutcomeRepository courseOutcomeRepository,
            ProgrammeOutcomeRepository programmeOutcomeRepository,
            CoPoMappingRepository coPoMappingRepository,
            CurriculumOutcomeSnapshotRepository snapshotRepository,
            WorkflowEventRepository eventRepository,
            @org.springframework.context.annotation.Lazy AgentRegistry agentRegistry,
            ObjectMapper objectMapper
    ) {
        this.courseRepository = courseRepository;
        this.curriculumCourseRepository = curriculumCourseRepository;
        this.versionRepository = versionRepository;
        this.semesterRepository = semesterRepository;
        this.prerequisiteRepository = prerequisiteRepository;
        this.courseOutcomeRepository = courseOutcomeRepository;
        this.programmeOutcomeRepository = programmeOutcomeRepository;
        this.coPoMappingRepository = coPoMappingRepository;
        this.snapshotRepository = snapshotRepository;
        this.eventRepository = eventRepository;
        this.agentRegistry = agentRegistry;
        this.objectMapper = objectMapper;
    }

    private void verifyAuthorization(String toolId) {
        var agentDef = agentRegistry.getAgentDefinition(AGENT_ID);
        if (agentDef.isEmpty() || !agentDef.get().permittedTools().contains(toolId)) {
            String err = "Agent '" + AGENT_ID + "' is not permitted to access tool '" + toolId + "'";
            log.warn("Security violation: {}", err);
            throw new org.springframework.security.access.AccessDeniedException(err);
        }
    }

    private void auditAccess(String runId, String taskId, String toolId, String summary, Map<String, Object> meta) {
        if (runId == null) return;
        try {
            WorkflowEvent event = new WorkflowEvent(
                    "evt-gov-" + UUID.randomUUID(),
                    runId,
                    taskId,
                    AGENT_ID,
                    WorkflowEventType.TOOL_INVOKED,
                    "Tool '" + toolId + "' invoked by '" + AGENT_ID + "': " + summary,
                    objectMapper.writeValueAsString(meta)
            );
            eventRepository.save(event);
        } catch (Exception e) {
            log.warn("Failed to log governed retrieval audit event: {}", e.getMessage());
        }
    }

    public Optional<Course> findCourseById(String runId, String taskId, String courseId) {
        verifyAuthorization("academic_catalog_retrieval");
        Optional<Course> result = courseRepository.findById(courseId);
        auditAccess(runId, taskId, "academic_catalog_retrieval", "Find course by id: " + courseId,
                Map.of("courseId", courseId, "found", result.isPresent()));
        return result;
    }

    public Optional<Course> findCourseByCode(String runId, String taskId, String code, String departmentId) {
        verifyAuthorization("academic_catalog_retrieval");
        if (code == null || code.isBlank()) return Optional.empty();
        String normalized = code.trim().toUpperCase();

        Optional<Course> course;
        if (departmentId != null && !departmentId.isBlank()) {
            course = courseRepository.findByDepartmentIdAndCode(departmentId, normalized);
        } else {
            course = courseRepository.findByCodeIgnoreCase(normalized);
            if (course.isEmpty()) {
                course = courseRepository.findByCode(normalized);
            }
        }

        auditAccess(runId, taskId, "academic_catalog_retrieval", "Find course by code: " + normalized,
                Map.of("code", normalized, "departmentId", String.valueOf(departmentId), "found", course.isPresent()));
        return course;
    }

    public List<Course> findCoursesByTitle(String runId, String taskId, String titleQuery, String departmentId) {
        verifyAuthorization("academic_catalog_retrieval");
        if (titleQuery == null || titleQuery.isBlank()) return Collections.emptyList();
        String trimmed = titleQuery.trim();

        List<Course> courses;
        if (departmentId != null && !departmentId.isBlank()) {
            courses = courseRepository.findByDepartmentIdAndTitleContainingIgnoreCase(departmentId, trimmed);
        } else {
            courses = courseRepository.findByTitleContainingIgnoreCase(trimmed);
        }

        if (courses.size() > MAX_QUERY_RESULTS) {
            courses = courses.subList(0, MAX_QUERY_RESULTS);
        }

        auditAccess(runId, taskId, "academic_catalog_retrieval", "Find courses by title query: " + trimmed,
                Map.of("query", trimmed, "departmentId", String.valueOf(departmentId), "count", courses.size()));
        return courses;
    }

    public List<CurriculumCourse> findOfferingsForCourse(String runId, String taskId, String courseId) {
        verifyAuthorization("curriculum_version_retrieval");
        List<CurriculumCourse> offerings = curriculumCourseRepository.findByCourseId(courseId);
        auditAccess(runId, taskId, "curriculum_version_retrieval", "Find offerings for course: " + courseId,
                Map.of("courseId", courseId, "count", offerings.size()));
        return offerings;
    }

    public Optional<CurriculumCourse> findOfferingForCourseInVersion(String runId, String taskId, String curriculumVersionId, String courseId) {
        verifyAuthorization("curriculum_version_retrieval");
        Optional<CurriculumCourse> offering = curriculumCourseRepository.findByCurriculumVersionIdAndCourseId(curriculumVersionId, courseId);
        auditAccess(runId, taskId, "curriculum_version_retrieval", "Find offering in version " + curriculumVersionId + " for course: " + courseId,
                Map.of("versionId", curriculumVersionId, "courseId", courseId, "found", offering.isPresent()));
        return offering;
    }

    public Optional<CurriculumVersion> findCurriculumVersion(String runId, String taskId, String versionId) {
        verifyAuthorization("curriculum_version_retrieval");
        Optional<CurriculumVersion> version = versionRepository.findById(versionId);
        auditAccess(runId, taskId, "curriculum_version_retrieval", "Find curriculum version: " + versionId,
                Map.of("versionId", versionId, "found", version.isPresent()));
        return version;
    }

    public List<CurriculumVersion> findCurriculumVersionsByProgramme(String runId, String taskId, String programmeId) {
        verifyAuthorization("curriculum_version_retrieval");
        List<CurriculumVersion> versions = versionRepository.findByProgrammeId(programmeId);
        auditAccess(runId, taskId, "curriculum_version_retrieval", "Find curriculum versions by programme: " + programmeId,
                Map.of("programmeId", programmeId, "count", versions.size()));
        return versions;
    }

    public List<CoursePrerequisite> findPrerequisites(String runId, String taskId, String courseId) {
        verifyAuthorization("academic_catalog_retrieval");
        List<CoursePrerequisite> prereqs = prerequisiteRepository.findByCourseId(courseId);
        auditAccess(runId, taskId, "academic_catalog_retrieval", "Find prerequisites for course: " + courseId,
                Map.of("courseId", courseId, "count", prereqs.size()));
        return prereqs;
    }

    public List<CourseOutcome> findCourseOutcomes(String runId, String taskId, String courseId) {
        verifyAuthorization("academic_catalog_retrieval");
        List<CourseOutcome> outcomes = courseOutcomeRepository.findByCourseIdOrderByCodeAsc(courseId);
        auditAccess(runId, taskId, "academic_catalog_retrieval", "Find course outcomes: " + courseId,
                Map.of("courseId", courseId, "count", outcomes.size()));
        return outcomes;
    }

    public List<ProgrammeOutcome> findProgrammeOutcomes(String runId, String taskId, String programmeId) {
        verifyAuthorization("curriculum_version_retrieval");
        List<ProgrammeOutcome> pos = programmeOutcomeRepository.findByProgrammeIdOrderByCodeAsc(programmeId);
        auditAccess(runId, taskId, "curriculum_version_retrieval", "Find programme outcomes: " + programmeId,
                Map.of("programmeId", programmeId, "count", pos.size()));
        return pos;
    }

    public List<CoPoMapping> findCoPoMappings(String runId, String taskId, String courseOutcomeId) {
        verifyAuthorization("curriculum_version_retrieval");
        List<CoPoMapping> maps = coPoMappingRepository.findByCourseOutcomeId(courseOutcomeId);
        auditAccess(runId, taskId, "curriculum_version_retrieval", "Find CO-PO mappings for CO: " + courseOutcomeId,
                Map.of("courseOutcomeId", courseOutcomeId, "count", maps.size()));
        return maps;
    }

    public boolean hasSnapshotsForVersion(String curriculumVersionId) {
        return snapshotRepository.existsByCurriculumVersionId(curriculumVersionId);
    }

    public List<CurriculumOutcomeSnapshot> findOutcomeSnapshotsForCourse(String runId, String taskId, String curriculumVersionId, String courseId) {
        verifyAuthorization("curriculum_version_retrieval");
        List<CurriculumOutcomeSnapshot> snapshots = snapshotRepository.findByCurriculumVersionIdAndCourseId(curriculumVersionId, courseId);
        auditAccess(runId, taskId, "curriculum_version_retrieval", "Find outcome snapshots for version " + curriculumVersionId + ", course: " + courseId,
                Map.of("curriculumVersionId", curriculumVersionId, "courseId", courseId, "count", snapshots.size()));
        return snapshots;
    }

    public List<CurriculumOutcomeSnapshot> findProgrammeOutcomeSnapshots(String runId, String taskId, String curriculumVersionId) {
        verifyAuthorization("curriculum_version_retrieval");
        List<CurriculumOutcomeSnapshot> snapshots = snapshotRepository.findByCurriculumVersionIdAndSnapshotType(curriculumVersionId, "PROGRAMME_OUTCOME");
        auditAccess(runId, taskId, "curriculum_version_retrieval", "Find PO snapshots for version: " + curriculumVersionId,
                Map.of("curriculumVersionId", curriculumVersionId, "count", snapshots.size()));
        return snapshots;
    }

    public List<Course> findRelatedCourses(String runId, String taskId, String subject, String excludeCourseId) {
        verifyAuthorization("academic_catalog_retrieval");
        if (subject == null || subject.isBlank()) return Collections.emptyList();

        String[] tokens = subject.split("\\s+");
        Set<String> addedIds = new HashSet<>();
        if (excludeCourseId != null) addedIds.add(excludeCourseId);

        List<Course> related = new ArrayList<>();
        for (String token : tokens) {
            if (token.length() < 3) continue; // skip short stop words
            List<Course> candidates = courseRepository.findByTitleContainingIgnoreCase(token);
            for (Course c : candidates) {
                if (addedIds.add(c.getId())) {
                    related.add(c);
                    if (related.size() >= 5) break;
                }
            }
            if (related.size() >= 5) break;
        }

        auditAccess(runId, taskId, "academic_catalog_retrieval", "Find related courses for: " + subject,
                Map.of("subject", subject, "count", related.size()));
        return related;
    }
}

