package org.curriculumai.academic.repository;

import org.curriculumai.academic.domain.CoPoMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoPoMappingRepository extends JpaRepository<CoPoMapping, String> {
    List<CoPoMapping> findByCourseOutcomeId(String courseOutcomeId);
    List<CoPoMapping> findByProgrammeOutcomeId(String programmeOutcomeId);
    Optional<CoPoMapping> findByCourseOutcomeIdAndProgrammeOutcomeId(String courseOutcomeId, String programmeOutcomeId);
}

