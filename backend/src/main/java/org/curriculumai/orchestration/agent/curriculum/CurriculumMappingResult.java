package org.curriculumai.orchestration.agent.curriculum;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CurriculumMappingResult(
        String subject,
        ResolutionStatus resolutionStatus,
        String summary,
        InstitutionScope institution,
        DepartmentScope department,
        ProgrammeScope programme,
        VersionScope curriculumVersion,
        CanonicalCourseInfo canonicalCourse,
        OfferingInfo offering,
        List<PrerequisiteItem> prerequisites,
        List<CourseOutcomeItem> courseOutcomes,
        List<ProgrammeOutcomeItem> programmeOutcomes,
        List<CoPoMappingItem> coPoMappings,
        boolean historicalSnapshotUsed,
        String snapshotSource,
        List<RelatedCourseItem> relatedCourses,
        List<AmbiguityOption> ambiguousOptions,
        Map<String, String> dataPresence,
        Map<String, List<String>> sourceRecordIdentifiers,
        List<String> limitations,
        List<String> unresolvedAmbiguities
) {

    public enum ResolutionStatus {
        RESOLVED,
        RESOLVED_CANONICAL_ONLY,
        AMBIGUOUS,
        NOT_FOUND,
        ERROR
    }

    public enum DataPresence {
        PRESENT,
        NOT_RECORDED,
        NOT_APPLICABLE,
        AMBIGUOUS,
        NOT_FOUND
    }

    public record InstitutionScope(String id, String code, String name) {}
    public record DepartmentScope(String id, String code, String name) {}
    public record ProgrammeScope(String id, String code, String name, String degreeLevel) {}
    public record VersionScope(String id, String versionCode, Integer effectiveYear, String lifecycleStatus) {}

    public record CanonicalCourseInfo(
            String courseId,
            String code,
            String title,
            String description,
            String courseType,
            Integer defaultCredits,
            Integer lectureHours,
            Integer tutorialHours,
            Integer practicalHours
    ) {}

    public record OfferingInfo(
            String curriculumCourseId,
            String curriculumVersionId,
            String versionCode,
            Integer effectiveYear,
            String lifecycleStatus,
            String semesterId,
            Integer semesterNumber,
            String semesterLabel,
            Integer credits,
            Integer lectureHours,
            Integer tutorialHours,
            Integer practicalHours,
            boolean isElective,
            String electiveGroup
    ) {}

    public record PrerequisiteItem(
            String prerequisiteId,
            String courseId,
            String courseCode,
            String courseTitle,
            String requirementType,
            String minimumGrade
    ) {}

    public record CourseOutcomeItem(
            String courseOutcomeId,
            String code,
            String description,
            String bloomLevel
    ) {}

    public record ProgrammeOutcomeItem(
            String programmeOutcomeId,
            String code,
            String title,
            String description,
            String outcomeType
    ) {}

    public record CoPoMappingItem(
            String mappingId,
            String courseOutcomeCode,
            String programmeOutcomeCode,
            int mappingLevel,
            String justification
    ) {}

    public record RelatedCourseItem(
            String courseId,
            String code,
            String title,
            String departmentName,
            String relevanceReason
    ) {}

    public record AmbiguityOption(
            String courseId,
            String code,
            String title,
            String departmentCode,
            String programmeName,
            String versionCode,
            String lifecycleStatus,
            String clarificationPrompt
    ) {}

    public Map<String, Object> toMap(ObjectMapper objectMapper) {
        return objectMapper.convertValue(this, new TypeReference<Map<String, Object>>() {});
    }

    public static Builder builder(String subject, ResolutionStatus resolutionStatus) {
        return new Builder(subject, resolutionStatus);
    }

    public static class Builder {
        private final String subject;
        private final ResolutionStatus resolutionStatus;
        private String summary;
        private InstitutionScope institution;
        private DepartmentScope department;
        private ProgrammeScope programme;
        private VersionScope curriculumVersion;
        private CanonicalCourseInfo canonicalCourse;
        private OfferingInfo offering;
        private List<PrerequisiteItem> prerequisites = new ArrayList<>();
        private List<CourseOutcomeItem> courseOutcomes = new ArrayList<>();
        private List<ProgrammeOutcomeItem> programmeOutcomes = new ArrayList<>();
        private List<CoPoMappingItem> coPoMappings = new ArrayList<>();
        private boolean historicalSnapshotUsed = false;
        private String snapshotSource = "live_catalog";
        private List<RelatedCourseItem> relatedCourses = new ArrayList<>();
        private List<AmbiguityOption> ambiguousOptions = new ArrayList<>();
        private Map<String, String> dataPresence = new LinkedHashMap<>();
        private Map<String, List<String>> sourceRecordIdentifiers = new LinkedHashMap<>();
        private List<String> limitations = new ArrayList<>();
        private List<String> unresolvedAmbiguities = new ArrayList<>();

        public Builder(String subject, ResolutionStatus resolutionStatus) {
            this.subject = subject;
            this.resolutionStatus = resolutionStatus;
        }

        public Builder summary(String summary) { this.summary = summary; return this; }
        public Builder institution(InstitutionScope institution) { this.institution = institution; return this; }
        public Builder department(DepartmentScope department) { this.department = department; return this; }
        public Builder programme(ProgrammeScope programme) { this.programme = programme; return this; }
        public Builder curriculumVersion(VersionScope curriculumVersion) { this.curriculumVersion = curriculumVersion; return this; }
        public Builder canonicalCourse(CanonicalCourseInfo canonicalCourse) { this.canonicalCourse = canonicalCourse; return this; }
        public Builder offering(OfferingInfo offering) { this.offering = offering; return this; }
        public Builder prerequisites(List<PrerequisiteItem> prerequisites) { this.prerequisites = prerequisites; return this; }
        public Builder courseOutcomes(List<CourseOutcomeItem> courseOutcomes) { this.courseOutcomes = courseOutcomes; return this; }
        public Builder programmeOutcomes(List<ProgrammeOutcomeItem> programmeOutcomes) { this.programmeOutcomes = programmeOutcomes; return this; }
        public Builder coPoMappings(List<CoPoMappingItem> coPoMappings) { this.coPoMappings = coPoMappings; return this; }
        public Builder historicalSnapshotUsed(boolean used, String source) {
            this.historicalSnapshotUsed = used;
            this.snapshotSource = source;
            return this;
        }
        public Builder relatedCourses(List<RelatedCourseItem> relatedCourses) { this.relatedCourses = relatedCourses; return this; }
        public Builder ambiguousOptions(List<AmbiguityOption> ambiguousOptions) { this.ambiguousOptions = ambiguousOptions; return this; }
        public Builder dataPresence(Map<String, String> dataPresence) { this.dataPresence = dataPresence; return this; }
        public Builder sourceRecordIdentifiers(Map<String, List<String>> identifiers) { this.sourceRecordIdentifiers = identifiers; return this; }
        public Builder limitations(List<String> limitations) { this.limitations = limitations; return this; }
        public Builder unresolvedAmbiguities(List<String> ambiguities) { this.unresolvedAmbiguities = ambiguities; return this; }

        public CurriculumMappingResult build() {
            return new CurriculumMappingResult(
                    subject,
                    resolutionStatus,
                    summary,
                    institution,
                    department,
                    programme,
                    curriculumVersion,
                    canonicalCourse,
                    offering,
                    prerequisites,
                    courseOutcomes,
                    programmeOutcomes,
                    coPoMappings,
                    historicalSnapshotUsed,
                    snapshotSource,
                    relatedCourses,
                    ambiguousOptions,
                    dataPresence,
                    sourceRecordIdentifiers,
                    limitations,
                    unresolvedAmbiguities
            );
        }
    }
}

