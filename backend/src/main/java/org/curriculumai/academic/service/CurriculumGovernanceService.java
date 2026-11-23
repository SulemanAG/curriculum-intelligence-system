package org.curriculumai.academic.service;

import org.curriculumai.academic.domain.*;
import org.curriculumai.academic.dto.*;
import org.curriculumai.academic.repository.*;
import org.curriculumai.common.exceptions.ResourceNotFoundException;
import org.curriculumai.users.User;
import org.curriculumai.users.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class CurriculumGovernanceService {

    private static final Set<String> AUTHORIZED_APPROVER_ROLES = Set.of(
            "ADMIN", "DEAN", "HOD", "APPROVER", "ACADEMIC_LEAD", "BOS_REVIEWER"
    );

    private final CurriculumVersionRepository versionRepository;
    private final ProgrammeRepository programmeRepository;
    private final SemesterRepository semesterRepository;
    private final CourseRepository courseRepository;
    private final CurriculumCourseRepository curriculumCourseRepository;
    private final UserRepository userRepository;
    private final CurriculumOutcomeSnapshotRepository snapshotRepository;
    private final ProgrammeOutcomeRepository programmeOutcomeRepository;
    private final CourseOutcomeRepository courseOutcomeRepository;
    private final CoPoMappingRepository coPoMappingRepository;

    public CurriculumGovernanceService(
            CurriculumVersionRepository versionRepository,
            ProgrammeRepository programmeRepository,
            SemesterRepository semesterRepository,
            CourseRepository courseRepository,
            CurriculumCourseRepository curriculumCourseRepository,
            UserRepository userRepository,
            CurriculumOutcomeSnapshotRepository snapshotRepository,
            ProgrammeOutcomeRepository programmeOutcomeRepository,
            CourseOutcomeRepository courseOutcomeRepository,
            CoPoMappingRepository coPoMappingRepository
    ) {
        this.versionRepository = versionRepository;
        this.programmeRepository = programmeRepository;
        this.semesterRepository = semesterRepository;
        this.courseRepository = courseRepository;
        this.curriculumCourseRepository = curriculumCourseRepository;
        this.userRepository = userRepository;
        this.snapshotRepository = snapshotRepository;
        this.programmeOutcomeRepository = programmeOutcomeRepository;
        this.courseOutcomeRepository = courseOutcomeRepository;
        this.coPoMappingRepository = coPoMappingRepository;
    }

    // --- Curriculum Version Operations ---
    public CurriculumVersionDto.CurriculumVersionResponse createCurriculumVersion(
            String userId,
            CurriculumVersionDto.CreateCurriculumVersionRequest req
    ) {
        Programme prog = programmeRepository.findById(req.programmeId())
                .orElseThrow(() -> new ResourceNotFoundException("Programme not found: " + req.programmeId()));

        if (versionRepository.existsByProgrammeIdAndVersionCode(prog.getId(), req.versionCode().trim())) {
            throw new IllegalArgumentException("Version code '" + req.versionCode() + "' already exists for programme " + prog.getCode());
        }

        User user = userRepository.findById(userId).orElse(null);

        CurriculumVersion cv = new CurriculumVersion(
                "currv-" + UUID.randomUUID(),
                prog,
                req.versionCode().trim(),
                req.effectiveYear()
        );
        cv.setTotalCredits(req.totalCredits());
        cv.setSourceProvenance(req.sourceProvenance() != null ? req.sourceProvenance() : "MANUAL_FACULTY_INPUT");
        cv.setCreatedBy(user);
        cv.setUpdatedBy(user);

        return CurriculumVersionDto.CurriculumVersionResponse.fromEntity(versionRepository.save(cv));
    }

    @Transactional(readOnly = true)
    public CurriculumVersionDto.CurriculumVersionResponse getCurriculumVersion(String id) {
        return versionRepository.findById(id)
                .map(CurriculumVersionDto.CurriculumVersionResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum version not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<CurriculumVersionDto.CurriculumVersionResponse> listVersionsByProgramme(String programmeId) {
        return versionRepository.findByProgrammeId(programmeId).stream()
                .map(CurriculumVersionDto.CurriculumVersionResponse::fromEntity)
                .toList();
    }

    // --- Lifecycle State Transitions ---
    public CurriculumVersionDto.CurriculumVersionResponse transitionStatus(
            String userId,
            String versionId,
            CurriculumVersionDto.TransitionStatusRequest req
    ) {
        CurriculumVersion cv = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum version not found: " + versionId));

        CurriculumLifecycleStatus targetStatus;
        try {
            targetStatus = CurriculumLifecycleStatus.valueOf(req.targetStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid target lifecycle status: " + req.targetStatus() +
                    ". Permitted statuses: DRAFT, UNDER_REVIEW, APPROVED, ARCHIVED.");
        }

        if (!cv.getLifecycleStatus().canTransitionTo(targetStatus)) {
            throw new IllegalStateException("Illegal lifecycle transition: cannot transition curriculum version from " +
                    cv.getLifecycleStatus() + " to " + targetStatus + ".");
        }

        User user = userRepository.findById(userId).orElse(null);
        cv.setUpdatedBy(user);
        cv.setUpdatedAt(Instant.now());

        if (targetStatus == CurriculumLifecycleStatus.APPROVED) {
            if (cv.getLifecycleStatus() == CurriculumLifecycleStatus.APPROVED) {
                throw new IllegalStateException("Curriculum version '" + cv.getVersionCode() + "' is already in APPROVED status. Repeated approval is prohibited to preserve snapshot immutability.");
            }
            if (snapshotRepository.existsByCurriculumVersionId(cv.getId())) {
                throw new IllegalStateException("Outcome snapshots already exist for curriculum version " + cv.getVersionCode() + ". Snapshots are immutable and cannot be re-generated.");
            }
            if (user == null || !AUTHORIZED_APPROVER_ROLES.contains(user.getRole().toUpperCase())) {
                throw new AccessDeniedException("User role '" + (user != null ? user.getRole() : "ANONYMOUS") +
                        "' is not authorized to approve curriculum versions. Required roles: " + AUTHORIZED_APPROVER_ROLES);
            }

            cv.setApprovedBy(user);
            cv.setApprovedAt(Instant.now());
            String meta = String.format("{\"resolution\":\"%s\",\"notes\":\"%s\",\"approvedBy\":\"%s\"}",
                    req.resolutionNumber() != null ? req.resolutionNumber() : "UNSPECIFIED",
                    req.approvalNotes() != null ? req.approvalNotes() : "",
                    user.getUsername());
            cv.setApprovalMetadataJson(meta);

            // Snapshot immutable Programme Outcomes, Course Outcomes, and CO-PO Mappings
            createOutcomeSnapshots(cv);
        }

        cv.setLifecycleStatus(targetStatus);
        return CurriculumVersionDto.CurriculumVersionResponse.fromEntity(versionRepository.save(cv));
    }

    private void createOutcomeSnapshots(CurriculumVersion cv) {
        List<CurriculumOutcomeSnapshot> snapshots = new ArrayList<>();

        // 1. Snapshot Programme Outcomes
        List<ProgrammeOutcome> pos = programmeOutcomeRepository.findByProgrammeIdOrderByCodeAsc(cv.getProgramme().getId());
        for (ProgrammeOutcome po : pos) {
            snapshots.add(CurriculumOutcomeSnapshot.ofProgrammeOutcome("snap-po-" + UUID.randomUUID(), cv, po));
        }

        // 2. Snapshot Course Outcomes & CO-PO Mappings for courses in this curriculum version
        List<CurriculumCourse> curriculumCourses = curriculumCourseRepository.findByCurriculumVersionId(cv.getId());
        for (CurriculumCourse cc : curriculumCourses) {
            Course course = cc.getCourse();
            List<CourseOutcome> cos = courseOutcomeRepository.findByCourseIdOrderByCodeAsc(course.getId());
            for (CourseOutcome co : cos) {
                snapshots.add(CurriculumOutcomeSnapshot.ofCourseOutcome("snap-co-" + UUID.randomUUID(), cv, course, co));

                List<CoPoMapping> mappings = coPoMappingRepository.findByCourseOutcomeId(co.getId());
                for (CoPoMapping map : mappings) {
                    snapshots.add(CurriculumOutcomeSnapshot.ofCoPoMapping("snap-copo-" + UUID.randomUUID(), cv, course, map));
                }
            }
        }

        if (!snapshots.isEmpty()) {
            snapshotRepository.saveAll(snapshots);
        }
    }

    @Transactional(readOnly = true)
    public List<OutcomeDto.CurriculumOutcomeSnapshotResponse> getCurriculumOutcomeSnapshots(String curriculumVersionId) {
        return snapshotRepository.findByCurriculumVersionId(curriculumVersionId).stream()
                .map(OutcomeDto.CurriculumOutcomeSnapshotResponse::fromEntity)
                .toList();
    }

    // --- Immutability & Cloning ---
    public CurriculumVersionDto.CurriculumVersionResponse cloneAsNewDraft(
            String userId,
            String sourceVersionId,
            CurriculumVersionDto.CloneDraftRequest req
    ) {
        CurriculumVersion source = versionRepository.findById(sourceVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Source curriculum version not found: " + sourceVersionId));

        if (versionRepository.existsByProgrammeIdAndVersionCode(source.getProgramme().getId(), req.newVersionCode().trim())) {
            throw new IllegalArgumentException("Version code '" + req.newVersionCode() + "' already exists for programme " + source.getProgramme().getCode());
        }

        User user = userRepository.findById(userId).orElse(null);

        // 1. Create cloned version in DRAFT
        CurriculumVersion draft = new CurriculumVersion(
                "currv-" + UUID.randomUUID(),
                source.getProgramme(),
                req.newVersionCode().trim(),
                req.newEffectiveYear()
        );
        draft.setTotalCredits(source.getTotalCredits());
        draft.setSourceProvenance("CLONED_FROM_" + source.getVersionCode());
        draft.setCreatedBy(user);
        draft.setUpdatedBy(user);
        draft.setLifecycleStatus(CurriculumLifecycleStatus.DRAFT);
        CurriculumVersion savedDraft = versionRepository.save(draft);

        // 2. Clone semesters
        List<Semester> sourceSemesters = semesterRepository.findByCurriculumVersionIdOrderBySemesterNumberAsc(source.getId());
        for (Semester srcSem : sourceSemesters) {
            Semester newSem = new Semester(
                    "sem-" + UUID.randomUUID(),
                    savedDraft,
                    srcSem.getSemesterNumber(),
                    srcSem.getLabel(),
                    srcSem.getCreditsTarget()
            );
            Semester savedSem = semesterRepository.save(newSem);

            // 3. Clone curriculum courses
            List<CurriculumCourse> srcCourses = curriculumCourseRepository.findBySemesterId(srcSem.getId());
            for (CurriculumCourse srcCc : srcCourses) {
                CurriculumCourse newCc = new CurriculumCourse(
                        "currcrs-" + UUID.randomUUID(),
                        savedDraft,
                        savedSem,
                        srcCc.getCourse(),
                        srcCc.getCredits(),
                        srcCc.getLectureHours(),
                        srcCc.getTutorialHours(),
                        srcCc.getPracticalHours(),
                        srcCc.isElective(),
                        srcCc.getElectiveGroup()
                );
                curriculumCourseRepository.save(newCc);
            }
        }

        return CurriculumVersionDto.CurriculumVersionResponse.fromEntity(savedDraft);
    }

    // --- Semester Operations (Immutable Gate Protected) ---
    public SemesterDto.SemesterResponse createSemester(SemesterDto.CreateSemesterRequest req) {
        CurriculumVersion cv = versionRepository.findById(req.curriculumVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum version not found: " + req.curriculumVersionId()));

        assertMutable(cv);

        if (semesterRepository.findByCurriculumVersionIdAndSemesterNumber(cv.getId(), req.semesterNumber()).isPresent()) {
            throw new IllegalArgumentException("Semester number " + req.semesterNumber() + " already exists in curriculum version " + cv.getVersionCode());
        }

        Semester sem = new Semester(
                "sem-" + UUID.randomUUID(),
                cv,
                req.semesterNumber(),
                req.label().trim(),
                req.creditsTarget()
        );

        return SemesterDto.SemesterResponse.fromEntity(semesterRepository.save(sem));
    }

    @Transactional(readOnly = true)
    public List<SemesterDto.SemesterResponse> listSemesters(String curriculumVersionId) {
        return semesterRepository.findByCurriculumVersionIdOrderBySemesterNumberAsc(curriculumVersionId).stream()
                .map(SemesterDto.SemesterResponse::fromEntity)
                .toList();
    }

    // --- Curriculum Course Operations (Immutable Gate Protected) ---
    public CurriculumCourseDto.CurriculumCourseResponse addCourseToSemester(CurriculumCourseDto.AddCourseToSemesterRequest req) {
        CurriculumVersion cv = versionRepository.findById(req.curriculumVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum version not found: " + req.curriculumVersionId()));

        assertMutable(cv);

        Semester sem = semesterRepository.findById(req.semesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found: " + req.semesterId()));

        if (!sem.getCurriculumVersion().getId().equals(cv.getId())) {
            throw new IllegalArgumentException("Semester " + sem.getId() + " does not belong to curriculum version " + cv.getId());
        }

        Course course = courseRepository.findById(req.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + req.courseId()));

        if (curriculumCourseRepository.existsByCurriculumVersionIdAndCourseId(cv.getId(), course.getId())) {
            throw new IllegalArgumentException("Course " + course.getCode() + " is already included in curriculum version " + cv.getVersionCode());
        }

        CurriculumCourse cc = new CurriculumCourse(
                "currcrs-" + UUID.randomUUID(),
                cv,
                sem,
                course,
                req.credits(),
                req.lectureHours() != null ? req.lectureHours() : course.getLectureHours(),
                req.tutorialHours() != null ? req.tutorialHours() : course.getTutorialHours(),
                req.practicalHours() != null ? req.practicalHours() : course.getPracticalHours(),
                req.isElective() != null ? req.isElective() : false,
                req.electiveGroup()
        );

        // Update total credits in curriculum version
        int creditDelta = req.credits() != null ? req.credits() : (course.getDefaultCredits() != null ? course.getDefaultCredits() : 0);
        int currentTotal = cv.getTotalCredits() != null ? cv.getTotalCredits() : 0;
        cv.setTotalCredits(currentTotal + creditDelta);
        versionRepository.save(cv);

        return CurriculumCourseDto.CurriculumCourseResponse.fromEntity(curriculumCourseRepository.save(cc));
    }

    public void removeCourseFromSemester(String curriculumVersionId, String curriculumCourseId) {
        CurriculumVersion cv = versionRepository.findById(curriculumVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum version not found: " + curriculumVersionId));

        assertMutable(cv);

        CurriculumCourse cc = curriculumCourseRepository.findById(curriculumCourseId)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum course not found: " + curriculumCourseId));

        if (!cc.getCurriculumVersion().getId().equals(cv.getId())) {
            throw new IllegalArgumentException("Course offering does not belong to curriculum version: " + cv.getId());
        }

        int creditDelta = cc.getCredits() != null ? cc.getCredits() : 0;
        int currentTotal = cv.getTotalCredits() != null ? cv.getTotalCredits() : 0;
        cv.setTotalCredits(Math.max(0, currentTotal - creditDelta));
        versionRepository.save(cv);

        curriculumCourseRepository.delete(cc);
    }

    @Transactional(readOnly = true)
    public List<CurriculumCourseDto.CurriculumCourseResponse> listCoursesByCurriculumVersion(String curriculumVersionId) {
        return curriculumCourseRepository.findByCurriculumVersionId(curriculumVersionId).stream()
                .map(CurriculumCourseDto.CurriculumCourseResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CurriculumCourseDto.CurriculumCourseResponse> listCoursesBySemester(String semesterId) {
        return curriculumCourseRepository.findBySemesterId(semesterId).stream()
                .map(CurriculumCourseDto.CurriculumCourseResponse::fromEntity)
                .toList();
    }

    private void assertMutable(CurriculumVersion cv) {
        if (cv.isImmutable()) {
            throw new IllegalStateException("Curriculum version '" + cv.getVersionCode() + "' is " +
                    cv.getLifecycleStatus() + " and immutable. Approved regulations cannot be modified; " +
                    "use cloneAsNewDraft to create a new revision.");
        }
    }
}

