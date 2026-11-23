package org.curriculumai.coordinator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunStageRepository extends JpaRepository<RunStage, String> {
    List<RunStage> findByRunIdOrderByStageOrderAsc(String runId);
}

