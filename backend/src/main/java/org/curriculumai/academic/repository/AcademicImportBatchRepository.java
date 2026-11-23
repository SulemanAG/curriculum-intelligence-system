package org.curriculumai.academic.repository;

import org.curriculumai.academic.domain.AcademicImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademicImportBatchRepository extends JpaRepository<AcademicImportBatch, String> {
    List<AcademicImportBatch> findByUserIdOrderByCreatedAtDesc(String userId);
    List<AcademicImportBatch> findByStatus(String status);
}

