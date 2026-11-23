package org.curriculumai.academic.repository;

import org.curriculumai.academic.domain.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, String> {
    List<Semester> findByCurriculumVersionIdOrderBySemesterNumberAsc(String curriculumVersionId);
    Optional<Semester> findByCurriculumVersionIdAndSemesterNumber(String curriculumVersionId, int semesterNumber);
}

