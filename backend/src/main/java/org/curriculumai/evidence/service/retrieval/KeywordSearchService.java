package org.curriculumai.evidence.service.retrieval;

import org.curriculumai.evidence.domain.*;
import org.curriculumai.evidence.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KeywordSearchService {

    private static final Logger log = LoggerFactory.getLogger(KeywordSearchService.class);

    private final EvidenceChunkRepository chunkRepository;
    private final ExtractedDocumentRepository documentRepository;
    private final SourceVersionRepository versionRepository;
    private final ResearchSourceRepository sourceRepository;

    public record RankedResult(EvidenceChunk chunk, int rank, double score) {}

    public KeywordSearchService(EvidenceChunkRepository chunkRepository,
                                ExtractedDocumentRepository documentRepository,
                                SourceVersionRepository versionRepository,
                                ResearchSourceRepository sourceRepository) {
        this.chunkRepository = chunkRepository;
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
        this.sourceRepository = sourceRepository;
    }

    /**
     * Executes keyword search with lexical relevance scoring and metadata filtering.
     */
    public List<RankedResult> searchKeyword(String query, SourceType sourceType, String publisher,
                                           String institutionId, String departmentId, int limit) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        String trimmed = query.trim().toLowerCase();
        String[] terms = Arrays.stream(trimmed.split("\\s+"))
                .filter(t -> t.length() > 1)
                .toArray(String[]::new);

        if (terms.length == 0) {
            terms = new String[]{trimmed};
        }

        List<EvidenceChunk> allChunks = chunkRepository.findAll();
        List<ScoredCandidate> scored = new ArrayList<>();

        for (EvidenceChunk chunk : allChunks) {
            if (!passesMetadataFilters(chunk, sourceType, publisher, institutionId, departmentId)) {
                continue;
            }

            double score = computeLexicalScore(chunk, trimmed, terms);
            if (score > 0.0) {
                scored.add(new ScoredCandidate(chunk, score));
            }
        }

        // Sort descending by score, ascending by sequence number
        scored.sort((a, b) -> {
            int cmp = Double.compare(b.score, a.score);
            if (cmp != 0) return cmp;
            return Integer.compare(a.chunk.getSequenceNumber(), b.chunk.getSequenceNumber());
        });

        List<RankedResult> results = new ArrayList<>();
        int rank = 1;
        for (ScoredCandidate c : scored) {
            results.add(new RankedResult(c.chunk, rank++, c.score));
            if (results.size() >= limit) break;
        }

        return results;
    }

    private double computeLexicalScore(EvidenceChunk chunk, String fullQuery, String[] terms) {
        String text = chunk.getTextContent() != null ? chunk.getTextContent().toLowerCase() : "";
        String heading = chunk.getSectionHeading() != null ? chunk.getSectionHeading().toLowerCase() : "";

        double score = 0.0;

        // Exact phrase match bonus
        if (text.contains(fullQuery)) {
            score += 2.0;
        }
        if (heading.contains(fullQuery)) {
            score += 1.5;
        }

        // Term matches
        int matchedTerms = 0;
        for (String term : terms) {
            boolean inText = text.contains(term);
            boolean inHeading = heading.contains(term);
            if (inText || inHeading) {
                matchedTerms++;
                if (inText) {
                    // Count occurrences (up to 5 for saturation)
                    int count = 0;
                    int idx = 0;
                    while ((idx = text.indexOf(term, idx)) != -1 && count < 5) {
                        count++;
                        idx += term.length();
                    }
                    score += 0.2 * count;
                }
                if (inHeading) {
                    score += 0.5;
                }
            }
        }

        // Coverage bonus (what fraction of search terms matched)
        if (terms.length > 0) {
            score += 1.0 * ((double) matchedTerms / terms.length);
        }

        return score;
    }

    private boolean passesMetadataFilters(EvidenceChunk chunk, SourceType sourceType, String publisher,
                                          String institutionId, String departmentId) {
        if (sourceType == null && publisher == null && institutionId == null && departmentId == null) {
            return true;
        }

        ExtractedDocument doc = documentRepository.findById(chunk.getExtractedDocumentId()).orElse(null);
        if (doc == null) return false;

        SourceVersion version = versionRepository.findById(doc.getSourceVersionId()).orElse(null);
        if (version == null) return false;

        ResearchSource source = sourceRepository.findById(version.getSourceId()).orElse(null);
        if (source == null) return false;

        if (sourceType != null && source.getSourceType() != sourceType) {
            return false;
        }
        if (publisher != null && !publisher.isBlank()) {
            if (source.getPublisher() == null || !source.getPublisher().toLowerCase().contains(publisher.toLowerCase())) {
                return false;
            }
        }
        if (institutionId != null && !institutionId.isBlank()) {
            if (source.getInstitutionId() == null || !source.getInstitutionId().equalsIgnoreCase(institutionId)) {
                return false;
            }
        }
        if (departmentId != null && !departmentId.isBlank()) {
            if (source.getDepartmentId() == null || !source.getDepartmentId().equalsIgnoreCase(departmentId)) {
                return false;
            }
        }

        return true;
    }

    private record ScoredCandidate(EvidenceChunk chunk, double score) {}
}

