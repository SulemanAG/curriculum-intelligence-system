package org.curriculumai.academic.service;

import org.curriculumai.academic.domain.*;
import org.curriculumai.academic.dto.*;
import org.curriculumai.academic.repository.*;
import org.curriculumai.common.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OutcomeMappingService {

    private final ProgrammeRepository programmeRepository;
    private final ProgrammeOutcomeRepository programmeOutcomeRepository;
    private final CourseRepository courseRepository;
    private final CourseOutcomeRepository courseOutcomeRepository;
    private final CoPoMappingRepository coPoMappingRepository;

    public OutcomeMappingService(
            ProgrammeRepository programmeRepository,
            ProgrammeOutcomeRepository programmeOutcomeRepository,
            CourseRepository courseRepository,
            CourseOutcomeRepository courseOutcomeRepository,
            CoPoMappingRepository coPoMappingRepository
    ) {
        this.programmeRepository = programmeRepository;
        this.programmeOutcomeRepository = programmeOutcomeRepository;
        this.courseRepository = courseRepository;
        this.courseOutcomeRepository = courseOutcomeRepository;
        this.coPoMappingRepository = coPoMappingRepository;
    }

    // --- Programme Outcome Operations ---
    public OutcomeDto.ProgrammeOutcomeResponse createProgrammeOutcome(OutcomeDto.CreateProgrammeOutcomeRequest req) {
        Programme prog = programmeRepository.findById(req.programmeId())
                .orElseThrow(() -> new ResourceNotFoundException("Programme not found: " + req.programmeId()));

        if (programmeOutcomeRepository.existsByProgrammeIdAndCode(prog.getId(), req.code().trim())) {
            throw new IllegalArgumentException("Programme outcome with code '" + req.code() + "' already exists for programme " + prog.getCode());
        }

        ProgrammeOutcome po = new ProgrammeOutcome(
                "po-" + UUID.randomUUID(),
                prog,
                req.code().trim().toUpperCase(),
                req.title().trim(),
                req.description().trim(),
                req.outcomeType() != null ? req.outcomeType().trim().toUpperCase() : "PO"
        );

        return OutcomeDto.ProgrammeOutcomeResponse.fromEntity(programmeOutcomeRepository.save(po));
    }

    @Transactional(readOnly = true)
    public List<OutcomeDto.ProgrammeOutcomeResponse> listProgrammeOutcomes(String programmeId) {
        return programmeOutcomeRepository.findByProgrammeIdOrderByCodeAsc(programmeId).stream()
                .map(OutcomeDto.ProgrammeOutcomeResponse::fromEntity)
                .toList();
    }

    // --- Course Outcome Operations ---
    public OutcomeDto.CourseOutcomeResponse createCourseOutcome(OutcomeDto.CreateCourseOutcomeRequest req) {
        Course course = courseRepository.findById(req.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + req.courseId()));

        if (courseOutcomeRepository.existsByCourseIdAndCode(course.getId(), req.code().trim())) {
            throw new IllegalArgumentException("Course outcome with code '" + req.code() + "' already exists for course " + course.getCode());
        }

        CourseOutcome co = new CourseOutcome(
                "co-" + UUID.randomUUID(),
                course,
                req.code().trim().toUpperCase(),
                req.description().trim(),
                req.bloomLevel() != null ? req.bloomLevel().trim() : null
        );

        return OutcomeDto.CourseOutcomeResponse.fromEntity(courseOutcomeRepository.save(co));
    }

    @Transactional(readOnly = true)
    public List<OutcomeDto.CourseOutcomeResponse> listCourseOutcomes(String courseId) {
        return courseOutcomeRepository.findByCourseIdOrderByCodeAsc(courseId).stream()
                .map(OutcomeDto.CourseOutcomeResponse::fromEntity)
                .toList();
    }

    // --- CO-PO Mapping Operations ---
    public OutcomeDto.CoPoMappingResponse createCoPoMapping(OutcomeDto.CreateCoPoMappingRequest req) {
        CourseOutcome co = courseOutcomeRepository.findById(req.courseOutcomeId())
                .orElseThrow(() -> new ResourceNotFoundException("Course outcome not found: " + req.courseOutcomeId()));

        ProgrammeOutcome po = programmeOutcomeRepository.findById(req.programmeOutcomeId())
                .orElseThrow(() -> new ResourceNotFoundException("Programme outcome not found: " + req.programmeOutcomeId()));

        if (req.mappingLevel() < 1 || req.mappingLevel() > 3) {
            throw new IllegalArgumentException("Mapping level must be 1 (Low), 2 (Medium), or 3 (High). Provided: " + req.mappingLevel());
        }

        if (coPoMappingRepository.findByCourseOutcomeIdAndProgrammeOutcomeId(co.getId(), po.getId()).isPresent()) {
            throw new IllegalArgumentException("CO-PO mapping already exists between " + co.getCode() + " and " + po.getCode());
        }

        CoPoMapping mapping = new CoPoMapping(
                "copo-" + UUID.randomUUID(),
                co,
                po,
                req.mappingLevel(),
                req.justification()
        );

        return OutcomeDto.CoPoMappingResponse.fromEntity(coPoMappingRepository.save(mapping));
    }

    @Transactional(readOnly = true)
    public List<OutcomeDto.CoPoMappingResponse> listMappingsByCourseOutcome(String courseOutcomeId) {
        return coPoMappingRepository.findByCourseOutcomeId(courseOutcomeId).stream()
                .map(OutcomeDto.CoPoMappingResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OutcomeDto.CoPoMappingResponse> listMappingsByProgrammeOutcome(String programmeOutcomeId) {
        return coPoMappingRepository.findByProgrammeOutcomeId(programmeOutcomeId).stream()
                .map(OutcomeDto.CoPoMappingResponse::fromEntity)
                .toList();
    }

    public OutcomeDto.CourseOutcomeResponse updateCourseOutcome(String id, OutcomeDto.UpdateCourseOutcomeRequest req) {
        CourseOutcome co = courseOutcomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course outcome not found: " + id));
        if (req.description() != null && !req.description().isBlank()) {
            co.setDescription(req.description().trim());
        }
        if (req.bloomLevel() != null) {
            co.setBloomLevel(req.bloomLevel().trim());
        }
        co.setUpdatedAt(java.time.Instant.now());
        return OutcomeDto.CourseOutcomeResponse.fromEntity(courseOutcomeRepository.save(co));
    }

    public OutcomeDto.ProgrammeOutcomeResponse updateProgrammeOutcome(String id, OutcomeDto.UpdateProgrammeOutcomeRequest req) {
        ProgrammeOutcome po = programmeOutcomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programme outcome not found: " + id));
        if (req.title() != null && !req.title().isBlank()) {
            po.setTitle(req.title().trim());
        }
        if (req.description() != null && !req.description().isBlank()) {
            po.setDescription(req.description().trim());
        }
        if (req.outcomeType() != null && !req.outcomeType().isBlank()) {
            po.setOutcomeType(req.outcomeType().trim().toUpperCase());
        }
        po.setUpdatedAt(java.time.Instant.now());
        return OutcomeDto.ProgrammeOutcomeResponse.fromEntity(programmeOutcomeRepository.save(po));
    }
}
