package org.curriculumai.academic.repository;

import org.curriculumai.academic.domain.CurriculumCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurriculumCourseRepository extends JpaRepository<CurriculumCourse, String> {
    List<CurriculumCourse> findByCurriculumVersionId(String curriculumVersionId);
    List<CurriculumCourse> findBySemesterId(String semesterId);
    List<CurriculumCourse> findByCourseId(String courseId);
    Optional<CurriculumCourse> findByCurriculumVersionIdAndCourseId(String curriculumVersionId, String courseId);
    Optional<CurriculumCourse> findByCurriculumVersionIdAndSemesterIdAndCourseId(String curriculumVersionId, String semesterId, String courseId);
    boolean existsByCurriculumVersionIdAndCourseId(String curriculumVersionId, String courseId);
}

