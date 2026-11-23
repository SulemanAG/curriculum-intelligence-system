package org.curriculumai.academic.repository;

import org.curriculumai.academic.domain.CoursePrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, String> {
    List<CoursePrerequisite> findByCourseId(String courseId);
    List<CoursePrerequisite> findByPrerequisiteCourseId(String prerequisiteCourseId);
    Optional<CoursePrerequisite> findByCourseIdAndPrerequisiteCourseId(String courseId, String prerequisiteCourseId);
    boolean existsByCourseIdAndPrerequisiteCourseId(String courseId, String prerequisiteCourseId);
}

