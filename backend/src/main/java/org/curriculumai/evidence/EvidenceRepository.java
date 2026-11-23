package org.curriculumai.evidence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenceRepository extends JpaRepository<EvidenceRecord, String> {
    List<EvidenceRecord> findByProposalId(String proposalId);
    List<EvidenceRecord> findByRunId(String runId);
}

