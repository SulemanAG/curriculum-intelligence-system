package org.curriculumai.orchestration.repository;

import org.curriculumai.orchestration.domain.OrchestrationRun;
import org.curriculumai.orchestration.domain.RunStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrchestrationRunRepository extends JpaRepository<OrchestrationRun, String> {

    Page<OrchestrationRun> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<OrchestrationRun> findByUserId(String userId, Pageable pageable);

    List<OrchestrationRun> findByStatus(RunStatus status);

    List<OrchestrationRun> findByConversationId(String conversationId);

    Optional<OrchestrationRun> findByIdAndUserId(String id, String userId);
}
