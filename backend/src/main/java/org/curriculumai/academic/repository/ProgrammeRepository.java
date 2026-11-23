package org.curriculumai.academic.repository;

import org.curriculumai.academic.domain.Programme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgrammeRepository extends JpaRepository<Programme, String> {
    List<Programme> findByDepartmentId(String departmentId);
    Optional<Programme> findByDepartmentIdAndCode(String departmentId, String code);
    boolean existsByDepartmentIdAndCode(String departmentId, String code);
}

