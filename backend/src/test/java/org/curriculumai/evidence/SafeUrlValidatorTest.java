package org.curriculumai.evidence;

import org.curriculumai.evidence.service.SafeUrlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class SafeUrlValidatorTest {

    private SafeUrlValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SafeUrlValidator();
    }

    @Test
    @DisplayName("Allows valid public HTTP/HTTPS URLs")
    void testValidPublicUrls() {
        URI uri = validator.validateSafeUrl("https://example.com/curriculum/spec");
        assertNotNull(uri);
        assertEquals("example.com", uri.getHost());
        assertEquals("https", uri.getScheme());
    }

    @Test
    @DisplayName("Rejects null or blank URLs")
    void testRejectsNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateSafeUrl(null));
        assertThrows(IllegalArgumentException.class, () -> validator.validateSafeUrl("   "));
    }

    @Test
    @DisplayName("Rejects non-HTTP protocols (file, ftp, gopher)")
    void testRejectsNonHttpSchemes() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validateSafeUrl("file:///etc/passwd"));
        assertTrue(ex.getMessage().contains("Only HTTP and HTTPS URLs are permitted"));

        assertThrows(IllegalArgumentException.class,
                () -> validator.validateSafeUrl("ftp://ftp.example.com/file.txt"));
    }

    @Test
    @DisplayName("Rejects localhost and loopback hostnames")
    void testRejectsLocalhost() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> validator.validateSafeUrl("http://localhost:8080/admin"));
        assertTrue(ex1.getMessage().contains("prohibited"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> validator.validateSafeUrl("http://127.0.0.1:8080/api"));
        assertTrue(ex2.getMessage().contains("prohibited"));
    }

    @Test
    @DisplayName("Rejects cloud metadata endpoints (AWS/GCP/Azure)")
    void testRejectsCloudMetadata() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateSafeUrl("http://169.254.169.254/latest/meta-data/"));
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateSafeUrl("http://metadata.google.internal/computeMetadata/v1/"));
    }

    @Test
    @DisplayName("Detects prohibited IP address ranges")
    void testProhibitedIpRanges() throws Exception {
        // Loopback
        assertTrue(validator.isProhibitedIp(InetAddress.getByName("127.0.0.1")));
        assertTrue(validator.isProhibitedIp(InetAddress.getByName("127.0.1.5")));

        // Private 10.0.0.0/8
        assertTrue(validator.isProhibitedIp(InetAddress.getByName("10.0.0.1")));
        assertTrue(validator.isProhibitedIp(InetAddress.getByName("10.255.255.254")));

        // Private 172.16.0.0/12
        assertTrue(validator.isProhibitedIp(InetAddress.getByName("172.16.0.1")));
        assertTrue(validator.isProhibitedIp(InetAddress.getByName("172.31.255.255")));

        // Private 192.168.0.0/16
        assertTrue(validator.isProhibitedIp(InetAddress.getByName("192.168.1.1")));
        assertTrue(validator.isProhibitedIp(InetAddress.getByName("192.168.100.50")));

        // Link-Local / Metadata 169.254.0.0/16
        assertTrue(validator.isProhibitedIp(InetAddress.getByName("169.254.169.254")));

        // Public IP (Google DNS 8.8.8.8) is not prohibited
        assertFalse(validator.isProhibitedIp(InetAddress.getByName("8.8.8.8")));
    }
}

