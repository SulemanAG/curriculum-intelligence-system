package org.curriculumai.orchestration.research;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.curriculumai.evidence.domain.SourceType;
import org.curriculumai.evidence.dto.RegisterSourceRequest;
import org.curriculumai.evidence.dto.ResearchSourceDto;
import org.curriculumai.evidence.dto.SourceVersionDto;
import org.curriculumai.evidence.service.EvidenceIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
public class ExternalResearchService {

    private static final Logger log = LoggerFactory.getLogger(ExternalResearchService.class);

    private final ResearchProperties properties;
    private final EvidenceIngestionService ingestionService;
    private final ObjectMapper objectMapper;

    public record DiscoveredSource(
            String title,
            String url,
            String domain,
            String snippet
    ) {}

    public record ResearchResult(
            String query,
            String category,
            String synthesis,
            List<DiscoveredSource> sources,
            List<String> evidenceChunkIds,
            List<String> registeredSourceIds,
            boolean successful,
            String failureReason
    ) {}

    public ExternalResearchService(ResearchProperties properties,
                                   EvidenceIngestionService ingestionService,
                                   ObjectMapper objectMapper) {
        this.properties = properties;
        this.ingestionService = ingestionService;
        this.objectMapper = objectMapper;
    }

    public ResearchResult conductResearch(String query, String category, SourceType sourceType, String userId) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Research query must not be null or blank.");
        }

        log.info("Conducting genuine external research for query='{}', category='{}', sourceType='{}'", query, category, sourceType);

        String synthesis = null;
        List<DiscoveredSource> discoveredSources = new ArrayList<>();
        String failureReason = null;

        if (properties.getGeminiApiKey() != null && !properties.getGeminiApiKey().isBlank()) {
            for (int attempt = 1; attempt <= 2; attempt++) {
                try {
                    GroundedSearchResponse response = executeGeminiSearchGrounding(query);
                    if (response != null && response.text() != null && !response.text().isBlank()) {
                        synthesis = response.text();
                        discoveredSources.addAll(response.sources());
                        log.info("Gemini search grounding returned {} characters and {} grounding web sources for query '{}'",
                                synthesis.length(), discoveredSources.size(), query);
                        break;
                    } else {
                        failureReason = "Gemini search grounding returned empty response for query: " + query;
                    }
                } catch (Exception e) {
                    log.warn("Gemini search grounding attempt {} failed for query '{}': {}", attempt, query, e.getMessage());
                    failureReason = "External search grounding failed: " + e.getMessage();
                }
            }
        } else {
            failureReason = "External research provider API key is unconfigured. Set app.research.gemini-api-key.";
            log.warn(failureReason);
        }

        // If external retrieval failed or returned empty, do not fabricate fake syllabi or reports
        if (synthesis == null || synthesis.isBlank()) {
            log.warn("No external research evidence retrieved for query '{}'. Reporting honest failure.", query);
            return new ResearchResult(
                    query,
                    category,
                    null,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    false,
                    failureReason
            );
        }

        // Ingest genuine external evidence into repository
        List<String> registeredSourceIds = new ArrayList<>();
        List<String> evidenceChunkIds = new ArrayList<>();

        try {
            // 1. Primary document registration
            String primaryUrl = !discoveredSources.isEmpty()
                    ? discoveredSources.get(0).url()
                    : "https://generativelanguage.googleapis.com/search-grounding/" + UUID.randomUUID();

            String primaryPublisher = !discoveredSources.isEmpty()
                    ? discoveredSources.get(0).domain()
                    : "Google Search Grounding Engine";

            String primaryTitle = category + ": " + query;

            RegisterSourceRequest regReq = new RegisterSourceRequest(
                    primaryUrl,
                    sourceType,
                    primaryPublisher,
                    primaryTitle,
                    category,
                    null,
                    null,
                    null
            );

            ResearchSourceDto registeredSource = ingestionService.registerSource(regReq, userId);
            registeredSourceIds.add(registeredSource.id());

            // Attempt safe live URL retrieval to record retrieval attempt in source_retrievals
            if (primaryUrl.startsWith("http://") || primaryUrl.startsWith("https://")) {
                try {
                    ingestionService.retrieveSourceUrl(registeredSource.id(), userId);
                } catch (Exception e) {
                    log.debug("Notice: Primary source URL retrieval recorded/skipped: {}", e.getMessage());
                }
            }

            // Ingest text into SourceVersion and EvidenceChunks with embeddings
            String sanitizedFilename = "research_" + category.toLowerCase().replaceAll("[^a-z0-9]", "_") + ".txt";
            SourceVersionDto versionDto = ingestionService.ingestDirectText(
                    registeredSource.id(),
                    synthesis,
                    sanitizedFilename,
                    LocalDate.now().toString(),
                    userId
            );

            List<String> chunkIds = ingestionService.getChunkIdsForVersion(versionDto.id());
            evidenceChunkIds.addAll(chunkIds);
            log.info("Successfully ingested external evidence: sourceId='{}', chunksSaved={}",
                    registeredSource.id(), chunkIds.size());

            // 2. Register discovered web sources for citation tracking and retrieve top citations
            for (int i = 1; i < discoveredSources.size(); i++) {
                DiscoveredSource ds = discoveredSources.get(i);
                try {
                    RegisterSourceRequest citationReq = new RegisterSourceRequest(
                            ds.url(),
                            sourceType,
                            ds.domain(),
                            ds.title(),
                            category + "_CITATION",
                            null,
                            null,
                            null
                    );
                    ResearchSourceDto citSource = ingestionService.registerSource(citationReq, userId);
                    registeredSourceIds.add(citSource.id());

                    // Safely attempt retrieval for top 2 citations to verify source provenance in repository
                    if (i <= 2 && (ds.url().startsWith("http://") || ds.url().startsWith("https://"))) {
                        try {
                            ingestionService.retrieveSourceUrl(citSource.id(), userId);
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Exception e) {
                    log.debug("Notice: Citation source registration skipped for URL {}: {}", ds.url(), e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Failed to persist external research evidence into repository: {}", e.getMessage(), e);
            failureReason = "Failed to ingest retrieved evidence: " + e.getMessage();
        }

        return new ResearchResult(
                query,
                category,
                synthesis,
                discoveredSources,
                evidenceChunkIds,
                registeredSourceIds,
                true,
                null
        );
    }

    private record GroundedSearchResponse(String text, List<DiscoveredSource> sources) {}

    private GroundedSearchResponse executeGeminiSearchGrounding(String promptQuery) throws Exception {
        String urlStr = "https://generativelanguage.googleapis.com/v1beta/models/"
                + properties.getGeminiModel() + ":generateContent?key=" + properties.getGeminiApiKey();

        URI uri = URI.create(urlStr);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(properties.getTimeoutSeconds() * 1000);
        conn.setReadTimeout(properties.getTimeoutSeconds() * 1000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String systemInstruction = "You are an expert academic curriculum researcher and labor-market skills analyst. " +
                "Execute real-world search grounding to find factual, current information. " +
                "Extract specific course syllabi, institution names, official course codes, industry frameworks, " +
                "and verifiable web URLs. Do not fabricate data. If information for a specific topic is unavailable, state so.";

        Map<String, Object> requestMap = new LinkedHashMap<>();
        requestMap.put("contents", List.of(
                Map.of("parts", List.of(
                        Map.of("text", systemInstruction + "\n\nSearch and Research Target: " + promptQuery)
                ))
        ));
        requestMap.put("tools", List.of(
                Map.of("google_search", Collections.emptyMap())
        ));

        String jsonPayload = objectMapper.writeValueAsString(requestMap);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int statusCode = conn.getResponseCode();
        if (statusCode != 200) {
            String errorMsg = "";
            try (InputStream es = conn.getErrorStream()) {
                if (es != null) {
                    errorMsg = new String(es.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (Exception ignored) {}
            throw new RuntimeException("Gemini Search Grounding HTTP " + statusCode + ": " + errorMsg);
        }

        byte[] respBytes;
        try (InputStream is = conn.getInputStream()) {
            respBytes = is.readAllBytes();
        }

        JsonNode root = objectMapper.readTree(respBytes);
        JsonNode candidate = root.path("candidates").path(0);
        String text = candidate.path("content").path("parts").path(0).path("text").asText("");

        List<DiscoveredSource> sources = new ArrayList<>();
        JsonNode groundingMetadata = candidate.path("groundingMetadata");
        JsonNode chunks = groundingMetadata.path("groundingChunks");
        if (chunks.isArray()) {
            for (JsonNode chunk : chunks) {
                JsonNode web = chunk.path("web");
                if (!web.isMissingNode()) {
                    String uriVal = web.path("uri").asText("");
                    String titleVal = web.path("title").asText("");
                    if (!uriVal.isBlank()) {
                        String domain = extractDomain(uriVal);
                        sources.add(new DiscoveredSource(titleVal, uriVal, domain, titleVal));
                    }
                }
            }
        }

        return new GroundedSearchResponse(text, sources);
    }

    private String extractDomain(String url) {
        try {
            URI u = URI.create(url);
            String host = u.getHost();
            return host != null ? host : "web";
        } catch (Exception e) {
            return "web";
        }
    }
}
