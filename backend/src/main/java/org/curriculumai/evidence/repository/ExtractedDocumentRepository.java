package org.curriculumai.evidence.repository;

import org.curriculumai.evidence.domain.ExtractedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExtractedDocumentRepository extends JpaRepository<ExtractedDocument, String> {
    Optional<ExtractedDocument> findBySourceVersionId(String sourceVersionId);
}

