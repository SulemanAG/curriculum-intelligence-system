package org.curriculumai.academic.repository;

import org.curriculumai.academic.domain.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    List<Course> findByDepartmentId(String departmentId);
    List<Course> findByTitleContainingIgnoreCase(String title);
    List<Course> findByDepartmentIdAndTitleContainingIgnoreCase(String departmentId, String title);
    Page<Course> findByDepartmentId(String departmentId, Pageable pageable);
    Optional<Course> findByDepartmentIdAndCode(String departmentId, String code);
    Optional<Course> findByCode(String code);
    Optional<Course> findByCodeIgnoreCase(String code);
    boolean existsByDepartmentIdAndCode(String departmentId, String code);
}

