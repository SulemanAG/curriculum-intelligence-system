package org.curriculumai.academic.repository;

import org.curriculumai.academic.domain.CourseOutcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseOutcomeRepository extends JpaRepository<CourseOutcome, String> {
    List<CourseOutcome> findByCourseIdOrderByCodeAsc(String courseId);
    Optional<CourseOutcome> findByCourseIdAndCode(String courseId, String code);
    boolean existsByCourseIdAndCode(String courseId, String code);
}

