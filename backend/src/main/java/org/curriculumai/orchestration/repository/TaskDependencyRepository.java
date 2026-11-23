package org.curriculumai.orchestration.repository;

import org.curriculumai.orchestration.domain.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, String> {

    List<TaskDependency> findByRunId(String runId);

    List<TaskDependency> findByTaskId(String taskId);

    List<TaskDependency> findByDependsOnTaskId(String dependsOnTaskId);
}

