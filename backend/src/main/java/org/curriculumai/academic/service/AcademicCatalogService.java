package org.curriculumai.academic.service;

import org.curriculumai.academic.domain.*;
import org.curriculumai.academic.dto.*;
import org.curriculumai.academic.repository.*;
import org.curriculumai.common.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class AcademicCatalogService {

    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final ProgrammeRepository programmeRepository;
    private final CourseRepository courseRepository;
    private final CoursePrerequisiteRepository prerequisiteRepository;

    public AcademicCatalogService(
            InstitutionRepository institutionRepository,
            DepartmentRepository departmentRepository,
            ProgrammeRepository programmeRepository,
            CourseRepository courseRepository,
            CoursePrerequisiteRepository prerequisiteRepository
    ) {
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.programmeRepository = programmeRepository;
        this.courseRepository = courseRepository;
        this.prerequisiteRepository = prerequisiteRepository;
    }

    // --- Institution Operations ---
    public InstitutionDto.InstitutionResponse createInstitution(InstitutionDto.CreateInstitutionRequest req) {
        if (institutionRepository.existsByCode(req.code().trim())) {
            throw new IllegalArgumentException("Institution with code '" + req.code() + "' already exists.");
        }
        Institution inst = new Institution(
                "inst-" + UUID.randomUUID(),
                req.code().trim(),
                req.name().trim(),
                req.location(),
                req.country()
        );
        inst.setWebsite(req.website());
        inst.setMetadataJson(req.metadataJson());
        return InstitutionDto.InstitutionResponse.fromEntity(institutionRepository.save(inst));
    }

    @Transactional(readOnly = true)
    public InstitutionDto.InstitutionResponse getInstitution(String id) {
        return institutionRepository.findById(id)
                .map(InstitutionDto.InstitutionResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<InstitutionDto.InstitutionResponse> listInstitutions() {
        return institutionRepository.findAll().stream()
                .map(InstitutionDto.InstitutionResponse::fromEntity)
                .toList();
    }

    // --- Department Operations ---
    public DepartmentDto.DepartmentResponse createDepartment(DepartmentDto.CreateDepartmentRequest req) {
        Institution inst = institutionRepository.findById(req.institutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found: " + req.institutionId()));

        if (departmentRepository.existsByInstitutionIdAndCode(inst.getId(), req.code().trim())) {
            throw new IllegalArgumentException("Department code '" + req.code() + "' already exists in institution " + inst.getCode());
        }

        Department dept = new Department(
                "dept-" + UUID.randomUUID(),
                inst,
                req.code().trim(),
                req.name().trim()
        );
        return DepartmentDto.DepartmentResponse.fromEntity(departmentRepository.save(dept));
    }

    @Transactional(readOnly = true)
    public DepartmentDto.DepartmentResponse getDepartment(String id) {
        return departmentRepository.findById(id)
                .map(DepartmentDto.DepartmentResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto.DepartmentResponse> listDepartmentsByInstitution(String institutionId) {
        return departmentRepository.findByInstitutionId(institutionId).stream()
                .map(DepartmentDto.DepartmentResponse::fromEntity)
                .toList();
    }

    // --- Programme Operations ---
    public ProgrammeDto.ProgrammeResponse createProgramme(ProgrammeDto.CreateProgrammeRequest req) {
        Department dept = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + req.departmentId()));

        if (programmeRepository.existsByDepartmentIdAndCode(dept.getId(), req.code().trim())) {
            throw new IllegalArgumentException("Programme code '" + req.code() + "' already exists in department " + dept.getCode());
        }

        Programme prog = new Programme(
                "prog-" + UUID.randomUUID(),
                dept,
                req.code().trim(),
                req.name().trim(),
                req.degreeLevel(),
                req.durationYears(),
                req.totalCreditsRequired()
        );
        return ProgrammeDto.ProgrammeResponse.fromEntity(programmeRepository.save(prog));
    }

    @Transactional(readOnly = true)
    public ProgrammeDto.ProgrammeResponse getProgramme(String id) {
        return programmeRepository.findById(id)
                .map(ProgrammeDto.ProgrammeResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Programme not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProgrammeDto.ProgrammeResponse> listProgrammesByDepartment(String departmentId) {
        return programmeRepository.findByDepartmentId(departmentId).stream()
                .map(ProgrammeDto.ProgrammeResponse::fromEntity)
                .toList();
    }

    // --- Course Operations ---
    public CourseDto.CourseResponse createCourse(CourseDto.CreateCourseRequest req) {
        Department dept = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + req.departmentId()));

        if (courseRepository.existsByDepartmentIdAndCode(dept.getId(), req.code().trim())) {
            throw new IllegalArgumentException("Course with code '" + req.code() + "' already exists in department " + dept.getCode());
        }

        Course course = new Course(
                "crs-" + UUID.randomUUID(),
                dept,
                req.code().trim(),
                req.title().trim(),
                req.defaultCredits(),
                req.lectureHours(),
                req.tutorialHours(),
                req.practicalHours()
        );
        course.setDescription(req.description());
        if (req.courseType() != null && !req.courseType().isBlank()) {
            course.setCourseType(req.courseType().trim());
        } else {
            course.setCourseType(null);
        }

        return CourseDto.CourseResponse.fromEntity(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public CourseDto.CourseResponse getCourse(String id) {
        return courseRepository.findById(id)
                .map(CourseDto.CourseResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<CourseDto.CourseResponse> listCoursesByDepartment(String departmentId) {
        return courseRepository.findByDepartmentId(departmentId).stream()
                .map(CourseDto.CourseResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CourseDto.CourseResponse> listCoursesPaged(String departmentId, Pageable pageable) {
        if (departmentId != null && !departmentId.isBlank()) {
            return courseRepository.findByDepartmentId(departmentId, pageable)
                    .map(CourseDto.CourseResponse::fromEntity);
        }
        return courseRepository.findAll(pageable)
                .map(CourseDto.CourseResponse::fromEntity);
    }

    // --- Prerequisite Operations ---
    public PrerequisiteDto.CoursePrerequisiteResponse addPrerequisite(PrerequisiteDto.CreatePrerequisiteRequest req) {
        if (req.courseId().equals(req.prerequisiteCourseId())) {
            throw new IllegalArgumentException("A course cannot have itself as a prerequisite.");
        }

        Course course = courseRepository.findById(req.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + req.courseId()));
        Course prereqCourse = courseRepository.findById(req.prerequisiteCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Prerequisite course not found: " + req.prerequisiteCourseId()));

        if (prerequisiteRepository.existsByCourseIdAndPrerequisiteCourseId(course.getId(), prereqCourse.getId())) {
            throw new IllegalArgumentException("Prerequisite relationship already exists between " + course.getCode() + " and " + prereqCourse.getCode());
        }

        // Multi-node cycle detection: verify courseId is not reachable from prerequisiteCourseId
        detectPrerequisiteCycle(course.getId(), prereqCourse.getId());

        CoursePrerequisite cp = new CoursePrerequisite(
                "prq-" + UUID.randomUUID(),
                course,
                prereqCourse,
                req.requirementType(),
                req.minimumGrade()
        );

        return PrerequisiteDto.CoursePrerequisiteResponse.fromEntity(prerequisiteRepository.save(cp));
    }

    private void detectPrerequisiteCycle(String courseId, String proposedPrerequisiteId) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        queue.add(proposedPrerequisiteId);
        visited.add(proposedPrerequisiteId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(courseId)) {
                throw new IllegalArgumentException("Cyclic prerequisite detected: Adding this prerequisite would create a dependency cycle.");
            }

            List<CoursePrerequisite> prerequisites = prerequisiteRepository.findByCourseId(current);
            for (CoursePrerequisite cp : prerequisites) {
                String nextPrereqId = cp.getPrerequisiteCourse().getId();
                if (visited.add(nextPrereqId)) {
                    queue.add(nextPrereqId);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<PrerequisiteDto.CoursePrerequisiteResponse> listPrerequisitesForCourse(String courseId) {
        return prerequisiteRepository.findByCourseId(courseId).stream()
                .map(PrerequisiteDto.CoursePrerequisiteResponse::fromEntity)
                .toList();
    }
}

