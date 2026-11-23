package org.curriculumai.evidence.service;

import org.curriculumai.evidence.domain.*;
import org.curriculumai.evidence.dto.EvidenceChunkDto;
import org.curriculumai.evidence.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EvidenceSearchService {

    private final EvidenceChunkRepository chunkRepository;
    private final ExtractedDocumentRepository documentRepository;
    private final SourceVersionRepository versionRepository;
    private final ResearchSourceRepository sourceRepository;
    private final EvidenceIngestionService ingestionService;

    public EvidenceSearchService(EvidenceChunkRepository chunkRepository,
                                 ExtractedDocumentRepository documentRepository,
                                 SourceVersionRepository versionRepository,
                                 ResearchSourceRepository sourceRepository,
                                 EvidenceIngestionService ingestionService) {
        this.chunkRepository = chunkRepository;
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
        this.sourceRepository = sourceRepository;
        this.ingestionService = ingestionService;
    }

    /**
     * Multi-criteria search for evidence chunks matching query and filters.
     */
    public Page<EvidenceChunkDto> searchEvidence(String query, SourceType sourceType, String publisher,
                                                 String institutionId, String departmentId,
                                                 Pageable pageable) {
        Page<EvidenceChunk> candidateChunks;
        if (query != null && !query.isBlank()) {
            candidateChunks = chunkRepository.searchByTextContent(query.trim(), pageable);
        } else {
            candidateChunks = chunkRepository.findAll(pageable);
        }

        if (sourceType == null && publisher == null && institutionId == null && departmentId == null) {
            return candidateChunks.map(ingestionService::mapChunkToDto);
        }

        // Apply metadata filters on parent ExtractedDocument -> SourceVersion -> ResearchSource
        List<EvidenceChunk> filtered = new ArrayList<>();
        for (EvidenceChunk chunk : candidateChunks.getContent()) {
            ExtractedDocument doc = documentRepository.findById(chunk.getExtractedDocumentId()).orElse(null);
            if (doc == null) continue;

            SourceVersion version = versionRepository.findById(doc.getSourceVersionId()).orElse(null);
            if (version == null) continue;

            ResearchSource source = sourceRepository.findById(version.getSourceId()).orElse(null);
            if (source == null) continue;

            if (sourceType != null && source.getSourceType() != sourceType) {
                continue;
            }

            if (publisher != null && !publisher.isBlank()) {
                if (source.getPublisher() == null || !source.getPublisher().toLowerCase().contains(publisher.toLowerCase())) {
                    continue;
                }
            }

            if (institutionId != null && !institutionId.isBlank()) {
                if (source.getInstitutionId() == null || !source.getInstitutionId().equalsIgnoreCase(institutionId)) {
                    continue;
                }
            }

            if (departmentId != null && !departmentId.isBlank()) {
                if (source.getDepartmentId() == null || !source.getDepartmentId().equalsIgnoreCase(departmentId)) {
                    continue;
                }
            }

            filtered.add(chunk);
        }

        List<EvidenceChunkDto> dtos = filtered.stream()
                .map(ingestionService::mapChunkToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, dtos.size());
    }
}

