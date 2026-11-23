package org.curriculumai.evidence.repository;

import org.curriculumai.evidence.domain.EmbeddingStatus;
import org.curriculumai.evidence.domain.EvidenceChunkEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EvidenceChunkEmbeddingRepository extends JpaRepository<EvidenceChunkEmbedding, String> {

    Optional<EvidenceChunkEmbedding> findByChunkIdAndModelName(String chunkId, String modelName);

    List<EvidenceChunkEmbedding> findByChunkId(String chunkId);

    List<EvidenceChunkEmbedding> findByStatus(EmbeddingStatus status);

    List<EvidenceChunkEmbedding> findByChunkIdIn(Collection<String> chunkIds);

    List<EvidenceChunkEmbedding> findByChunkIdInAndModelName(Collection<String> chunkIds, String modelName);

    long countByChunkIdInAndStatus(Collection<String> chunkIds, EmbeddingStatus status);
}

