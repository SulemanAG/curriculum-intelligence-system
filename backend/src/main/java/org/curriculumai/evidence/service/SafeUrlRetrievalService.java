package org.curriculumai.evidence.service;

import org.curriculumai.evidence.domain.RetrievalOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class SafeUrlRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(SafeUrlRetrievalService.class);
    public static final int MAX_RESPONSE_BYTES = 25 * 1024 * 1024; // 25 MB
    public static final int MAX_REDIRECTS = 5;

    private final SafeUrlValidator safeUrlValidator;

    public SafeUrlRetrievalService(SafeUrlValidator safeUrlValidator) {
        this.safeUrlValidator = safeUrlValidator;
    }

    public record RetrievalResult(
            RetrievalOutcome outcome,
            Integer httpStatus,
            String finalUrl,
            String contentType,
            String contentHash,
            byte[] contentBytes,
            String etag,
            String lastModified,
            String failureReason,
            long elapsedMillis
    ) {}

    /**
     * Safely executes an HTTP GET request to a permitted URL with redirect validation,
     * SSRF defense, timeout controls, and response size limits.
     */
    public RetrievalResult retrieveUrl(String targetUrl) {
        long startTime = System.currentTimeMillis();
        String currentUrl = targetUrl;
        int redirectCount = 0;

        while (redirectCount <= MAX_REDIRECTS) {
            URI validatedUri;
            try {
                validatedUri = safeUrlValidator.validateSafeUrl(currentUrl);
            } catch (Exception e) {
                log.warn("URL validation or SSRF check failed for '{}': {}", currentUrl, e.getMessage());
                return new RetrievalResult(
                        RetrievalOutcome.SSRF_BLOCKED,
                        null,
                        currentUrl,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "SSRF or URL validation error: " + e.getMessage(),
                        System.currentTimeMillis() - startTime
                );
            }

            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) validatedUri.toURL().openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(15000);
                connection.setInstanceFollowRedirects(false); // Manually validate redirect destinations
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "CurriculumAI-Evidence-Bot/1.0 (+http://curriculum.ai/research-agent)");
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document,text/plain,*/*;q=0.8");

                connection.connect();
                int statusCode = connection.getResponseCode();

                // Handle Redirects
                if (statusCode >= 300 && statusCode < 400) {
                    String location = connection.getHeaderField("Location");
                    if (location == null || location.isBlank()) {
                        return new RetrievalResult(
                                RetrievalOutcome.FAILED,
                                statusCode,
                                currentUrl,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "Redirect response missing Location header.",
                                System.currentTimeMillis() - startTime
                        );
                    }

                    URI redirectedUri = validatedUri.resolve(location);
                    currentUrl = redirectedUri.toString();
                    redirectCount++;
                    log.info("Following redirect #{} to: {}", redirectCount, currentUrl);
                    continue;
                }

                String contentType = connection.getContentType();
                String etag = connection.getHeaderField("ETag");
                String lastModified = connection.getHeaderField("Last-Modified");

                // Handle HTTP errors
                if (statusCode == 401 || statusCode == 403) {
                    return new RetrievalResult(
                            RetrievalOutcome.ACCESS_DENIED,
                            statusCode,
                            currentUrl,
                            contentType,
                            null,
                            null,
                            etag,
                            lastModified,
                            "Access denied (HTTP " + statusCode + ")",
                            System.currentTimeMillis() - startTime
                    );
                }

                if (statusCode >= 400) {
                    return new RetrievalResult(
                            RetrievalOutcome.FAILED,
                            statusCode,
                            currentUrl,
                            contentType,
                            null,
                            null,
                            etag,
                            lastModified,
                            "HTTP " + statusCode + " error received from remote source.",
                            System.currentTimeMillis() - startTime
                    );
                }

                // Read response stream with size limits
                try (InputStream in = connection.getInputStream();
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    int totalRead = 0;
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");

                    while ((bytesRead = in.read(buffer)) != -1) {
                        totalRead += bytesRead;
                        if (totalRead > MAX_RESPONSE_BYTES) {
                            return new RetrievalResult(
                                    RetrievalOutcome.FAILED,
                                    statusCode,
                                    currentUrl,
                                    contentType,
                                    null,
                                    null,
                                    etag,
                                    lastModified,
                                    "Response size exceeded maximum allowed limit of " + (MAX_RESPONSE_BYTES / (1024 * 1024)) + " MB",
                                    System.currentTimeMillis() - startTime
                            );
                        }
                        out.write(buffer, 0, bytesRead);
                        digest.update(buffer, 0, bytesRead);
                    }

                    byte[] contentBytes = out.toByteArray();
                    String contentHash = HexFormat.of().formatHex(digest.digest());

                    return new RetrievalResult(
                            RetrievalOutcome.RETRIEVED,
                            statusCode,
                            currentUrl,
                            contentType,
                            contentHash,
                            contentBytes,
                            etag,
                            lastModified,
                            null,
                            System.currentTimeMillis() - startTime
                    );
                }

            } catch (SocketTimeoutException e) {
                return new RetrievalResult(
                        RetrievalOutcome.TIMEOUT,
                        null,
                        currentUrl,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "Connection or read timed out: " + e.getMessage(),
                        System.currentTimeMillis() - startTime
                );
            } catch (Exception e) {
                return new RetrievalResult(
                        RetrievalOutcome.FAILED,
                        null,
                        currentUrl,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "Retrieval failed: " + e.getMessage(),
                        System.currentTimeMillis() - startTime
                );
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        return new RetrievalResult(
                RetrievalOutcome.FAILED,
                null,
                currentUrl,
                null,
                null,
                null,
                null,
                null,
                "Exceeded maximum redirect count (" + MAX_REDIRECTS + ")",
                System.currentTimeMillis() - startTime
        );
    }
}
