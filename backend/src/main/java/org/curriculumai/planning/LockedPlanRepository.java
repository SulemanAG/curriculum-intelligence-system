package org.curriculumai.planning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LockedPlanRepository extends JpaRepository<LockedPlan, String> {
    Optional<LockedPlan> findByRunId(String runId);
}

