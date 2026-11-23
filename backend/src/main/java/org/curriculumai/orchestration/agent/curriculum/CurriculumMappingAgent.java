package org.curriculumai.orchestration.agent.curriculum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.academic.domain.*;
import org.curriculumai.orchestration.agent.AgentExecutionException;
import org.curriculumai.orchestration.agent.SpecialistAgent;
import org.curriculumai.orchestration.agent.StructuredAgentResult;
import org.curriculumai.orchestration.agent.TaskExecutionContext;
import org.curriculumai.orchestration.domain.AgentStatus;
import org.curriculumai.orchestration.llm.LlmExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Transactional
public class CurriculumMappingAgent implements SpecialistAgent {

    private static final Logger log = LoggerFactory.getLogger(CurriculumMappingAgent.class);
    public static final String AGENT_ID = "curriculum-mapping-agent";

    private static final Pattern COURSE_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{2,6}\\s?[0-9]{2,4}[A-Z0-9]?$", Pattern.CASE_INSENSITIVE);

    private final GovernedAcademicCatalogRetriever retriever;
    private final LlmExecutionService llmExecutionService;
    private final ObjectMapper objectMapper;

    public CurriculumMappingAgent(
            GovernedAcademicCatalogRetriever retriever,
            LlmExecutionService llmExecutionService,
            ObjectMapper objectMapper
    ) {
        this.retriever = retriever;
        this.llmExecutionService = llmExecutionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getAgentId() {
        return AGENT_ID;
    }

    @Override
    public String getDisplayName() {
        return "Curriculum Mapping Agent";
    }

    @Override
    public String getDescription() {
        return "Analyzes institutional course structures, outcomes (CO-PO), Bloom taxonomy levels, and prerequisites";
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
        String rawPrompt = ctx.rawPrompt();
        Map<String, Object> scope = ctx.academicScope() != null ? ctx.academicScope() : Collections.emptyMap();

        log.info("CurriculumMappingAgent executing task {} in run {} for subject '{}'", taskId, runId, subject);

        // Normalize subject
        final String resolvedQuery;
        if (subject != null && !subject.isBlank()) {
            resolvedQuery = subject.trim();
        } else if (rawPrompt != null && !rawPrompt.isBlank()) {
            resolvedQuery = rawPrompt.trim();
        } else {
            resolvedQuery = "";
        }

        if (resolvedQuery.isBlank()) {
            CurriculumMappingResult result = CurriculumMappingResult.builder("UNKNOWN", CurriculumMappingResult.ResolutionStatus.ERROR)
                    .summary("Subject or course name must be provided in task execution context.")
                    .limitations(List.of("Missing query/subject parameter."))
                    .build();
            return new StructuredAgentResult(
                    runId, taskId, AGENT_ID, "FAILED",
                    result.toMap(objectMapper), Collections.emptyList(),
                    List.of("Missing query/subject parameter."),
                    "Subject parameter is missing",
                    Map.of("executionDurationMs", System.currentTimeMillis() - startMs)
            );
        }

        // Extract scope filters if provided
        String scopeCourseCode = (String) scope.get("courseCode");
        String scopeCourseId = (String) scope.get("courseId");
        String scopeDepartmentId = (String) scope.get("departmentId");
        String scopeProgrammeId = (String) scope.get("programmeId");
        String scopeCurriculumVersionId = (String) scope.get("curriculumVersionId");

        // 1. Resolve Candidate Course
        List<Course> candidateCourses = new ArrayList<>();

        if (scopeCourseId != null && !scopeCourseId.isBlank()) {
            retriever.findCourseById(runId, taskId, scopeCourseId).ifPresent(candidateCourses::add);
        }
        if (candidateCourses.isEmpty() && scopeCourseCode != null && !scopeCourseCode.isBlank()) {
            retriever.findCourseByCode(runId, taskId, scopeCourseCode, scopeDepartmentId).ifPresent(candidateCourses::add);
        }
        if (candidateCourses.isEmpty()) {
            retriever.findCourseByCode(runId, taskId, resolvedQuery, scopeDepartmentId).ifPresent(candidateCourses::add);
        }
        if (candidateCourses.isEmpty()) {
            candidateCourses = retriever.findCoursesByTitle(runId, taskId, resolvedQuery, scopeDepartmentId);
        }

        // 2. Evaluate Resolution
        if (candidateCourses.isEmpty()) {
            return handleNotFound(runId, taskId, resolvedQuery, startMs);
        }

        if (candidateCourses.size() > 1) {
            // Check for exact title match among candidates
            List<Course> exactMatches = candidateCourses.stream()
                    .filter(c -> c.getTitle().equalsIgnoreCase(resolvedQuery))
                    .toList();

            if (exactMatches.size() == 1) {
                candidateCourses = exactMatches;
            } else {
                return handleAmbiguity(runId, taskId, resolvedQuery, candidateCourses, startMs);
            }
        }

        // 3. Exactly 1 Course Resolved
        Course resolvedCourse = candidateCourses.get(0);
        return mapResolvedCourse(runId, taskId, resolvedQuery, resolvedCourse, scopeProgrammeId, scopeCurriculumVersionId, startMs);
    }

    private StructuredAgentResult handleNotFound(String runId, String taskId, String subject, long startMs) {
        List<Course> related = retriever.findRelatedCourses(runId, taskId, subject, null);
        List<CurriculumMappingResult.RelatedCourseItem> relatedItems = related.stream()
                .map(r -> new CurriculumMappingResult.RelatedCourseItem(
                        r.getId(),
                        r.getCode(),
                        r.getTitle(),
                        r.getDepartment() != null ? r.getDepartment().getName() : "Unknown Department",
                        "Topical keyword overlap in institutional catalog"
                ))
                .toList();

        Map<String, String> dataPresence = new LinkedHashMap<>();
        dataPresence.put("canonicalCourse", CurriculumMappingResult.DataPresence.NOT_FOUND.name());
        dataPresence.put("offering", CurriculumMappingResult.DataPresence.NOT_FOUND.name());
        dataPresence.put("syllabusUnits", CurriculumMappingResult.DataPresence.NOT_RECORDED.name());

        CurriculumMappingResult result = CurriculumMappingResult.builder(subject, CurriculumMappingResult.ResolutionStatus.NOT_FOUND)
                .summary("No course matching '" + subject + "' was found in the institutional academic catalog.")
                .relatedCourses(relatedItems)
                .dataPresence(dataPresence)
                .limitations(List.of(
                        "The institutional curriculum database contains no matching course for '" + subject + "'.",
                        "No artificial course offering, credits, or syllabus was fabricated."
                ))
                .build();

        long duration = System.currentTimeMillis() - startMs;
        return new StructuredAgentResult(
                runId,
                taskId,
                AGENT_ID,
                "SUCCESS",
                result.toMap(objectMapper),
                related.stream().map(Course::getId).toList(),
                result.limitations(),
                null,
                Map.of("resolutionStatus", "NOT_FOUND", "executionDurationMs", duration)
        );
    }

    private StructuredAgentResult handleAmbiguity(String runId, String taskId, String subject, List<Course> candidates, long startMs) {
        List<CurriculumMappingResult.AmbiguityOption> options = candidates.stream()
                .map(c -> new CurriculumMappingResult.AmbiguityOption(
                        c.getId(),
                        c.getCode(),
                        c.getTitle(),
                        c.getDepartment() != null ? c.getDepartment().getCode() : "N/A",
                        null,
                        null,
                        null,
                        "Specify course code '" + c.getCode() + "' or department '" + (c.getDepartment() != null ? c.getDepartment().getCode() : "") + "' to map this course."
                ))
                .toList();

        Map<String, String> dataPresence = new LinkedHashMap<>();
        dataPresence.put("canonicalCourse", CurriculumMappingResult.DataPresence.AMBIGUOUS.name());
        dataPresence.put("offering", CurriculumMappingResult.DataPresence.AMBIGUOUS.name());

        CurriculumMappingResult result = CurriculumMappingResult.builder(subject, CurriculumMappingResult.ResolutionStatus.AMBIGUOUS)
                .summary("The subject '" + subject + "' matches " + candidates.size() + " institutional courses. Explicit department or course code clarification is required.")
                .ambiguousOptions(options)
                .dataPresence(dataPresence)
                .limitations(List.of(
                        "Multiple candidate courses matched. The agent did not guess or select an arbitrary course.",
                        "Faculty clarification is required to narrow down the academic scope."
                ))
                .unresolvedAmbiguities(List.of("Matching candidate courses: " + candidates.stream().map(Course::getCode).collect(Collectors.joining(", "))))
                .build();

        long duration = System.currentTimeMillis() - startMs;
        return new StructuredAgentResult(
                runId,
                taskId,
                AGENT_ID,
                "SUCCESS",
                result.toMap(objectMapper),
                candidates.stream().map(Course::getId).toList(),
                result.limitations(),
                null,
                Map.of("resolutionStatus", "AMBIGUOUS", "executionDurationMs", duration)
        );
    }

    private StructuredAgentResult mapResolvedCourse(
            String runId,
            String taskId,
            String subject,
            Course course,
            String scopeProgrammeId,
            String scopeCurriculumVersionId,
            long startMs
    ) {
        Map<String, List<String>> sourceRecordIds = new LinkedHashMap<>();
        sourceRecordIds.computeIfAbsent("courses", k -> new ArrayList<>()).add(course.getId());

        Department dept = course.getDepartment();
        Institution inst = dept != null ? dept.getInstitution() : null;
        if (dept != null) sourceRecordIds.computeIfAbsent("departments", k -> new ArrayList<>()).add(dept.getId());
        if (inst != null) sourceRecordIds.computeIfAbsent("institutions", k -> new ArrayList<>()).add(inst.getId());

        CurriculumMappingResult.CanonicalCourseInfo canonicalInfo = new CurriculumMappingResult.CanonicalCourseInfo(
                course.getId(),
                course.getCode(),
                course.getTitle(),
                course.getDescription(),
                course.getCourseType(),
                course.getDefaultCredits(),
                course.getLectureHours(),
                course.getTutorialHours(),
                course.getPracticalHours()
        );

        // 4. Resolve Curriculum Offering
        List<CurriculumCourse> offerings = retriever.findOfferingsForCourse(runId, taskId, course.getId());

        if (scopeCurriculumVersionId != null && !scopeCurriculumVersionId.isBlank()) {
            offerings = offerings.stream()
                    .filter(o -> o.getCurriculumVersion() != null && o.getCurriculumVersion().getId().equals(scopeCurriculumVersionId))
                    .toList();
        } else if (scopeProgrammeId != null && !scopeProgrammeId.isBlank()) {
            offerings = offerings.stream()
                    .filter(o -> o.getCurriculumVersion() != null && o.getCurriculumVersion().getProgramme() != null
                            && o.getCurriculumVersion().getProgramme().getId().equals(scopeProgrammeId))
                    .toList();
        }

        CurriculumCourse chosenOffering = null;
        CurriculumVersion chosenVersion = null;
        Programme chosenProgramme = null;
        Semester chosenSemester = null;

        CurriculumMappingResult.ResolutionStatus status = CurriculumMappingResult.ResolutionStatus.RESOLVED;

        if (offerings.isEmpty()) {
            status = CurriculumMappingResult.ResolutionStatus.RESOLVED_CANONICAL_ONLY;
        } else if (offerings.size() == 1) {
            chosenOffering = offerings.get(0);
        } else {
            // Check if offerings span multiple distinct programmes
            Set<String> programmeIds = offerings.stream()
                    .map(o -> o.getCurriculumVersion().getProgramme().getId())
                    .collect(Collectors.toSet());

            if (programmeIds.size() > 1) {
                // Ambiguous across programmes
                List<CurriculumMappingResult.AmbiguityOption> options = offerings.stream()
                        .map(o -> new CurriculumMappingResult.AmbiguityOption(
                                course.getId(),
                                course.getCode(),
                                course.getTitle(),
                                dept != null ? dept.getCode() : "N/A",
                                o.getCurriculumVersion().getProgramme().getName(),
                                o.getCurriculumVersion().getVersionCode(),
                                o.getCurriculumVersion().getLifecycleStatus().name(),
                                "Offered in programme " + o.getCurriculumVersion().getProgramme().getName() + " (" + o.getCurriculumVersion().getVersionCode() + ")"
                        ))
                        .toList();

                CurriculumMappingResult result = CurriculumMappingResult.builder(subject, CurriculumMappingResult.ResolutionStatus.AMBIGUOUS)
                        .summary("Course '" + course.getCode() + "' is offered across multiple distinct programmes. Clarification of target programme is required.")
                        .canonicalCourse(canonicalInfo)
                        .ambiguousOptions(options)
                        .limitations(List.of("Course exists in multiple programmes. Scope clarification required."))
                        .build();

                long duration = System.currentTimeMillis() - startMs;
                return new StructuredAgentResult(
                        runId, taskId, AGENT_ID, "SUCCESS",
                        result.toMap(objectMapper), List.of(course.getId()),
                        result.limitations(), null,
                        Map.of("resolutionStatus", "AMBIGUOUS", "executionDurationMs", duration)
                );
            }

            // Same programme: prefer APPROVED version
            chosenOffering = offerings.stream()
                    .filter(o -> o.getCurriculumVersion().getLifecycleStatus() == CurriculumLifecycleStatus.APPROVED)
                    .findFirst()
                    .orElse(offerings.get(0));
        }

        if (chosenOffering != null) {
            chosenSemester = chosenOffering.getSemester();
            chosenVersion = chosenOffering.getCurriculumVersion();
            chosenProgramme = chosenVersion != null ? chosenVersion.getProgramme() : null;

            sourceRecordIds.computeIfAbsent("curriculum_courses", k -> new ArrayList<>()).add(chosenOffering.getId());
            if (chosenSemester != null) sourceRecordIds.computeIfAbsent("semesters", k -> new ArrayList<>()).add(chosenSemester.getId());
            if (chosenVersion != null) sourceRecordIds.computeIfAbsent("curriculum_versions", k -> new ArrayList<>()).add(chosenVersion.getId());
            if (chosenProgramme != null) sourceRecordIds.computeIfAbsent("programmes", k -> new ArrayList<>()).add(chosenProgramme.getId());
        }

        // 5. Prerequisites
        List<CoursePrerequisite> prereqs = retriever.findPrerequisites(runId, taskId, course.getId());
        List<CurriculumMappingResult.PrerequisiteItem> prereqItems = new ArrayList<>();
        for (CoursePrerequisite cp : prereqs) {
            sourceRecordIds.computeIfAbsent("course_prerequisites", k -> new ArrayList<>()).add(cp.getId());
            Course pCourse = cp.getPrerequisiteCourse();
            prereqItems.add(new CurriculumMappingResult.PrerequisiteItem(
                    cp.getId(),
                    pCourse.getId(),
                    pCourse.getCode(),
                    pCourse.getTitle(),
                    cp.getRequirementType(),
                    cp.getMinimumGrade()
            ));
        }

        // 6. Outcomes & Mappings (Preserve Historical Snapshots for Approved Versions!)
        boolean snapshotUsed = false;
        String snapshotSource = "live_catalog";
        List<CurriculumMappingResult.CourseOutcomeItem> coItems = new ArrayList<>();
        List<CurriculumMappingResult.ProgrammeOutcomeItem> poItems = new ArrayList<>();
        List<CurriculumMappingResult.CoPoMappingItem> copoItems = new ArrayList<>();

        if (chosenVersion != null && retriever.hasSnapshotsForVersion(chosenVersion.getId())) {
            // Use immutable snapshots
            snapshotUsed = true;
            snapshotSource = "curriculum_outcome_snapshots";

            List<CurriculumOutcomeSnapshot> courseSnapshots = retriever.findOutcomeSnapshotsForCourse(runId, taskId, chosenVersion.getId(), course.getId());
            List<CurriculumOutcomeSnapshot> poSnapshots = retriever.findProgrammeOutcomeSnapshots(runId, taskId, chosenVersion.getId());

            for (CurriculumOutcomeSnapshot s : courseSnapshots) {
                sourceRecordIds.computeIfAbsent("curriculum_outcome_snapshots", k -> new ArrayList<>()).add(s.getId());
                if ("COURSE_OUTCOME".equals(s.getSnapshotType())) {
                    coItems.add(new CurriculumMappingResult.CourseOutcomeItem(
                            s.getSourceEntityId(),
                            s.getOutcomeCode(),
                            s.getDescription(),
                            s.getBloomLevel()
                    ));
                } else if ("CO_PO_MAPPING".equals(s.getSnapshotType())) {
                    copoItems.add(new CurriculumMappingResult.CoPoMappingItem(
                            s.getSourceEntityId(),
                            s.getOutcomeCode(),
                            s.getTargetOutcomeCode(),
                            s.getMappingLevel() != null ? s.getMappingLevel() : 0,
                            s.getJustification()
                    ));
                }
            }

            for (CurriculumOutcomeSnapshot poSnap : poSnapshots) {
                sourceRecordIds.computeIfAbsent("curriculum_outcome_snapshots", k -> new ArrayList<>()).add(poSnap.getId());
                poItems.add(new CurriculumMappingResult.ProgrammeOutcomeItem(
                        poSnap.getSourceEntityId(),
                        poSnap.getOutcomeCode(),
                        poSnap.getTitle(),
                        poSnap.getDescription(),
                        "PO"
                ));
            }
        } else {
            // Use live catalog
            List<CourseOutcome> liveCos = retriever.findCourseOutcomes(runId, taskId, course.getId());
            for (CourseOutcome co : liveCos) {
                sourceRecordIds.computeIfAbsent("course_outcomes", k -> new ArrayList<>()).add(co.getId());
                coItems.add(new CurriculumMappingResult.CourseOutcomeItem(
                        co.getId(),
                        co.getCode(),
                        co.getDescription(),
                        co.getBloomLevel()
                ));

                List<CoPoMapping> maps = retriever.findCoPoMappings(runId, taskId, co.getId());
                for (CoPoMapping m : maps) {
                    sourceRecordIds.computeIfAbsent("co_po_mappings", k -> new ArrayList<>()).add(m.getId());
                    copoItems.add(new CurriculumMappingResult.CoPoMappingItem(
                            m.getId(),
                            m.getCourseOutcome().getCode(),
                            m.getProgrammeOutcome().getCode(),
                            m.getMappingLevel(),
                            m.getJustification()
                    ));
                }
            }

            if (chosenProgramme != null) {
                List<ProgrammeOutcome> livePos = retriever.findProgrammeOutcomes(runId, taskId, chosenProgramme.getId());
                for (ProgrammeOutcome po : livePos) {
                    sourceRecordIds.computeIfAbsent("programme_outcomes", k -> new ArrayList<>()).add(po.getId());
                    poItems.add(new CurriculumMappingResult.ProgrammeOutcomeItem(
                            po.getId(),
                            po.getCode(),
                            po.getTitle(),
                            po.getDescription(),
                            po.getOutcomeType()
                    ));
                }
            }
        }

        // 7. Track Data Presence
        Map<String, String> dataPresence = new LinkedHashMap<>();
        dataPresence.put("canonicalCourse", CurriculumMappingResult.DataPresence.PRESENT.name());
        dataPresence.put("offering", chosenOffering != null ? CurriculumMappingResult.DataPresence.PRESENT.name() : CurriculumMappingResult.DataPresence.NOT_RECORDED.name());
        dataPresence.put("semester", chosenSemester != null ? CurriculumMappingResult.DataPresence.PRESENT.name() : CurriculumMappingResult.DataPresence.NOT_RECORDED.name());
        dataPresence.put("prerequisites", prereqs.isEmpty() ? CurriculumMappingResult.DataPresence.NOT_RECORDED.name() : CurriculumMappingResult.DataPresence.PRESENT.name());
        dataPresence.put("courseOutcomes", coItems.isEmpty() ? CurriculumMappingResult.DataPresence.NOT_RECORDED.name() : CurriculumMappingResult.DataPresence.PRESENT.name());
        dataPresence.put("programmeOutcomes", poItems.isEmpty() ? CurriculumMappingResult.DataPresence.NOT_RECORDED.name() : CurriculumMappingResult.DataPresence.PRESENT.name());
        dataPresence.put("coPoMappings", copoItems.isEmpty() ? CurriculumMappingResult.DataPresence.NOT_RECORDED.name() : CurriculumMappingResult.DataPresence.PRESENT.name());
        dataPresence.put("syllabusUnits", CurriculumMappingResult.DataPresence.NOT_RECORDED.name());
        dataPresence.put("credits", (chosenOffering != null && chosenOffering.getCredits() != null) || course.getDefaultCredits() != null ? CurriculumMappingResult.DataPresence.PRESENT.name() : CurriculumMappingResult.DataPresence.NOT_RECORDED.name());
        dataPresence.put("teachingHours", (chosenOffering != null && chosenOffering.getLectureHours() != null) || course.getLectureHours() != null ? CurriculumMappingResult.DataPresence.PRESENT.name() : CurriculumMappingResult.DataPresence.NOT_RECORDED.name());

        // 8. Build Result Models
        CurriculumMappingResult.InstitutionScope instScope = inst != null ? new CurriculumMappingResult.InstitutionScope(inst.getId(), inst.getCode(), inst.getName()) : null;
        CurriculumMappingResult.DepartmentScope deptScope = dept != null ? new CurriculumMappingResult.DepartmentScope(dept.getId(), dept.getCode(), dept.getName()) : null;
        CurriculumMappingResult.ProgrammeScope progScope = chosenProgramme != null ? new CurriculumMappingResult.ProgrammeScope(chosenProgramme.getId(), chosenProgramme.getCode(), chosenProgramme.getName(), chosenProgramme.getDegreeLevel()) : null;
        CurriculumMappingResult.VersionScope verScope = chosenVersion != null ? new CurriculumMappingResult.VersionScope(chosenVersion.getId(), chosenVersion.getVersionCode(), chosenVersion.getEffectiveYear(), chosenVersion.getLifecycleStatus().name()) : null;

        CurriculumMappingResult.OfferingInfo offeringInfo = chosenOffering != null ? new CurriculumMappingResult.OfferingInfo(
                chosenOffering.getId(),
                chosenVersion != null ? chosenVersion.getId() : null,
                chosenVersion != null ? chosenVersion.getVersionCode() : null,
                chosenVersion != null ? chosenVersion.getEffectiveYear() : null,
                chosenVersion != null ? chosenVersion.getLifecycleStatus().name() : null,
                chosenSemester != null ? chosenSemester.getId() : null,
                chosenSemester != null ? chosenSemester.getSemesterNumber() : null,
                chosenSemester != null ? chosenSemester.getLabel() : null,
                chosenOffering.getCredits(),
                chosenOffering.getLectureHours(),
                chosenOffering.getTutorialHours(),
                chosenOffering.getPracticalHours(),
                chosenOffering.isElective(),
                chosenOffering.getElectiveGroup()
        ) : null;

        String summary = generateDeterministicSummary(course, chosenOffering, chosenSemester, chosenVersion, prereqItems, coItems, copoItems, snapshotUsed);

        List<String> limitations = new ArrayList<>();
        if (chosenOffering == null) {
            limitations.add("Course is registered in the canonical academic catalog but not allocated to a semester offering.");
        }
        if (snapshotUsed) {
            limitations.add("Using immutable outcome snapshots from approved curriculum version " + chosenVersion.getVersionCode() + ".");
        }
        limitations.add("Topic-level syllabus units and module breakdowns are NOT_RECORDED in the current academic domain schema.");

        CurriculumMappingResult result = CurriculumMappingResult.builder(subject, status)
                .summary(summary)
                .institution(instScope)
                .department(deptScope)
                .programme(progScope)
                .curriculumVersion(verScope)
                .canonicalCourse(canonicalInfo)
                .offering(offeringInfo)
                .prerequisites(prereqItems)
                .courseOutcomes(coItems)
                .programmeOutcomes(poItems)
                .coPoMappings(copoItems)
                .historicalSnapshotUsed(snapshotUsed, snapshotSource)
                .dataPresence(dataPresence)
                .sourceRecordIdentifiers(sourceRecordIds)
                .limitations(limitations)
                .build();

        long duration = System.currentTimeMillis() - startMs;
        List<String> allEvidenceIds = sourceRecordIds.values().stream().flatMap(List::stream).distinct().toList();

        return new StructuredAgentResult(
                runId,
                taskId,
                AGENT_ID,
                "SUCCESS",
                result.toMap(objectMapper),
                allEvidenceIds,
                result.limitations(),
                null,
                Map.of(
                        "resolutionStatus", status.name(),
                        "historicalSnapshotUsed", snapshotUsed,
                        "executionDurationMs", duration,
                        "recordsRetrievedCount", allEvidenceIds.size()
                )
        );
    }

    private String generateDeterministicSummary(
            Course course,
            CurriculumCourse offering,
            Semester sem,
            CurriculumVersion cv,
            List<CurriculumMappingResult.PrerequisiteItem> prereqs,
            List<CurriculumMappingResult.CourseOutcomeItem> cos,
            List<CurriculumMappingResult.CoPoMappingItem> copo,
            boolean snapshotUsed
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Institutional mapping established for course '").append(course.getCode()).append(" - ").append(course.getTitle()).append("'. ");

        if (offering != null && cv != null && sem != null) {
            sb.append("Allocated to Semester ").append(sem.getSemesterNumber()).append(" (").append(sem.getLabel()).append(")")
                    .append(" under Curriculum Version ").append(cv.getVersionCode())
                    .append(" [Lifecycle: ").append(cv.getLifecycleStatus()).append("]. ")
                    .append("Credits: ").append(offering.getCredits() != null ? offering.getCredits() : "NOT_RECORDED")
                    .append(" (L: ").append(offering.getLectureHours() != null ? offering.getLectureHours() : "-")
                    .append(", T: ").append(offering.getTutorialHours() != null ? offering.getTutorialHours() : "-")
                    .append(", P: ").append(offering.getPracticalHours() != null ? offering.getPracticalHours() : "-").append("). ");
        } else {
            sb.append("Canonical catalog entry found; no active semester offering or curriculum allocation recorded. ");
        }

        sb.append("Prerequisites: ").append(prereqs.isEmpty() ? "None recorded" : prereqs.stream().map(CurriculumMappingResult.PrerequisiteItem::courseCode).collect(Collectors.joining(", "))).append(". ");
        sb.append("Course Outcomes (COs): ").append(cos.size()).append(" defined");
        if (snapshotUsed) {
            sb.append(" (retrieved from immutable curriculum outcome snapshot)");
        }
        sb.append(". CO-PO Mappings: ").append(copo.size()).append(" mappings recorded.");

        return sb.toString();
    }
}

