package org.curriculumai.evidence.repository;

import org.curriculumai.evidence.domain.ResearchSource;
import org.curriculumai.evidence.domain.SourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResearchSourceRepository extends JpaRepository<ResearchSource, String> {
    Optional<ResearchSource> findByCanonicalUrl(String canonicalUrl);
    boolean existsByCanonicalUrl(String canonicalUrl);
    Page<ResearchSource> findBySourceType(SourceType sourceType, Pageable pageable);
    Page<ResearchSource> findByPublisherContainingIgnoreCase(String publisher, Pageable pageable);
    Page<ResearchSource> findByInstitutionId(String institutionId, Pageable pageable);
    Page<ResearchSource> findByDepartmentId(String departmentId, Pageable pageable);
}

