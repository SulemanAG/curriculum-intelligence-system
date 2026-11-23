package org.curriculumai.orchestration.repository;

import org.curriculumai.orchestration.domain.WorkflowEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowEventRepository extends JpaRepository<WorkflowEvent, String> {

    List<WorkflowEvent> findByRunIdOrderByCreatedAtAsc(String runId);

    Page<WorkflowEvent> findByRunIdOrderByCreatedAtDesc(String runId, Pageable pageable);

    List<WorkflowEvent> findByTaskIdOrderByCreatedAtAsc(String taskId);
}

