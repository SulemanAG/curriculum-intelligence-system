package org.curriculumai.evidence;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.evidence.domain.SourceType;
import org.curriculumai.evidence.dto.EvidenceChunkDto;
import org.curriculumai.evidence.dto.RegisterSourceRequest;
import org.curriculumai.evidence.dto.ResearchSourceDto;
import org.curriculumai.evidence.repository.*;
import org.curriculumai.evidence.service.DocumentExtractionService;
import org.curriculumai.evidence.service.DocumentStorageService;
import org.curriculumai.evidence.service.EvidenceIngestionService;
import org.curriculumai.evidence.service.EvidenceSearchService;
import org.curriculumai.evidence.service.SafeUrlRetrievalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EvidenceSearchServiceTest {

    @TempDir
    Path tempStorageDir;

    @Autowired
    private ResearchSourceRepository sourceRepository;

    @Autowired
    private SourceRetrievalRepository retrievalRepository;

    @Autowired
    private SourceVersionRepository versionRepository;

    @Autowired
    private ExtractedDocumentRepository documentRepository;

    @Autowired
    private EvidenceChunkRepository chunkRepository;

    @Autowired
    private SafeUrlRetrievalService retrievalService;

    @Autowired
    private DocumentExtractionService extractionService;

    @Autowired
    private org.curriculumai.evidence.service.embedding.EvidenceEmbeddingService embeddingService;

    private EvidenceIngestionService ingestionService;
    private EvidenceSearchService searchService;

    @BeforeEach
    void setUp() throws IOException {
        DocumentStorageService storageService = new DocumentStorageService(tempStorageDir.toString());
        storageService.init();

        ingestionService = new EvidenceIngestionService(
                sourceRepository,
                retrievalRepository,
                versionRepository,
                documentRepository,
                chunkRepository,
                storageService,
                retrievalService,
                extractionService,
                embeddingService,
                new ObjectMapper()
        );

        searchService = new EvidenceSearchService(
                chunkRepository,
                documentRepository,
                versionRepository,
                sourceRepository,
                ingestionService
        );

        // Seed two distinct evidence sources for search testing
        RegisterSourceRequest req1 = new RegisterSourceRequest(
                "https://acm.org/cs-2023",
                SourceType.ACADEMIC_STANDARD,
                "ACM",
                "Computer Science Curricula 2023",
                null, null, null, null
        );
        ResearchSourceDto s1 = ingestionService.registerSource(req1, "usr-admin");
        String content1 = "# Distributed Algorithms\n\nStudents must master Raft consensus protocol and Byzantine fault tolerance in distributed networks.";
        MockMultipartFile f1 = new MockMultipartFile("file", "acm_cs.md", "text/markdown", content1.getBytes(StandardCharsets.UTF_8));
        ingestionService.ingestUploadedDocument(s1.id(), f1, "2023-12-01", "usr-admin");

        RegisterSourceRequest req2 = new RegisterSourceRequest(
                "https://nasscom.in/report-ai-2024",
                SourceType.INDUSTRY_REPORT,
                "NASSCOM",
                "AI Skill Demand in Enterprise Engineering",
                null, null, null, null
        );
        ResearchSourceDto s2 = ingestionService.registerSource(req2, "usr-admin");
        String content2 = "# Industry Skill Gaps\n\nIndian tech enterprises report severe talent shortages in agentic workflows, LLM orchestration, and vector search.";
        MockMultipartFile f2 = new MockMultipartFile("file", "nasscom.md", "text/markdown", content2.getBytes(StandardCharsets.UTF_8));
        ingestionService.ingestUploadedDocument(s2.id(), f2, "2024-06-01", "usr-admin");
    }

    @Test
    @DisplayName("Searches evidence chunks by text query with provenance")
    void testSearchByTextQuery() {
        Page<EvidenceChunkDto> results = searchService.searchEvidence(
                "Byzantine fault tolerance",
                null, null, null, null,
                PageRequest.of(0, 10)
        );

        assertFalse(results.isEmpty());
        assertTrue(results.getContent().get(0).textContent().contains("Byzantine fault tolerance"));
        assertNotNull(results.getContent().get(0).provenance());
        assertEquals("ACM", results.getContent().get(0).provenance().publisher());
    }

    @Test
    @DisplayName("Filters evidence chunks by SourceType")
    void testFilterBySourceType() {
        Page<EvidenceChunkDto> industryResults = searchService.searchEvidence(
                null,
                SourceType.INDUSTRY_REPORT,
                null, null, null,
                PageRequest.of(0, 10)
        );

        assertFalse(industryResults.isEmpty());
        assertTrue(industryResults.getContent().stream().allMatch(c -> c.provenance().sourceType() == SourceType.INDUSTRY_REPORT));
    }

    @Test
    @DisplayName("Filters evidence chunks by publisher name")
    void testFilterByPublisher() {
        Page<EvidenceChunkDto> nasscomResults = searchService.searchEvidence(
                null,
                null,
                "NASSCOM",
                null, null,
                PageRequest.of(0, 10)
        );

        assertFalse(nasscomResults.isEmpty());
        assertTrue(nasscomResults.getContent().stream().allMatch(c -> "NASSCOM".equalsIgnoreCase(c.provenance().publisher())));
    }
}

