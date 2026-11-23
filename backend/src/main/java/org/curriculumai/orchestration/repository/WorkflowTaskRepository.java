package org.curriculumai.orchestration.repository;

import org.curriculumai.orchestration.domain.TaskStatus;
import org.curriculumai.orchestration.domain.WorkflowTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, String> {

    List<WorkflowTask> findByRunIdOrderBySequenceOrderAsc(String runId);

    List<WorkflowTask> findByRunIdAndStatus(String runId, TaskStatus status);

    Optional<WorkflowTask> findByRunIdAndTaskKey(String runId, String taskKey);

    long countByRunIdAndStatus(String runId, TaskStatus status);
}

