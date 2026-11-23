package org.curriculumai.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<AuditEvent, String> {
    List<AuditEvent> findByRunIdOrderByCreatedAtDesc(String runId);
}

