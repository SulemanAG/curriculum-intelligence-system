package org.curriculumai.orchestration.repository;

import org.curriculumai.orchestration.domain.AgentExecutionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentExecutionRecordRepository extends JpaRepository<AgentExecutionRecord, String> {

    List<AgentExecutionRecord> findByTaskIdOrderByAttemptNumberAsc(String taskId);

    List<AgentExecutionRecord> findByTaskId(String taskId);

    List<AgentExecutionRecord> findByRunIdOrderByCreatedAtAsc(String runId);
}
