package org.curriculumai.coordinator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalysisRunRepository extends JpaRepository<AnalysisRun, String> {

    @Query("SELECT r FROM AnalysisRun r LEFT JOIN FETCH r.stages WHERE r.id = :id")
    Optional<AnalysisRun> findByIdWithStages(@Param("id") String id);
}

