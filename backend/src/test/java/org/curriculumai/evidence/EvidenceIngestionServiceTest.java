package org.curriculumai.evidence;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.evidence.domain.ExtractionStatus;
import org.curriculumai.evidence.domain.ResearchSource;
import org.curriculumai.evidence.domain.SourceType;
import org.curriculumai.evidence.domain.SourceVersion;
import org.curriculumai.evidence.dto.EvidenceChunkDto;
import org.curriculumai.evidence.dto.RegisterSourceRequest;
import org.curriculumai.evidence.dto.ResearchSourceDto;
import org.curriculumai.evidence.dto.SourceVersionDto;
import org.curriculumai.evidence.repository.*;
import org.curriculumai.evidence.service.DocumentExtractionService;
import org.curriculumai.evidence.service.DocumentStorageService;
import org.curriculumai.evidence.service.EvidenceIngestionService;
import org.curriculumai.evidence.service.SafeUrlRetrievalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class EvidenceIngestionServiceTest {

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

    @BeforeEach
    void setUp() {
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
    }

    @Test
    @DisplayName("Registers source with canonical URL and domain extraction")
    void testRegisterSource() {
        RegisterSourceRequest req = new RegisterSourceRequest(
                "https://www.acm.org/education/curricula-recommendations",
                SourceType.ACADEMIC_STANDARD,
                "ACM",
                "ACM Curricula Recommendations",
                "Curriculum Guidelines",
                null, null, null
        );

        ResearchSourceDto source = ingestionService.registerSource(req, "usr-academic-admin");

        assertNotNull(source.id());
        assertEquals("www.acm.org", source.domain());
        assertEquals(SourceType.ACADEMIC_STANDARD, source.sourceType());
        assertEquals("ACM", source.publisher());
    }

    @Test
    @DisplayName("Re-registering existing canonical URL returns existing source without duplication")
    void testRegisterDuplicateSourceReturnsExisting() {
        RegisterSourceRequest req = new RegisterSourceRequest(
                "https://ieeexplore.ieee.org/document/software-engineering-curricula",
                SourceType.ACADEMIC_STANDARD,
                "IEEE",
                "Software Engineering Curricula",
                null, null, null, null
        );

        ResearchSourceDto s1 = ingestionService.registerSource(req, "usr-academic-admin");
        ResearchSourceDto s2 = ingestionService.registerSource(req, "usr-academic-admin");

        assertEquals(s1.id(), s2.id());
    }

    @Test
    @DisplayName("Ingests uploaded document, creates version v1, extracts document, and creates traceable chunks")
    void testIngestUploadedDocument() throws IOException {
        RegisterSourceRequest req = new RegisterSourceRequest(
                "https://acm.org/guidelines/ai-curricula-2024",
                SourceType.ACADEMIC_STANDARD,
                "ACM",
                "ACM AI Curricula 2024",
                null, null, null, null
        );
        ResearchSourceDto source = ingestionService.registerSource(req, "usr-academic-admin");

        String docContent = """
                # Autonomous AI Agents Curriculum Specification
                
                Students must understand foundational architectures including ReAct, Plan-and-Execute, and Multi-Agent Orchestration.
                
                ## Practical Lab Competencies
                
                Laboratory sessions must include designing coordinator-worker swarms, message bus isolation, and transactional state stores.
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "ai_curricula.md",
                "text/markdown",
                docContent.getBytes(StandardCharsets.UTF_8)
        );

        SourceVersionDto version = ingestionService.ingestUploadedDocument(source.id(), file, "2024-01-15", "usr-academic-admin");

        assertNotNull(version.id());
        assertEquals("v1", version.versionIdentifier());
        assertNotNull(version.contentHash());
        assertNotNull(version.storageReference());

        // Verify extracted document exists
        var extractedDoc = ingestionService.getExtractedDocumentByVersionId(version.id());
        assertEquals(ExtractionStatus.COMPLETED, extractedDoc.extractionStatus());
        assertTrue(extractedDoc.totalParagraphs() >= 2);

        // Verify evidence chunks exist
        var chunks = chunkRepository.findByExtractedDocumentIdOrderBySequenceNumberAsc(extractedDoc.id());
        assertFalse(chunks.isEmpty());

        EvidenceChunkDto chunkDto = ingestionService.getChunk(chunks.get(0).getId());
        assertNotNull(chunkDto.provenance());
        assertEquals("ACM", chunkDto.provenance().publisher());
        assertEquals(source.canonicalUrl(), chunkDto.provenance().canonicalUrl());
        assertEquals("v1", chunkDto.provenance().versionIdentifier());
    }

    @Test
    @DisplayName("Re-uploading identical document detects content hash and prevents duplicate version/chunks")
    void testDuplicateUploadDeduplication() throws IOException {
        RegisterSourceRequest req = new RegisterSourceRequest(
                "https://nasscom.in/insights/future-skills-prime-2025",
                SourceType.INDUSTRY_REPORT,
                "NASSCOM",
                "FutureSkills Prime Industry Needs",
                null, null, null, null
        );
        ResearchSourceDto source = ingestionService.registerSource(req, "usr-academic-admin");

        String content = "# Cloud and Distributed Computing Demand\n\nHigh industry demand for engineers skilled in microservices and PostgreSQL.";
        MockMultipartFile file = new MockMultipartFile("file", "report.md", "text/markdown", content.getBytes(StandardCharsets.UTF_8));

        SourceVersionDto v1 = ingestionService.ingestUploadedDocument(source.id(), file, "2025-02-01", "usr-academic-admin");
        SourceVersionDto v2 = ingestionService.ingestUploadedDocument(source.id(), file, "2025-02-01", "usr-academic-admin");

        assertEquals(v1.id(), v2.id());
        assertEquals(v1.contentHash(), v2.contentHash());
        assertEquals(1, versionRepository.countBySourceId(source.id()));
    }

    @Test
    @DisplayName("Uploading modified document creates sequential version v2")
    void testModifiedDocumentCreatesVersionV2() throws IOException {
        RegisterSourceRequest req = new RegisterSourceRequest(
                "https://abet.org/criteria/2026-criteria",
                SourceType.ACADEMIC_STANDARD,
                "ABET",
                "Engineering Accreditation Criteria",
                null, null, null, null
        );
        ResearchSourceDto source = ingestionService.registerSource(req, "usr-academic-admin");

        String content1 = "# Criteria Version 1\n\nEngineering design must integrate realistic constraints.";
        MockMultipartFile file1 = new MockMultipartFile("file", "criteria_v1.md", "text/markdown", content1.getBytes(StandardCharsets.UTF_8));
        SourceVersionDto v1 = ingestionService.ingestUploadedDocument(source.id(), file1, "2025-01-01", "usr-academic-admin");

        String content2 = "# Criteria Version 2 (Revised)\n\nEngineering design must incorporate AI ethics, sustainability, and data privacy.";
        MockMultipartFile file2 = new MockMultipartFile("file", "criteria_v2.md", "text/markdown", content2.getBytes(StandardCharsets.UTF_8));
        SourceVersionDto v2 = ingestionService.ingestUploadedDocument(source.id(), file2, "2026-01-01", "usr-academic-admin");

        assertNotEquals(v1.id(), v2.id());
        assertEquals("v1", v1.versionIdentifier());
        assertEquals("v2", v2.versionIdentifier());
        assertEquals(2, versionRepository.countBySourceId(source.id()));
    }
}

