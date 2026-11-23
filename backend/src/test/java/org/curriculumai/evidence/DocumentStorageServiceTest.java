package org.curriculumai.evidence;

import org.curriculumai.evidence.service.DocumentStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DocumentStorageServiceTest {

    @TempDir
    Path tempStorageDir;

    private DocumentStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new DocumentStorageService(tempStorageDir.toString());
        storageService.init();
    }

    @Test
    @DisplayName("Stores and reads document bytes correctly")
    void testStoreAndReadDocument() throws IOException {
        String testContent = "Curriculum Evidence: ACM/IEEE Computer Science Guidelines 2023.";
        byte[] bytes = testContent.getBytes(StandardCharsets.UTF_8);

        String ref = storageService.storeDocument(
                new ByteArrayInputStream(bytes),
                "acm_cs_2023.txt",
                "sha256_mock_hash_12345"
        );

        assertNotNull(ref);
        assertFalse(ref.startsWith("/"));
        assertTrue(ref.contains(".txt"));

        byte[] readBytes = storageService.readDocument(ref);
        assertArrayEquals(bytes, readBytes);
        assertEquals(testContent, new String(readBytes, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Rejects path traversal attempts during resolution")
    void testRejectsPathTraversal() {
        assertThrows(SecurityException.class, () ->
                storageService.resolvePath("../../secrets/passwords.txt"));
        assertThrows(SecurityException.class, () ->
                storageService.resolvePath("..\\..\\windows\\system32"));
    }

    @Test
    @DisplayName("Persists files across simulated service restarts")
    void testPersistenceAcrossRestarts() throws IOException {
        byte[] bytes = "Persistence check across application reboots".getBytes(StandardCharsets.UTF_8);
        String ref = storageService.storeDocument(
                new ByteArrayInputStream(bytes),
                "restart_test.md",
                "restart_hash_abc"
        );

        // Instantiate new service pointing to same directory
        DocumentStorageService restartedService = new DocumentStorageService(tempStorageDir.toString());
        restartedService.init();

        byte[] reRead = restartedService.readDocument(ref);
        assertArrayEquals(bytes, reRead);
    }
}

