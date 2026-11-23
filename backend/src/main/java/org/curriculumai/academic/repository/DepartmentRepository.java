package org.curriculumai.academic.repository;

import org.curriculumai.academic.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {
    List<Department> findByInstitutionId(String institutionId);
    Optional<Department> findByInstitutionIdAndCode(String institutionId, String code);
    boolean existsByInstitutionIdAndCode(String institutionId, String code);
}

