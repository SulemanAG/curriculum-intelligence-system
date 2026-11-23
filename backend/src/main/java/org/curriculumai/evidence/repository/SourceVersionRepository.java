package org.curriculumai.evidence.repository;

import org.curriculumai.evidence.domain.SourceVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SourceVersionRepository extends JpaRepository<SourceVersion, String> {
    Optional<SourceVersion> findBySourceIdAndContentHash(String sourceId, String contentHash);
    List<SourceVersion> findBySourceIdOrderByRetrievedTimestampDesc(String sourceId);
    Page<SourceVersion> findBySourceId(String sourceId, Pageable pageable);
    long countBySourceId(String sourceId);
}

