package org.curriculumai.evidence.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class DocumentStorageService {

    private static final Logger log = LoggerFactory.getLogger(DocumentStorageService.class);

    private final Path rootStoragePath;

    public DocumentStorageService(@Value("${app.storage.evidence-dir:data/evidence}") String storageDir) {
        this.rootStoragePath = Paths.get(storageDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootStoragePath);
            log.info("Evidence document storage initialized at: {}", rootStoragePath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize evidence storage directory: " + rootStoragePath, e);
        }
    }

    /**
     * Stores raw document bytes in the persistent storage directory.
     * Generates a partitioned subpath based on year/month and SHA hash.
     * Prevents path traversal and protects against overwrites.
     */
    public String storeDocument(InputStream inputStream, String originalFilename, String contentHash) throws IOException {
        String safeExt = extractExtension(originalFilename);
        LocalDate now = LocalDate.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy/MM"));
        
        Path dir = rootStoragePath.resolve(yearMonth).normalize();
        if (!dir.startsWith(rootStoragePath)) {
            throw new SecurityException("Path traversal attempt detected in storage path creation.");
        }
        Files.createDirectories(dir);

        String filename = contentHash.substring(0, Math.min(16, contentHash.length())) + "_" + UUID.randomUUID() + safeExt;
        Path targetFile = dir.resolve(filename).normalize();
        
        if (!targetFile.startsWith(rootStoragePath)) {
            throw new SecurityException("Path traversal attempt detected in target file path.");
        }

        Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path as storage reference
        return rootStoragePath.relativize(targetFile).toString().replace('\\', '/');
    }

    /**
     * Resolves storage reference to absolute Path with strict traversal check.
     */
    public Path resolvePath(String storageReference) {
        if (storageReference == null || storageReference.isBlank()) {
            throw new IllegalArgumentException("Storage reference cannot be empty.");
        }
        Path resolved = rootStoragePath.resolve(storageReference).normalize();
        if (!resolved.startsWith(rootStoragePath)) {
            throw new SecurityException("Access denied: Storage reference attempts path traversal outside root storage directory.");
        }
        return resolved;
    }

    public byte[] readDocument(String storageReference) throws IOException {
        Path path = resolvePath(storageReference);
        if (!Files.exists(path)) {
            throw new IOException("Stored document not found at: " + storageReference);
        }
        return Files.readAllBytes(path);
    }

    public InputStream openStream(String storageReference) throws IOException {
        Path path = resolvePath(storageReference);
        if (!Files.exists(path)) {
            throw new IOException("Stored document not found at: " + storageReference);
        }
        return Files.newInputStream(path);
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".bin";
        }
        String ext = filename.substring(filename.lastIndexOf('.')).toLowerCase();
        // Sanitize extension
        if (!ext.matches("^\\.[a-z0-9]{1,10}$")) {
            return ".bin";
        }
        return ext;
    }

    public Path getRootStoragePath() {
        return rootStoragePath;
    }
}

