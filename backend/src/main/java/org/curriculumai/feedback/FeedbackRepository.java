package org.curriculumai.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackRecord, String> {
    Optional<FeedbackRecord> findByRunId(String runId);
}

