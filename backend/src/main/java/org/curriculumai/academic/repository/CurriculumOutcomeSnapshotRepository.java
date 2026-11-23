package org.curriculumai.academic.repository;

import org.curriculumai.academic.domain.CurriculumOutcomeSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurriculumOutcomeSnapshotRepository extends JpaRepository<CurriculumOutcomeSnapshot, String> {
    List<CurriculumOutcomeSnapshot> findByCurriculumVersionId(String curriculumVersionId);
    List<CurriculumOutcomeSnapshot> findByCurriculumVersionIdAndCourseId(String curriculumVersionId, String courseId);
    List<CurriculumOutcomeSnapshot> findByCurriculumVersionIdAndSnapshotType(String curriculumVersionId, String snapshotType);
    boolean existsByCurriculumVersionId(String curriculumVersionId);
}

