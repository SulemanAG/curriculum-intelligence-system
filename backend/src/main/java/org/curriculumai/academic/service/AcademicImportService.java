package org.curriculumai.academic.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.academic.domain.*;
import org.curriculumai.academic.dto.*;
import org.curriculumai.academic.repository.*;
import org.curriculumai.common.exceptions.ResourceNotFoundException;
import org.curriculumai.users.User;
import org.curriculumai.users.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class AcademicImportService {

    private final AcademicImportBatchRepository batchRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AcademicImportService(
            AcademicImportBatchRepository batchRepository,
            InstitutionRepository institutionRepository,
            DepartmentRepository departmentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.batchRepository = batchRepository;
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public ImportDto.ImportPreviewResponse createImportPreview(String userId, ImportDto.AcademicImportRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Institution institution = institutionRepository.findByCode(req.institutionCode().trim())
                .orElse(null);

        List<String> errors = new ArrayList<>();
        if (institution == null) {
            errors.add("Institution code '" + req.institutionCode() + "' does not exist. Create institution before importing courses.");
        }

        List<ImportDto.CourseImportItem> validItems = new ArrayList<>();
        Set<String> seenCodesInBatch = new HashSet<>();

        if (req.courses() == null || req.courses().isEmpty()) {
            errors.add("Import payload contains zero course records.");
        } else {
            for (int idx = 0; idx < req.courses().size(); idx++) {
                ImportDto.CourseImportItem item = req.courses().get(idx);
                int recordIndex = idx + 1;

                if (item.courseCode() == null || item.courseCode().isBlank()) {
                    errors.add("Record #" + recordIndex + ": Missing mandatory course code.");
                    continue;
                }
                if (item.title() == null || item.title().isBlank()) {
                    errors.add("Record #" + recordIndex + " (" + item.courseCode() + "): Missing mandatory course title.");
                    continue;
                }
                if (item.departmentCode() == null || item.departmentCode().isBlank()) {
                    errors.add("Record #" + recordIndex + " (" + item.courseCode() + "): Missing mandatory department code.");
                    continue;
                }

                String batchKey = item.departmentCode().trim() + ":" + item.courseCode().trim();
                if (seenCodesInBatch.contains(batchKey)) {
                    errors.add("Record #" + recordIndex + ": Duplicate course code '" + item.courseCode() + "' detected within this import batch.");
                    continue;
                }
                seenCodesInBatch.add(batchKey);

                if (institution != null) {
                    Optional<Department> dept = departmentRepository.findByInstitutionIdAndCode(institution.getId(), item.departmentCode().trim());
                    if (dept.isEmpty()) {
                        errors.add("Record #" + recordIndex + ": Department '" + item.departmentCode() + "' does not exist in institution " + institution.getCode());
                        continue;
                    }

                    if (courseRepository.existsByDepartmentIdAndCode(dept.get().getId(), item.courseCode().trim())) {
                        errors.add("Record #" + recordIndex + ": Course '" + item.courseCode() + "' already exists in department " + item.departmentCode() + ".");
                        continue;
                    }
                }

                validItems.add(item);
            }
        }

        String batchId = "batch-" + UUID.randomUUID();
        AcademicImportBatch batch = new AcademicImportBatch(batchId, user, institution, req.format().trim().toUpperCase());
        batch.setTotalRecords(req.courses() != null ? req.courses().size() : 0);
        batch.setValidRecords(validItems.size());
        batch.setErrorRecords(errors.size());

        try {
            batch.setPreviewDataJson(objectMapper.writeValueAsString(validItems));
            batch.setValidationReportJson(objectMapper.writeValueAsString(errors));
        } catch (Exception e) {
            batch.setPreviewDataJson("[]");
            batch.setValidationReportJson("[\"Serialization error\"]");
        }

        batchRepository.save(batch);

        return new ImportDto.ImportPreviewResponse(
                batchId,
                batch.getStatus(),
                batch.getTotalRecords(),
                batch.getValidRecords(),
                batch.getErrorRecords(),
                errors,
                validItems
        );
    }

    public ImportDto.CommitImportResponse commitImport(String userId, String batchId) {
        AcademicImportBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Import batch not found: " + batchId));

        if (!"PREVIEW".equalsIgnoreCase(batch.getStatus())) {
            throw new IllegalStateException("Batch " + batchId + " is in status '" + batch.getStatus() + "' and cannot be committed.");
        }

        if (batch.getValidRecords() == 0) {
            throw new IllegalStateException("Batch " + batchId + " has zero valid records to commit.");
        }

        List<ImportDto.CourseImportItem> items;
        try {
            items = objectMapper.readValue(batch.getPreviewDataJson(), new TypeReference<List<ImportDto.CourseImportItem>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize preview data for batch: " + batchId);
        }

        Institution institution = batch.getInstitution();
        if (institution == null) {
            throw new IllegalStateException("Batch has no associated institution.");
        }

        int committedCount = 0;
        for (ImportDto.CourseImportItem item : items) {
            Department dept = departmentRepository.findByInstitutionIdAndCode(institution.getId(), item.departmentCode().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + item.departmentCode()));

            if (courseRepository.existsByDepartmentIdAndCode(dept.getId(), item.courseCode().trim())) {
                continue; // Skip if created in between
            }

            Course course = new Course(
                    "crs-" + UUID.randomUUID(),
                    dept,
                    item.courseCode().trim(),
                    item.title().trim(),
                    item.credits(),
                    item.lectureHours(),
                    item.tutorialHours(),
                    item.practicalHours()
            );
            course.setDescription(item.description());
            if (item.courseType() != null && !item.courseType().isBlank()) {
                course.setCourseType(item.courseType().trim());
            } else {
                course.setCourseType(null);
            }

            courseRepository.save(course);
            committedCount++;
        }

        batch.setStatus("COMMITTED");
        batch.setCommittedAt(Instant.now());
        batchRepository.save(batch);

        return new ImportDto.CommitImportResponse(
                batchId,
                "COMMITTED",
                committedCount,
                batch.getCommittedAt(),
                "Successfully committed " + committedCount + " courses to the academic catalog with provenance preserved."
        );
    }

    @Transactional(readOnly = true)
    public ImportDto.ImportPreviewResponse getBatchPreview(String batchId) {
        AcademicImportBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Import batch not found: " + batchId));

        List<ImportDto.CourseImportItem> validItems = Collections.emptyList();
        List<String> errors = Collections.emptyList();

        try {
            if (batch.getPreviewDataJson() != null) {
                validItems = objectMapper.readValue(batch.getPreviewDataJson(), new TypeReference<List<ImportDto.CourseImportItem>>() {});
            }
            if (batch.getValidationReportJson() != null) {
                errors = objectMapper.readValue(batch.getValidationReportJson(), new TypeReference<List<String>>() {});
            }
        } catch (Exception ignored) {}

        return new ImportDto.ImportPreviewResponse(
                batch.getId(),
                batch.getStatus(),
                batch.getTotalRecords(),
                batch.getValidRecords(),
                batch.getErrorRecords(),
                errors,
                validItems
        );
    }
}

