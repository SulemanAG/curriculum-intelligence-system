package org.curriculumai.evidence.repository;

import org.curriculumai.evidence.domain.EvidenceChunk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvidenceChunkRepository extends JpaRepository<EvidenceChunk, String> {
    List<EvidenceChunk> findByExtractedDocumentIdOrderBySequenceNumberAsc(String extractedDocumentId);
    Page<EvidenceChunk> findByExtractedDocumentId(String extractedDocumentId, Pageable pageable);
    Optional<EvidenceChunk> findByExtractedDocumentIdAndSequenceNumber(String extractedDocumentId, Integer sequenceNumber);

    @Query("SELECT c FROM EvidenceChunk c WHERE LOWER(c.textContent) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<EvidenceChunk> searchByTextContent(@Param("query") String query, Pageable pageable);
}

