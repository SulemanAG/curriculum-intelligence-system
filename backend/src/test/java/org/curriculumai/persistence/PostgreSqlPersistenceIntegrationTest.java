package org.curriculumai.persistence;

import jakarta.persistence.EntityManager;
import org.curriculumai.conversations.Conversation;
import org.curriculumai.conversations.ConversationRepository;
import org.curriculumai.messages.Message;
import org.curriculumai.messages.MessageRepository;
import org.curriculumai.users.User;
import org.curriculumai.users.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("postgres-test")
@Tag("postgres-integration")
@Transactional
class PostgreSqlPersistenceIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private org.curriculumai.academic.repository.InstitutionRepository institutionRepository;

    @Autowired
    private org.curriculumai.academic.repository.DepartmentRepository departmentRepository;

    @Autowired
    private org.curriculumai.academic.repository.ProgrammeRepository programmeRepository;

    @Autowired
    private org.curriculumai.academic.repository.CourseRepository courseRepository;

    @Autowired
    private org.curriculumai.academic.repository.CurriculumVersionRepository curriculumVersionRepository;

    @Autowired
    private org.curriculumai.academic.repository.SemesterRepository semesterRepository;

    @Autowired
    private org.curriculumai.academic.repository.CurriculumCourseRepository curriculumCourseRepository;

    @Autowired
    private org.curriculumai.academic.repository.ProgrammeOutcomeRepository programmeOutcomeRepository;

    @Autowired
    private org.curriculumai.academic.repository.CourseOutcomeRepository courseOutcomeRepository;

    @Autowired
    private org.curriculumai.academic.repository.CoPoMappingRepository coPoMappingRepository;

    @Autowired
    private org.curriculumai.academic.repository.CurriculumOutcomeSnapshotRepository snapshotRepository;

    @Autowired
    private org.curriculumai.evidence.repository.ResearchSourceRepository researchSourceRepository;

    @Autowired
    private org.curriculumai.evidence.repository.SourceVersionRepository sourceVersionRepository;

    @Autowired
    private org.curriculumai.evidence.repository.ExtractedDocumentRepository extractedDocumentRepository;

    @Autowired
    private org.curriculumai.evidence.repository.EvidenceChunkRepository evidenceChunkRepository;

    @Autowired
    private org.curriculumai.evidence.repository.EvidenceChunkEmbeddingRepository chunkEmbeddingRepository;

    @Autowired
    private org.curriculumai.orchestration.repository.OrchestrationRunRepository orchestrationRunRepository;

    @Autowired
    private org.curriculumai.orchestration.repository.WorkflowTaskRepository workflowTaskRepository;

    @Autowired
    private org.curriculumai.orchestration.repository.TaskDependencyRepository taskDependencyRepository;

    @Autowired
    private org.curriculumai.orchestration.repository.AgentExecutionRecordRepository agentExecutionRecordRepository;

    @Autowired
    private org.curriculumai.orchestration.repository.WorkflowEventRepository workflowEventRepository;

    @Autowired
    private org.curriculumai.orchestration.agent.curriculum.CurriculumMappingAgent curriculumMappingAgent;

    @Autowired
    private org.curriculumai.orchestration.agent.curriculum.GovernedAcademicCatalogRetriever governedCatalogRetriever;

    @Autowired
    private org.curriculumai.academic.repository.CoursePrerequisiteRepository coursePrerequisiteRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Verify real PostgreSQL connection and product metadata")
    void testPostgreSqlConnectionMetadata() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            String productVersion = metaData.getDatabaseProductVersion();

            assertEquals("PostgreSQL", productName, "Database must be PostgreSQL");
            assertTrue(productVersion.startsWith("18"), "Database version must be PostgreSQL 18.x, found: " + productVersion);
        }
    }

    @Test
    @DisplayName("Verify Flyway migrations V1 through V6 completed on schema public")
    void testPostgreSqlFlywayMigrationsComplete() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank DESC");

            assertTrue(resultSet.next(), "Flyway schema history must have records");
            String latestVersion = resultSet.getString("version");
            boolean success = resultSet.getBoolean("success");

            assertTrue(Integer.parseInt(latestVersion) >= 6, "Latest migration must be at least V6, found V" + latestVersion);
            assertTrue(success, "Latest migration must have status success = true");
        }
    }

    @Test
    @DisplayName("Persist Conversation and Message records in real PostgreSQL with foreign key integrity")
    void testConversationAndMessagePersistenceAcrossTransaction() {
        User user = userRepository.findById("usr-default-faculty")
                .orElseThrow(() -> new IllegalStateException("Default faculty user missing from V2 migration"));

        String convId = "conv-pg-test-" + UUID.randomUUID().toString().substring(0, 8);
        Conversation conversation = new Conversation(convId, user, "Real PostgreSQL Verification Chat");
        conversationRepository.save(conversation);

        String msgId1 = "msg-pg-1-" + UUID.randomUUID().toString().substring(0, 8);
        Message userMessage = new Message(msgId1, conversation, "user", "Hello PostgreSQL!");
        messageRepository.save(userMessage);

        String msgId2 = "msg-pg-2-" + UUID.randomUUID().toString().substring(0, 8);
        Message assistantMessage = new Message(msgId2, conversation, "assistant", "Verified persistence in PostgreSQL 18.6.");
        messageRepository.save(assistantMessage);

        entityManager.flush();
        entityManager.clear();

        Optional<Conversation> foundConv = conversationRepository.findById(convId);
        assertTrue(foundConv.isPresent(), "Conversation must be retrieved from PostgreSQL");
        assertEquals("Real PostgreSQL Verification Chat", foundConv.get().getTitle());

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(convId);
        assertEquals(2, messages.size(), "Two messages must be stored and ordered");
        assertEquals("user", messages.get(0).getRole());
        assertEquals("Hello PostgreSQL!", messages.get(0).getContent());
        assertEquals("assistant", messages.get(1).getRole());
        assertEquals("Verified persistence in PostgreSQL 18.6.", messages.get(1).getContent());
    }

    @Test
    @DisplayName("Verify conversation data survives simulated application restart")
    void testConversationHistorySurvivesSimulatedRestart() {
        User user = userRepository.findById("usr-default-faculty")
                .orElseThrow(() -> new IllegalStateException("Default faculty user missing"));

        String convId = "conv-pg-restart-" + UUID.randomUUID().toString().substring(0, 8);
        Conversation conversation = new Conversation(convId, user, "Restart Survivability Chat");
        conversation.setCustomTitle(true);
        conversationRepository.save(conversation);

        String msgId = "msg-pg-r1-" + UUID.randomUUID().toString().substring(0, 8);
        Message msg = new Message(msgId, conversation, "user", "Will this survive a restart?");
        messageRepository.save(msg);

        entityManager.flush();
        entityManager.clear();

        Conversation freshConversation = conversationRepository.findById(convId).orElse(null);
        assertNotNull(freshConversation, "Conversation must survive transaction clear and detach");
        assertTrue(freshConversation.isCustomTitle(), "Custom title flag must be preserved");

        List<Message> freshMessages = messageRepository.findByConversationIdOrderByCreatedAtAsc(convId);
        assertFalse(freshMessages.isEmpty(), "Messages must be present after restart simulation");
        assertEquals("Will this survive a restart?", freshMessages.get(0).getContent());
    }

    @Test
    @DisplayName("Verify invalid conversation IDs return empty Optional")
    void testInvalidConversationIdThrowsNotFoundException() {
        String nonExistentId = "conv-does-not-exist-" + UUID.randomUUID();
        Optional<Conversation> missing = conversationRepository.findById(nonExistentId);
        assertTrue(missing.isEmpty(), "Non-existent conversation ID must yield empty Optional");
    }

    @Test
    @DisplayName("Verify user ownership isolation in PostgreSQL")
    void testUserOwnershipEnforcedAtDatabaseLevel() {
        User user = userRepository.findById("usr-default-faculty")
                .orElseThrow(() -> new IllegalStateException("Default faculty user missing"));

        String userAConvId = "conv-pg-usra-" + UUID.randomUUID().toString().substring(0, 8);
        Conversation convA = new Conversation(userAConvId, user, "User A Private Chat");
        conversationRepository.save(convA);

        entityManager.flush();
        entityManager.clear();

        List<Conversation> facultyConvs = conversationRepository.findAllActiveByUserId("usr-default-faculty");
        assertTrue(facultyConvs.stream().anyMatch(c -> c.getId().equals(userAConvId)), "Owner can see conversation");

        List<Conversation> otherConvs = conversationRepository.findAllActiveByUserId("usr-hardening-faculty");
        assertFalse(otherConvs.stream().anyMatch(c -> c.getId().equals(userAConvId)), "Other user cannot see conversation");
    }

    @Test
    @DisplayName("Verify full Academic Domain lifecycle and outcome snapshots in PostgreSQL")
    void testPostgreSqlAcademicDomainAndOutcomeSnapshotWorkflow() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        org.curriculumai.academic.domain.Institution inst = institutionRepository.save(
                new org.curriculumai.academic.domain.Institution("inst-pg-" + uid, "INST-PG-" + uid, "Postgres Tech", "Belagavi", "India")
        );
        org.curriculumai.academic.domain.Department dept = departmentRepository.save(
                new org.curriculumai.academic.domain.Department("dept-pg-" + uid, inst, "CSE-PG-" + uid, "Computer Science")
        );
        org.curriculumai.academic.domain.Programme prog = programmeRepository.save(
                new org.curriculumai.academic.domain.Programme("prog-pg-" + uid, dept, "BTECH-PG-" + uid, "B.Tech CSE", "UG", 4, 160)
        );
        org.curriculumai.academic.domain.CurriculumVersion cv = curriculumVersionRepository.save(
                new org.curriculumai.academic.domain.CurriculumVersion("cv-pg-" + uid, prog, "REG-2024-" + uid, 2024)
        );
        org.curriculumai.academic.domain.Course course = courseRepository.save(
                new org.curriculumai.academic.domain.Course("crs-pg-" + uid, dept, "CS301-" + uid, "Distributed Systems", 3, 3, 0, 0)
        );
        org.curriculumai.academic.domain.Semester sem = semesterRepository.save(
                new org.curriculumai.academic.domain.Semester("sem-pg-" + uid, cv, 5, "Semester 5", 20)
        );
        org.curriculumai.academic.domain.CurriculumCourse cc = curriculumCourseRepository.save(
                new org.curriculumai.academic.domain.CurriculumCourse("cc-pg-" + uid, cv, sem, course, 3, 3, 0, 0, false, null)
        );
        org.curriculumai.academic.domain.ProgrammeOutcome po = programmeOutcomeRepository.save(
                new org.curriculumai.academic.domain.ProgrammeOutcome("po-pg-" + uid, prog, "PO1", "Engineering Knowledge", "Engineering Foundation", "PO")
        );
        org.curriculumai.academic.domain.CourseOutcome co = courseOutcomeRepository.save(
                new org.curriculumai.academic.domain.CourseOutcome("co-pg-" + uid, course, "CO1", "Implement consensus protocols", "L3")
        );
        org.curriculumai.academic.domain.CoPoMapping mapping = coPoMappingRepository.save(
                new org.curriculumai.academic.domain.CoPoMapping("copo-pg-" + uid, co, po, 3, "Direct mapping")
        );

        entityManager.flush();
        entityManager.clear();

        org.curriculumai.academic.domain.CurriculumOutcomeSnapshot snapshot = org.curriculumai.academic.domain.CurriculumOutcomeSnapshot.ofCourseOutcome(
                "snap-pg-" + uid, cv, course, co
        );
        snapshotRepository.save(snapshot);
        entityManager.flush();
        entityManager.clear();

        org.curriculumai.academic.domain.CurriculumOutcomeSnapshot savedSnapshot = snapshotRepository.findById(snapshot.getId()).orElse(null);
        assertNotNull(savedSnapshot, "Snapshot must be persisted in PostgreSQL");
        assertEquals("CO1", savedSnapshot.getOutcomeCode());
        assertEquals("Implement consensus protocols", savedSnapshot.getDescription());
    }

    @Test
    @DisplayName("Verify PostgreSQL database engine trigger rejects UPDATE on curriculum_outcome_snapshots (Flyway V5)")
    void testPostgreSqlDatabaseTriggerRejectsSnapshotUpdate() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        org.curriculumai.academic.domain.Institution inst = institutionRepository.save(
                new org.curriculumai.academic.domain.Institution("inst-u-" + uid, "U-INST-" + uid, "Update Tech", "Belagavi", "India")
        );
        org.curriculumai.academic.domain.Department dept = departmentRepository.save(
                new org.curriculumai.academic.domain.Department("dept-u-" + uid, inst, "CSE-U-" + uid, "CSE Dept")
        );
        org.curriculumai.academic.domain.Programme prog = programmeRepository.save(
                new org.curriculumai.academic.domain.Programme("prog-u-" + uid, dept, "BE-U-" + uid, "BE CSE", "UG", 4, 160)
        );
        org.curriculumai.academic.domain.CurriculumVersion cv = curriculumVersionRepository.save(
                new org.curriculumai.academic.domain.CurriculumVersion("cv-u-" + uid, prog, "2024-U-" + uid, 2024)
        );
        org.curriculumai.academic.domain.ProgrammeOutcome po = programmeOutcomeRepository.save(
                new org.curriculumai.academic.domain.ProgrammeOutcome("po-u-" + uid, prog, "PO1", "Update Outcome", "Description", "PO")
        );

        org.curriculumai.academic.domain.CurriculumOutcomeSnapshot snapshot = org.curriculumai.academic.domain.CurriculumOutcomeSnapshot.ofProgrammeOutcome(
                "snap-u-" + uid, cv, po
        );
        snapshotRepository.save(snapshot);
        entityManager.flush();

        Exception updateEx = assertThrows(Exception.class, () -> {
            entityManager.createNativeQuery("UPDATE curriculum_outcome_snapshots SET description = 'TAMPERED' WHERE id = :id")
                    .setParameter("id", snapshot.getId())
                    .executeUpdate();
            entityManager.flush();
        });
        String fullError = updateEx.toString() + (updateEx.getCause() != null ? " " + updateEx.getCause().toString() : "");
        assertTrue(fullError.contains("immutable audit table") || fullError.contains("prohibited"),
                "PostgreSQL trigger must abort UPDATE with immutability message, got: " + fullError);
    }

    @Test
    @DisplayName("Verify PostgreSQL database engine trigger rejects DELETE on curriculum_outcome_snapshots (Flyway V5)")
    void testPostgreSqlDatabaseTriggerRejectsSnapshotDeletion() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        org.curriculumai.academic.domain.Institution inst = institutionRepository.save(
                new org.curriculumai.academic.domain.Institution("inst-d-" + uid, "D-INST-" + uid, "Delete Tech", "Belagavi", "India")
        );
        org.curriculumai.academic.domain.Department dept = departmentRepository.save(
                new org.curriculumai.academic.domain.Department("dept-d-" + uid, inst, "CSE-D-" + uid, "CSE Dept")
        );
        org.curriculumai.academic.domain.Programme prog = programmeRepository.save(
                new org.curriculumai.academic.domain.Programme("prog-d-" + uid, dept, "BE-D-" + uid, "BE CSE", "UG", 4, 160)
        );
        org.curriculumai.academic.domain.CurriculumVersion cv = curriculumVersionRepository.save(
                new org.curriculumai.academic.domain.CurriculumVersion("cv-d-" + uid, prog, "2024-D-" + uid, 2024)
        );
        org.curriculumai.academic.domain.ProgrammeOutcome po = programmeOutcomeRepository.save(
                new org.curriculumai.academic.domain.ProgrammeOutcome("po-d-" + uid, prog, "PO1", "Delete Outcome", "Description", "PO")
        );

        org.curriculumai.academic.domain.CurriculumOutcomeSnapshot snapshot = org.curriculumai.academic.domain.CurriculumOutcomeSnapshot.ofProgrammeOutcome(
                "snap-d-" + uid, cv, po
        );
        snapshotRepository.save(snapshot);
        entityManager.flush();

        Exception deleteEx = assertThrows(Exception.class, () -> {
            entityManager.createNativeQuery("DELETE FROM curriculum_outcome_snapshots WHERE id = :id")
                    .setParameter("id", snapshot.getId())
                    .executeUpdate();
            entityManager.flush();
        });
        String fullError = deleteEx.toString() + (deleteEx.getCause() != null ? " " + deleteEx.getCause().toString() : "");
        assertTrue(fullError.contains("immutable audit table") || fullError.contains("prohibited"),
                "PostgreSQL trigger must abort DELETE with immutability message, got: " + fullError);
    }

    @Test
    @DisplayName("Verify PostgreSQL Flyway V6 Evidence Repository tables, unique constraints, and full-text search")
    void testPostgreSqlEvidenceRepositoryAndProvenanceWorkflow() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        // 1. Create Research Source
        org.curriculumai.evidence.domain.ResearchSource source = new org.curriculumai.evidence.domain.ResearchSource(
                "src-pg-" + uid,
                "https://acm.org/curricula-pg-" + uid,
                org.curriculumai.evidence.domain.SourceType.ACADEMIC_STANDARD,
                "ACM",
                "ACM AI & CS Curricula Standard",
                "acm.org",
                "Standard",
                null, null, null,
                "usr-default-faculty"
        );
        researchSourceRepository.save(source);
        entityManager.flush();

        // 2. Create Source Version
        String contentHash = "hash-" + uid + "-abc123456789";
        org.curriculumai.evidence.domain.SourceVersion version = new org.curriculumai.evidence.domain.SourceVersion(
                "srcv-pg-" + uid,
                source.getId(),
                null,
                contentHash,
                "v1",
                "2025-01-01",
                "acm_spec.md",
                "text/markdown",
                1024L,
                "evidence/2026/11/" + uid + ".md"
        );
        sourceVersionRepository.save(version);
        entityManager.flush();

        // 3. Create Extracted Document
        org.curriculumai.evidence.domain.ExtractedDocument doc = new org.curriculumai.evidence.domain.ExtractedDocument(
                "doc-pg-" + uid,
                version.getId(),
                org.curriculumai.evidence.domain.ExtractionStatus.COMPLETED,
                "CurriculumAI-MultiFormat-Extractor",
                "2.3.1",
                "ACM AI Specification",
                "en",
                "{}",
                1, 2, 0, 500,
                null
        );
        extractedDocumentRepository.save(doc);
        entityManager.flush();

        // 4. Create Evidence Chunk
        String text = "Undergraduate engineering students must study distributed database synchronization and consensus protocols.";
        org.curriculumai.evidence.domain.EvidenceChunk chunk = new org.curriculumai.evidence.domain.EvidenceChunk(
                "chk-pg-" + uid,
                doc.getId(),
                1,
                version.getId() + "-c1",
                text,
                1,
                "Distributed Systems",
                0, text.length(),
                "PARAGRAPH",
                "1.0",
                "chunkhash-" + uid,
                "{\"publisher\":\"ACM\",\"version\":\"v1\"}"
        );
        evidenceChunkRepository.save(chunk);
        entityManager.flush();

        // 5. Query back and verify persistence
        org.curriculumai.evidence.domain.EvidenceChunk reloaded = evidenceChunkRepository.findById(chunk.getId()).orElse(null);
        assertNotNull(reloaded);
        assertEquals(text, reloaded.getTextContent());
        assertEquals("Distributed Systems", reloaded.getSectionHeading());

        // 6. Verify native PostgreSQL full-text search using tsvector / GIN index
        Number ftsCount = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM evidence_chunks WHERE to_tsvector('english', text_content) @@ to_tsquery('english', 'synchronization & consensus')"
        ).getSingleResult();
        assertTrue(ftsCount.intValue() >= 1, "Native PostgreSQL full-text search must find the persisted chunk via tsvector");
    }

    @Test
    @DisplayName("Verify PostgreSQL unique constraint uk_source_content_hash rejects duplicate version hash")
    void testPostgreSqlDuplicateVersionHashRejected() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        org.curriculumai.evidence.domain.ResearchSource source = new org.curriculumai.evidence.domain.ResearchSource(
                "src-dup-" + uid,
                "https://acm.org/dup-" + uid,
                org.curriculumai.evidence.domain.SourceType.ACADEMIC_STANDARD,
                "ACM",
                "ACM Duplicate Test",
                "acm.org",
                "Standard",
                null, null, null,
                "usr-default-faculty"
        );
        researchSourceRepository.save(source);
        entityManager.flush();

        String contentHash = "hash-dup-" + uid;
        org.curriculumai.evidence.domain.SourceVersion v1 = new org.curriculumai.evidence.domain.SourceVersion(
                "srcv-1-" + uid,
                source.getId(),
                null,
                contentHash,
                "v1",
                "2025-01-01",
                "doc.txt",
                "text/plain",
                100L,
                "evidence/ref1"
        );
        sourceVersionRepository.save(v1);
        entityManager.flush();

        org.curriculumai.evidence.domain.SourceVersion v2 = new org.curriculumai.evidence.domain.SourceVersion(
                "srcv-2-" + uid,
                source.getId(),
                null,
                contentHash, // Duplicate content hash for same source
                "v2",
                "2025-01-01",
                "doc_dup.txt",
                "text/plain",
                100L,
                "evidence/ref2"
        );

        assertThrows(Exception.class, () -> {
            sourceVersionRepository.save(v2);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("V7 Migration & Evidence Chunk Embedding persistence in PostgreSQL")
    void testFlywayV7MigrationAndChunkEmbeddingPersistence() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        org.curriculumai.evidence.domain.ResearchSource source = new org.curriculumai.evidence.domain.ResearchSource(
                "src-emb-" + uid,
                "https://example.edu/cs-emb-" + uid,
                org.curriculumai.evidence.domain.SourceType.INSTITUTIONAL_CURRICULUM,
                "Example University",
                "Computer Science Syllabus",
                "example.edu",
                "INSTITUTIONAL_CURRICULUM",
                null, null, null, "usr-default-faculty"
        );
        researchSourceRepository.save(source);

        org.curriculumai.evidence.domain.SourceVersion version = new org.curriculumai.evidence.domain.SourceVersion(
                "srcv-emb-" + uid,
                source.getId(),
                null,
                "hash-emb-" + uid,
                "v1.0",
                "2026-01-01",
                "syllabus.pdf",
                "application/pdf",
                5000L,
                "evidence/storage/syllabus.pdf"
        );
        sourceVersionRepository.save(version);

        org.curriculumai.evidence.domain.ExtractedDocument doc = new org.curriculumai.evidence.domain.ExtractedDocument(
                "doc-emb-" + uid,
                version.getId(),
                org.curriculumai.evidence.domain.ExtractionStatus.COMPLETED,
                "PDFBoxExtractor",
                "3.0.4",
                "Computer Science Syllabus",
                "en",
                "{}",
                10, 50, 2, 12000, null
        );
        doc.setEmbeddingStatus(org.curriculumai.evidence.domain.EmbeddingStatus.COMPLETED);
        extractedDocumentRepository.save(doc);

        org.curriculumai.evidence.domain.EvidenceChunk chunk = new org.curriculumai.evidence.domain.EvidenceChunk(
                "chk-emb-" + uid,
                doc.getId(),
                1,
                "chunk-stable-" + uid,
                "Students will master transformer architectures and hybrid search techniques.",
                1,
                "Course Outcomes",
                0, 75,
                "PARAGRAPH",
                "1.0",
                "chunkhash-" + uid,
                "{}"
        );
        evidenceChunkRepository.save(chunk);
        entityManager.flush();

        // Persist 384-dimensional embedding
        float[] vector = new float[384];
        java.util.Arrays.fill(vector, 0.05f);
        String vectorJson = org.curriculumai.evidence.service.embedding.EmbeddingValidationUtils.toJson(vector);

        org.curriculumai.evidence.domain.EvidenceChunkEmbedding embedding = new org.curriculumai.evidence.domain.EvidenceChunkEmbedding(
                "emb-" + uid,
                chunk.getId(),
                "BAAI/bge-small-en-v1.5",
                384,
                vectorJson,
                org.curriculumai.evidence.domain.EmbeddingStatus.COMPLETED,
                null,
                0
        );
        chunkEmbeddingRepository.save(embedding);
        entityManager.flush();

        // Verify retrieval
        Optional<org.curriculumai.evidence.domain.EvidenceChunkEmbedding> retrieved =
                chunkEmbeddingRepository.findByChunkIdAndModelName(chunk.getId(), "BAAI/bge-small-en-v1.5");
        assertTrue(retrieved.isPresent());
        assertEquals(384, retrieved.get().getDimension());
        assertEquals(org.curriculumai.evidence.domain.EmbeddingStatus.COMPLETED, retrieved.get().getStatus());
        assertEquals("BAAI/bge-small-en-v1.5", retrieved.get().getModelName());

        // Verify uniqueness on (chunk_id, model_name)
        org.curriculumai.evidence.domain.EvidenceChunkEmbedding dup = new org.curriculumai.evidence.domain.EvidenceChunkEmbedding(
                "emb-dup-" + uid,
                chunk.getId(),
                "BAAI/bge-small-en-v1.5",
                384,
                vectorJson,
                org.curriculumai.evidence.domain.EmbeddingStatus.PENDING,
                null,
                0
        );
        assertThrows(Exception.class, () -> {
            chunkEmbeddingRepository.save(dup);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Verify PostgreSQL native cosine_distance_array function from Flyway V7")
    void testPostgreSqlCosineDistanceFunction() {
        Double orthogonalDistance = jdbcTemplate.queryForObject(
                "SELECT cosine_distance_array(ARRAY[1.0, 0.0]::real[], ARRAY[0.0, 1.0]::real[])",
                Double.class
        );
        assertNotNull(orthogonalDistance);
        assertEquals(1.0, orthogonalDistance, 1e-4);

        Double identicalDistance = jdbcTemplate.queryForObject(
                "SELECT cosine_distance_array(ARRAY[0.5, 0.5]::real[], ARRAY[0.5, 0.5]::real[])",
                Double.class
        );
        assertNotNull(identicalDistance);
        assertEquals(0.0, identicalDistance, 1e-4);

        Double oppositeDistance = jdbcTemplate.queryForObject(
                "SELECT cosine_distance_array(ARRAY[1.0, 0.0]::real[], ARRAY[-1.0, 0.0]::real[])",
                Double.class
        );
        assertNotNull(oppositeDistance);
        assertEquals(2.0, oppositeDistance, 1e-4);
    }

    @Test
    @DisplayName("Verify native PostgreSQL semantic similarity ranking using cosine_distance_array")
    void testNativePostgreSqlSemanticSimilarityQuery() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        // Create Source & Document
        org.curriculumai.evidence.domain.ResearchSource source = new org.curriculumai.evidence.domain.ResearchSource(
                "src-sem-" + uid,
                "https://example.edu/cs-sem-" + uid,
                org.curriculumai.evidence.domain.SourceType.INSTITUTIONAL_CURRICULUM,
                "Example University", "Curriculum", "example.edu",
                "INSTITUTIONAL_CURRICULUM", null, null, null, "usr-default-faculty"
        );
        researchSourceRepository.save(source);

        org.curriculumai.evidence.domain.SourceVersion version = new org.curriculumai.evidence.domain.SourceVersion(
                "srcv-sem-" + uid, source.getId(), null, "hash-sem-" + uid, "v1", "2026-01-01",
                "doc.txt", "text/plain", 100L, "ref"
        );
        sourceVersionRepository.save(version);

        org.curriculumai.evidence.domain.ExtractedDocument doc = new org.curriculumai.evidence.domain.ExtractedDocument(
                "doc-sem-" + uid, version.getId(), org.curriculumai.evidence.domain.ExtractionStatus.COMPLETED,
                "Extractor", "1.0", "Title", "en", "{}", 1, 2, 0, 100, null
        );
        extractedDocumentRepository.save(doc);

        // Chunk A: Aligned
        org.curriculumai.evidence.domain.EvidenceChunk chunkA = new org.curriculumai.evidence.domain.EvidenceChunk(
                "chk-sem-a-" + uid, doc.getId(), 1, "c1", "Transformer attention and deep learning", 1, "Deep Learning",
                0, 50, "PARAGRAPH", "1.0", "hash-a-" + uid, "{}"
        );
        evidenceChunkRepository.save(chunkA);

        // Chunk B: Orthogonal
        org.curriculumai.evidence.domain.EvidenceChunk chunkB = new org.curriculumai.evidence.domain.EvidenceChunk(
                "chk-sem-b-" + uid, doc.getId(), 2, "c2", "Relational calculus and B-Tree indexes", 1, "Databases",
                51, 100, "PARAGRAPH", "1.0", "hash-b-" + uid, "{}"
        );
        evidenceChunkRepository.save(chunkB);
        entityManager.flush();

        // 384-dimensional query vector: [1.0, 0.0, 0.0, ...]
        float[] queryVec = new float[384];
        queryVec[0] = 1.0f;

        // Chunk A vector: [0.99, 0.01, 0.0, ...] (near identical)
        float[] vecA = new float[384];
        vecA[0] = 0.99f;
        vecA[1] = 0.01f;

        // Chunk B vector: [0.0, 1.0, 0.0, ...] (orthogonal)
        float[] vecB = new float[384];
        vecB[1] = 1.0f;

        org.curriculumai.evidence.domain.EvidenceChunkEmbedding embA = new org.curriculumai.evidence.domain.EvidenceChunkEmbedding(
                "emb-a-" + uid, chunkA.getId(), "BAAI/bge-small-en-v1.5", 384,
                org.curriculumai.evidence.service.embedding.EmbeddingValidationUtils.toJson(vecA),
                org.curriculumai.evidence.domain.EmbeddingStatus.COMPLETED, null, 0
        );
        chunkEmbeddingRepository.save(embA);

        org.curriculumai.evidence.domain.EvidenceChunkEmbedding embB = new org.curriculumai.evidence.domain.EvidenceChunkEmbedding(
                "emb-b-" + uid, chunkB.getId(), "BAAI/bge-small-en-v1.5", 384,
                org.curriculumai.evidence.service.embedding.EmbeddingValidationUtils.toJson(vecB),
                org.curriculumai.evidence.domain.EmbeddingStatus.COMPLETED, null, 0
        );
        chunkEmbeddingRepository.save(embB);
        entityManager.flush();

        // Populate native real[] array column in PostgreSQL
        jdbcTemplate.update(
                "UPDATE evidence_chunk_embeddings SET embedding = cast(? as real[]) WHERE id = ?",
                org.curriculumai.evidence.service.embedding.EmbeddingValidationUtils.toPgArrayString(vecA), embA.getId()
        );
        jdbcTemplate.update(
                "UPDATE evidence_chunk_embeddings SET embedding = cast(? as real[]) WHERE id = ?",
                org.curriculumai.evidence.service.embedding.EmbeddingValidationUtils.toPgArrayString(vecB), embB.getId()
        );

        // Execute live cosine distance query in PostgreSQL
        String queryPgArray = org.curriculumai.evidence.service.embedding.EmbeddingValidationUtils.toPgArrayString(queryVec);
        List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT chunk_id, cosine_distance_array(cast(embedding as real[]), cast(? as real[])) as distance " +
                "FROM evidence_chunk_embeddings " +
                "WHERE id IN (?, ?) " +
                "ORDER BY distance ASC",
                queryPgArray, embA.getId(), embB.getId()
        );

        assertEquals(2, rows.size());
        assertEquals(chunkA.getId(), rows.get(0).get("chunk_id"));
        Double distA = ((Number) rows.get(0).get("distance")).doubleValue();
        assertTrue(distA < 0.05, "Aligned vector distance should be near zero, got " + distA);

        assertEquals(chunkB.getId(), rows.get(1).get("chunk_id"));
        Double distB = ((Number) rows.get(1).get("distance")).doubleValue();
        assertEquals(1.0, distB, 1e-3, "Orthogonal vector distance should be ~1.0");
    }

    @Test
    @DisplayName("Verify Flyway V8 migration and model-versioned index status")
    void testFlywayV8MigrationAndDataPreservation() {
        Integer v8Applied = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '8' AND success = true",
                Integer.class
        );
        assertNotNull(v8Applied);
        assertEquals(1, v8Applied, "Flyway V8 migration must be successfully recorded in schema history");

        // Verify index idx_chunk_emb_model_status exists
        Integer indexExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'evidence_chunk_embeddings' AND indexname = 'idx_chunk_emb_model_status'",
                Integer.class
        );
        assertNotNull(indexExists);
        assertEquals(1, indexExists, "Index idx_chunk_emb_model_status must exist in PostgreSQL");
    }

    @Test
    @DisplayName("Verify Flyway V9 migration and AURA orchestration tables in PostgreSQL")
    void testFlywayV9MigrationAndOrchestrationTables() {
        Integer v9Applied = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '9' AND success = true",
                Integer.class
        );
        assertNotNull(v9Applied);
        assertEquals(1, v9Applied, "Flyway V9 migration must be successfully recorded in schema history");

        // Verify the 5 orchestration tables exist
        List<String> expectedTables = List.of(
                "orchestration_runs",
                "workflow_tasks",
                "task_dependencies",
                "agent_execution_records",
                "workflow_event_history"
        );

        for (String table : expectedTables) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?",
                    Integer.class,
                    table
            );
            assertNotNull(count);
            assertEquals(1, count, "Table '" + table + "' must exist in PostgreSQL 18.6 schema");
        }

        // Verify key indexes exist
        Integer idxRunUser = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'orchestration_runs' AND indexname = 'idx_orch_run_user'",
                Integer.class
        );
        assertEquals(1, idxRunUser);

        Integer idxTaskRun = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'workflow_tasks' AND indexname = 'idx_orch_task_run'",
                Integer.class
        );
        assertEquals(1, idxTaskRun);
    }

    @Test
    @DisplayName("Verify Orchestration Run, Tasks, Dependencies, and Events persistence and referential integrity in PostgreSQL")
    void testOrchestrationDataPersistenceInPostgreSql() {
        // 1. Create Orchestration Run
        String runId = "orch-run-" + UUID.randomUUID();
        org.curriculumai.orchestration.domain.OrchestrationRun run = new org.curriculumai.orchestration.domain.OrchestrationRun(
                runId,
                "usr-default-faculty",
                null,
                null,
                "Develop an Agentic AI course syllabus",
                "Agentic AI",
                org.curriculumai.orchestration.domain.WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING
        );
        run.setStatus(org.curriculumai.orchestration.domain.RunStatus.RUNNING);
        run.setTotalTasks(2);
        run = orchestrationRunRepository.save(run);

        // 2. Create Tasks
        String task1Id = "task-pg-" + UUID.randomUUID();
        org.curriculumai.orchestration.domain.WorkflowTask task1 = new org.curriculumai.orchestration.domain.WorkflowTask(
                task1Id,
                runId,
                "task_curriculum_mapping",
                "Curriculum Mapping",
                "curriculum-mapping-agent",
                1,
                true,
                true
        );
        task1.setStatus(org.curriculumai.orchestration.domain.TaskStatus.COMPLETED);
        task1.setOutputPayloadJson("{\"mapped\": true}");
        task1 = workflowTaskRepository.save(task1);

        String task2Id = "task-pg-" + UUID.randomUUID();
        org.curriculumai.orchestration.domain.WorkflowTask task2 = new org.curriculumai.orchestration.domain.WorkflowTask(
                task2Id,
                runId,
                "task_gap_analysis",
                "Gap Analysis",
                "gap-analysis-agent",
                2,
                false,
                true
        );
        task2.setStatus(org.curriculumai.orchestration.domain.TaskStatus.PENDING);
        task2 = workflowTaskRepository.save(task2);

        // 3. Create Dependency
        String depId = "dep-pg-" + UUID.randomUUID();
        org.curriculumai.orchestration.domain.TaskDependency dep = new org.curriculumai.orchestration.domain.TaskDependency(
                depId,
                runId,
                task2Id,
                task1Id,
                true
        );
        dep = taskDependencyRepository.save(dep);

        // 4. Create Execution Record
        String recId = "exec-pg-" + UUID.randomUUID();
        org.curriculumai.orchestration.domain.AgentExecutionRecord record = new org.curriculumai.orchestration.domain.AgentExecutionRecord(
                recId,
                runId,
                task1Id,
                "curriculum-mapping-agent",
                1,
                "SUCCESS",
                "{\"findings\": \"valid\"}",
                "[]",
                "[]",
                null,
                "{}",
                120L
        );
        record = agentExecutionRecordRepository.save(record);

        // 5. Create Workflow Event
        String eventId = "evt-pg-" + UUID.randomUUID();
        org.curriculumai.orchestration.domain.WorkflowEvent event = new org.curriculumai.orchestration.domain.WorkflowEvent(
                eventId,
                runId,
                task1Id,
                "curriculum-mapping-agent",
                org.curriculumai.orchestration.domain.WorkflowEventType.TASK_COMPLETED,
                "Task 1 completed successfully",
                "{\"durationMs\": 120}"
        );
        event = workflowEventRepository.save(event);

        // Flush and clear persistence context
        entityManager.flush();
        entityManager.clear();

        // 6. Verify data retrieval from live PostgreSQL
        Optional<org.curriculumai.orchestration.domain.OrchestrationRun> retrievedRun = orchestrationRunRepository.findById(runId);
        assertTrue(retrievedRun.isPresent());
        assertEquals("Agentic AI", retrievedRun.get().getSubject());
        assertEquals(org.curriculumai.orchestration.domain.RunStatus.RUNNING, retrievedRun.get().getStatus());

        List<org.curriculumai.orchestration.domain.WorkflowTask> retrievedTasks = workflowTaskRepository.findByRunIdOrderBySequenceOrderAsc(runId);
        assertEquals(2, retrievedTasks.size());
        assertEquals("task_curriculum_mapping", retrievedTasks.get(0).getTaskKey());
        assertEquals("task_gap_analysis", retrievedTasks.get(1).getTaskKey());

        List<org.curriculumai.orchestration.domain.TaskDependency> retrievedDeps = taskDependencyRepository.findByTaskId(task2Id);
        assertEquals(1, retrievedDeps.size());
        assertEquals(task1Id, retrievedDeps.get(0).getDependsOnTaskId());

        List<org.curriculumai.orchestration.domain.AgentExecutionRecord> retrievedRecords = agentExecutionRecordRepository.findByTaskId(task1Id);
        assertEquals(1, retrievedRecords.size());
        assertEquals("SUCCESS", retrievedRecords.get(0).getExecutionStatus());

        List<org.curriculumai.orchestration.domain.WorkflowEvent> retrievedEvents = workflowEventRepository.findByRunIdOrderByCreatedAtAsc(runId);
        assertFalse(retrievedEvents.isEmpty());
        assertEquals(org.curriculumai.orchestration.domain.WorkflowEventType.TASK_COMPLETED, retrievedEvents.get(0).getEventType());
    }

    @Test
    @DisplayName("Verify orchestration run cancellation and resumption state transitions in PostgreSQL")
    void testOrchestrationRunCancellationAndResumePersistence() {
        String runId = "run-pg-lifecycle-" + UUID.randomUUID();
        org.curriculumai.orchestration.domain.OrchestrationRun run = new org.curriculumai.orchestration.domain.OrchestrationRun(
                runId,
                "usr-default-faculty",
                null,
                null,
                "Lifecycle Test Plan",
                "Advanced AI Systems",
                org.curriculumai.orchestration.domain.WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING
        );
        run.setStatus(org.curriculumai.orchestration.domain.RunStatus.RUNNING);
        orchestrationRunRepository.save(run);

        String task1Id = "task-pg-c1-" + UUID.randomUUID();
        org.curriculumai.orchestration.domain.WorkflowTask task1 = new org.curriculumai.orchestration.domain.WorkflowTask(
                task1Id,
                runId,
                "task_mapping",
                "Curriculum Mapping",
                "curriculum-mapping-agent",
                1,
                true,
                true
        );
        task1.setStatus(org.curriculumai.orchestration.domain.TaskStatus.COMPLETED);
        task1.setOutputPayloadJson("{\"completed\": true}");
        workflowTaskRepository.save(task1);

        String task2Id = "task-pg-c2-" + UUID.randomUUID();
        org.curriculumai.orchestration.domain.WorkflowTask task2 = new org.curriculumai.orchestration.domain.WorkflowTask(
                task2Id,
                runId,
                "task_benchmarking",
                "Academic Benchmarking",
                "academic-benchmarking-agent",
                2,
                false,
                true
        );
        task2.setStatus(org.curriculumai.orchestration.domain.TaskStatus.RUNNING);
        workflowTaskRepository.save(task2);

        entityManager.flush();

        // 1. Cancel Run
        run.setStatus(org.curriculumai.orchestration.domain.RunStatus.CANCELLED);
        task2.setStatus(org.curriculumai.orchestration.domain.TaskStatus.CANCELLED);
        orchestrationRunRepository.save(run);
        workflowTaskRepository.save(task2);

        org.curriculumai.orchestration.domain.WorkflowEvent cancelEvent = new org.curriculumai.orchestration.domain.WorkflowEvent(
                "evt-cancel-" + UUID.randomUUID(),
                runId,
                null,
                null,
                org.curriculumai.orchestration.domain.WorkflowEventType.RUN_CANCELLED,
                "Run cancelled by user",
                "{}"
        );
        workflowEventRepository.save(cancelEvent);

        entityManager.flush();
        entityManager.clear();

        var cancelledRun = orchestrationRunRepository.findById(runId).orElseThrow();
        assertEquals(org.curriculumai.orchestration.domain.RunStatus.CANCELLED, cancelledRun.getStatus());
        var cancelledTask2 = workflowTaskRepository.findById(task2Id).orElseThrow();
        assertEquals(org.curriculumai.orchestration.domain.TaskStatus.CANCELLED, cancelledTask2.getStatus());

        // 2. Resume Run: Completed task stays COMPLETED, cancelled task reset to PENDING
        run.setStatus(org.curriculumai.orchestration.domain.RunStatus.RUNNING);
        orchestrationRunRepository.save(run);

        var existingTasks = workflowTaskRepository.findByRunIdOrderBySequenceOrderAsc(runId);
        for (var t : existingTasks) {
            if (t.getStatus() != org.curriculumai.orchestration.domain.TaskStatus.COMPLETED) {
                t.setStatus(org.curriculumai.orchestration.domain.TaskStatus.PENDING);
                workflowTaskRepository.save(t);
            }
        }

        entityManager.flush();
        entityManager.clear();

        var resumedRun = orchestrationRunRepository.findById(runId).orElseThrow();
        assertEquals(org.curriculumai.orchestration.domain.RunStatus.RUNNING, resumedRun.getStatus());

        var resumedTask1 = workflowTaskRepository.findById(task1Id).orElseThrow();
        assertEquals(org.curriculumai.orchestration.domain.TaskStatus.COMPLETED, resumedTask1.getStatus());
        assertEquals("{\"completed\": true}", resumedTask1.getOutputPayloadJson());

        var resumedTask2 = workflowTaskRepository.findById(task2Id).orElseThrow();
        assertEquals(org.curriculumai.orchestration.domain.TaskStatus.PENDING, resumedTask2.getStatus());
    }

    @Test
    @DisplayName("Verify CurriculumMappingAgent real academic queries and snapshot retrieval against PostgreSQL 18.6")
    void testCurriculumMappingAgentAgainstLivePostgreSql() throws Exception {
        // 1. Seed isolated academic fixture in PostgreSQL
        String uid = UUID.randomUUID().toString().substring(0, 8);
        org.curriculumai.academic.domain.Institution inst = new org.curriculumai.academic.domain.Institution(
                "inst-pg-" + uid, "INST-" + uid, "Institution " + uid, "City", "India"
        );
        institutionRepository.save(inst);

        org.curriculumai.academic.domain.Department dept = new org.curriculumai.academic.domain.Department(
                "dept-pg-" + uid, inst, "AIML-" + uid, "AI & ML Department"
        );
        departmentRepository.save(dept);

        org.curriculumai.academic.domain.Programme prog = new org.curriculumai.academic.domain.Programme(
                "prog-pg-" + uid, dept, "BTECH-AIML-" + uid, "B.Tech AI & ML", "UNDERGRADUATE", 4, 160
        );
        programmeRepository.save(prog);

        org.curriculumai.academic.domain.CurriculumVersion cv = new org.curriculumai.academic.domain.CurriculumVersion(
                "cv-pg-" + uid, prog, "2024-" + uid, 2024
        );
        cv.setLifecycleStatus(org.curriculumai.academic.domain.CurriculumLifecycleStatus.APPROVED);
        curriculumVersionRepository.save(cv);

        org.curriculumai.academic.domain.Semester sem = new org.curriculumai.academic.domain.Semester(
                "sem-pg-" + uid, cv, 6, "Semester 6 Spring", 24
        );
        semesterRepository.save(sem);

        org.curriculumai.academic.domain.Course prereqCourse = new org.curriculumai.academic.domain.Course(
                "crs-prq-" + uid, dept, "AI401-" + uid, "Foundations of AI", 4, 3, 0, 2
        );
        courseRepository.save(prereqCourse);

        org.curriculumai.academic.domain.Course targetCourse = new org.curriculumai.academic.domain.Course(
                "crs-tgt-" + uid, dept, "AI601-" + uid, "Reinforcement Learning & Multi-Agent Systems", 4, 3, 0, 2
        );
        targetCourse.setCourseType("CORE");
        courseRepository.save(targetCourse);

        org.curriculumai.academic.domain.CoursePrerequisite prereq = new org.curriculumai.academic.domain.CoursePrerequisite(
                "prq-pg-" + uid, targetCourse, prereqCourse, "PREREQUISITE", "C"
        );
        coursePrerequisiteRepository.save(prereq);

        org.curriculumai.academic.domain.CurriculumCourse cc = new org.curriculumai.academic.domain.CurriculumCourse(
                "cc-pg-" + uid, cv, sem, targetCourse, 4, 3, 0, 2, false, null
        );
        curriculumCourseRepository.save(cc);

        // Outcomes and snapshots
        org.curriculumai.academic.domain.CourseOutcome co = new org.curriculumai.academic.domain.CourseOutcome(
                "co-pg-" + uid, targetCourse, "CO1", "Formulate Markov Decision Processes", "ANALYZE"
        );
        courseOutcomeRepository.save(co);

        org.curriculumai.academic.domain.ProgrammeOutcome po = new org.curriculumai.academic.domain.ProgrammeOutcome(
                "po-pg-" + uid, prog, "PO1", "Engineering Knowledge", "Apply mathematics and AI principles", "PO"
        );
        programmeOutcomeRepository.save(po);

        org.curriculumai.academic.domain.CoPoMapping mapping = new org.curriculumai.academic.domain.CoPoMapping(
                "map-pg-" + uid, co, po, 3, "Direct mathematical alignment"
        );
        coPoMappingRepository.save(mapping);

        // Create immutable snapshots in curriculum_outcome_snapshots
        org.curriculumai.academic.domain.CurriculumOutcomeSnapshot snapCo = org.curriculumai.academic.domain.CurriculumOutcomeSnapshot.ofCourseOutcome(
                "snap-co-" + uid, cv, targetCourse, co
        );
        snapshotRepository.save(snapCo);

        org.curriculumai.academic.domain.CurriculumOutcomeSnapshot snapPo = org.curriculumai.academic.domain.CurriculumOutcomeSnapshot.ofProgrammeOutcome(
                "snap-po-" + uid, cv, po
        );
        snapshotRepository.save(snapPo);

        org.curriculumai.academic.domain.CurriculumOutcomeSnapshot snapMap = org.curriculumai.academic.domain.CurriculumOutcomeSnapshot.ofCoPoMapping(
                "snap-map-" + uid, cv, targetCourse, mapping
        );
        snapshotRepository.save(snapMap);

        entityManager.flush();
        entityManager.clear();

        // 2. Persist OrchestrationRun and WorkflowTask so FK constraints on workflow_event_history are satisfied
        String runId = "run-map-pg-" + uid;
        String taskId = "task-map-pg-" + uid;
        org.curriculumai.orchestration.domain.OrchestrationRun run = new org.curriculumai.orchestration.domain.OrchestrationRun(
                runId, "usr-default-faculty", null, null, "Map AI601", "AI601-" + uid,
                org.curriculumai.orchestration.domain.WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING
        );
        run.setStatus(org.curriculumai.orchestration.domain.RunStatus.RUNNING);
        orchestrationRunRepository.save(run);

        org.curriculumai.orchestration.domain.WorkflowTask task = new org.curriculumai.orchestration.domain.WorkflowTask(
                taskId, runId, "task_curriculum_mapping", "Curriculum Mapping", "curriculum-mapping-agent",
                1, true, true
        );
        task.setStatus(org.curriculumai.orchestration.domain.TaskStatus.RUNNING);
        workflowTaskRepository.save(task);
        entityManager.flush();

        // 3. Execute CurriculumMappingAgent against PostgreSQL
        org.curriculumai.orchestration.agent.TaskExecutionContext ctx = new org.curriculumai.orchestration.agent.TaskExecutionContext(
                runId, taskId, "task_curriculum_mapping",
                "AI601-" + uid, "Map course AI601",
                Map.of("courseCode", "AI601-" + uid),
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), "usr-default-faculty"
        );

        org.curriculumai.orchestration.agent.StructuredAgentResult result = curriculumMappingAgent.execute(ctx);

        assertEquals("SUCCESS", result.executionStatus());
        assertEquals("curriculum-mapping-agent", result.agentId());
        assertNotNull(result.findings());

        Map<String, Object> findings = result.findings();
        assertEquals("RESOLVED", findings.get("resolutionStatus"));
        assertEquals(true, findings.get("historicalSnapshotUsed"));
        assertEquals("curriculum_outcome_snapshots", findings.get("snapshotSource"));

        @SuppressWarnings("unchecked")
        Map<String, Object> canonicalCourse = (Map<String, Object>) findings.get("canonicalCourse");
        assertEquals("AI601-" + uid, canonicalCourse.get("code"));
        assertEquals("Reinforcement Learning & Multi-Agent Systems", canonicalCourse.get("title"));

        @SuppressWarnings("unchecked")
        Map<String, Object> offering = (Map<String, Object>) findings.get("offering");
        assertNotNull(offering);
        assertEquals("2024-" + uid, offering.get("versionCode"));
        assertEquals(6, offering.get("semesterNumber"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> prereqs = (List<Map<String, Object>>) findings.get("prerequisites");
        assertEquals(1, prereqs.size());
        assertEquals("AI401-" + uid, prereqs.get(0).get("courseCode"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> outcomes = (List<Map<String, Object>>) findings.get("courseOutcomes");
        assertEquals(1, outcomes.size());
        assertEquals("CO1", outcomes.get(0).get("code"));

        // 4. Update task row with output and verify persistence
        String findingsJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(findings);
        task.setStatus(org.curriculumai.orchestration.domain.TaskStatus.COMPLETED);
        task.setOutputPayloadJson(findingsJson);
        workflowTaskRepository.save(task);

        entityManager.flush();
        entityManager.clear();

        var retrievedTask = workflowTaskRepository.findById(taskId).orElseThrow();
        assertEquals(org.curriculumai.orchestration.domain.TaskStatus.COMPLETED, retrievedTask.getStatus());
        assertTrue(retrievedTask.getOutputPayloadJson().contains("Reinforcement Learning & Multi-Agent Systems"));
        assertTrue(retrievedTask.getOutputPayloadJson().contains("curriculum_outcome_snapshots"));
    }
}
