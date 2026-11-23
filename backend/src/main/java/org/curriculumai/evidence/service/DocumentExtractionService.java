package org.curriculumai.evidence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.*;
import org.curriculumai.evidence.domain.ExtractionStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DocumentExtractionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentExtractionService.class);
    private static final String EXTRACTOR_NAME = "CurriculumAI-MultiFormat-Extractor";
    private static final String EXTRACTOR_VERSION = "2.3.1";
    private static final int MIN_CHUNK_CHARS = 40;
    private static final int MAX_CHUNK_CHARS = 1500;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public record ExtractedChunk(
            int sequenceNumber,
            String textContent,
            Integer pageNumber,
            String sectionHeading,
            Integer charOffsetStart,
            Integer charOffsetEnd
    ) {}

    public record ExtractionResult(
            ExtractionStatus status,
            String extractorName,
            String extractorVersion,
            String documentTitle,
            String detectedLanguage,
            String structuredMetadataJson,
            Integer totalPages,
            Integer totalParagraphs,
            Integer totalTables,
            Integer totalCharacters,
            String errorDetails,
            List<ExtractedChunk> chunks
    ) {}

    /**
     * Extracts structured text passages from raw file bytes according to MIME type or filename.
     */
    public ExtractionResult extract(byte[] contentBytes, String mimeType, String filename) {
        if (contentBytes == null || contentBytes.length == 0) {
            return new ExtractionResult(
                    ExtractionStatus.FAILED,
                    EXTRACTOR_NAME,
                    EXTRACTOR_VERSION,
                    null,
                    null,
                    null,
                    0, 0, 0, 0,
                    "Empty document content provided.",
                    Collections.emptyList()
            );
        }

        String normalizedMime = normalizeMimeType(mimeType, filename);

        try {
            return switch (normalizedMime) {
                case "application/pdf" -> extractPdf(contentBytes);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> extractDocx(contentBytes);
                case "text/html", "application/xhtml+xml" -> extractHtml(contentBytes);
                case "text/plain", "text/markdown" -> extractPlainText(contentBytes, filename);
                default -> new ExtractionResult(
                        ExtractionStatus.FAILED,
                        EXTRACTOR_NAME,
                        EXTRACTOR_VERSION,
                        null,
                        null,
                        null,
                        0, 0, 0, 0,
                        "Unsupported MIME type: " + normalizedMime + " for file: " + filename,
                        Collections.emptyList()
                );
            };
        } catch (Exception e) {
            log.error("Document extraction failed for file '{}' (mime: {}): {}", filename, normalizedMime, e.getMessage(), e);
            return new ExtractionResult(
                    ExtractionStatus.FAILED,
                    EXTRACTOR_NAME,
                    EXTRACTOR_VERSION,
                    null,
                    null,
                    null,
                    0, 0, 0, 0,
                    "Extraction error: " + e.getMessage(),
                    Collections.emptyList()
            );
        }
    }

    private ExtractionResult extractPdf(byte[] bytes) throws Exception {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            int numPages = document.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();
            List<ExtractedChunk> chunks = new ArrayList<>();
            int totalChars = 0;
            int totalParagraphs = 0;
            int seq = 1;

            String docTitle = null;
            if (document.getDocumentInformation() != null) {
                docTitle = document.getDocumentInformation().getTitle();
            }

            for (int page = 1; page <= numPages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(document);

                if (pageText == null || pageText.isBlank()) {
                    continue;
                }

                String[] paragraphs = pageText.split("(?m)^\\s*$");
                for (String para : paragraphs) {
                    String clean = para.trim().replaceAll("\\s+", " ");
                    if (clean.length() >= MIN_CHUNK_CHARS) {
                        totalParagraphs++;
                        totalChars += clean.length();

                        // Detect potential section heading in first line
                        String heading = null;
                        if (clean.length() < 120 && Character.isUpperCase(clean.charAt(0))) {
                            heading = clean;
                        }

                        chunks.add(new ExtractedChunk(
                                seq++,
                                clean,
                                page,
                                heading,
                                null,
                                null
                        ));
                    }
                }
            }

            if (chunks.isEmpty()) {
                return new ExtractionResult(
                        ExtractionStatus.FAILED,
                        EXTRACTOR_NAME,
                        EXTRACTOR_VERSION,
                        docTitle,
                        "en",
                        null,
                        numPages, 0, 0, 0,
                        "PDF contains no extractable text (scanned image or empty).",
                        Collections.emptyList()
                );
            }

            Map<String, Object> meta = Map.of(
                    "pages", numPages,
                    "extractedParagraphs", totalParagraphs
            );

            return new ExtractionResult(
                    ExtractionStatus.COMPLETED,
                    EXTRACTOR_NAME,
                    EXTRACTOR_VERSION,
                    docTitle,
                    "en",
                    objectMapper.writeValueAsString(meta),
                    numPages,
                    totalParagraphs,
                    0,
                    totalChars,
                    null,
                    chunks
            );
        }
    }

    private ExtractionResult extractDocx(byte[] bytes) throws Exception {
        try (InputStream in = new ByteArrayInputStream(bytes);
             XWPFDocument document = new XWPFDocument(in)) {

            List<ExtractedChunk> chunks = new ArrayList<>();
            int seq = 1;
            int totalChars = 0;
            int totalParagraphs = 0;
            int totalTables = 0;
            String currentHeading = null;
            String docTitle = null;

            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph para) {
                    String text = para.getText().trim();
                    if (text.isBlank()) continue;

                    String style = para.getStyle();
                    if (style != null && style.toLowerCase().contains("heading")) {
                        currentHeading = text;
                        if (docTitle == null && (style.equalsIgnoreCase("Heading1") || style.equalsIgnoreCase("Title"))) {
                            docTitle = text;
                        }
                    }

                    if (text.length() >= MIN_CHUNK_CHARS) {
                        totalParagraphs++;
                        totalChars += text.length();
                        chunks.add(new ExtractedChunk(
                                seq++,
                                text,
                                null,
                                currentHeading,
                                null,
                                null
                        ));
                    }
                } else if (element instanceof XWPFTable table) {
                    totalTables++;
                    StringBuilder tableText = new StringBuilder();
                    for (XWPFTableRow row : table.getRows()) {
                        List<String> cellTexts = new ArrayList<>();
                        for (XWPFTableCell cell : row.getTableCells()) {
                            cellTexts.add(cell.getText().trim());
                        }
                        tableText.append(String.join(" | ", cellTexts)).append("\n");
                    }

                    String tStr = tableText.toString().trim();
                    if (tStr.length() >= MIN_CHUNK_CHARS) {
                        totalChars += tStr.length();
                        chunks.add(new ExtractedChunk(
                                seq++,
                                tStr,
                                null,
                                currentHeading != null ? currentHeading + " [Table]" : "[Table]",
                                null,
                                null
                        ));
                    }
                }
            }

            Map<String, Object> meta = Map.of(
                    "paragraphs", totalParagraphs,
                    "tables", totalTables
            );

            return new ExtractionResult(
                    ExtractionStatus.COMPLETED,
                    EXTRACTOR_NAME,
                    EXTRACTOR_VERSION,
                    docTitle,
                    "en",
                    objectMapper.writeValueAsString(meta),
                    null,
                    totalParagraphs,
                    totalTables,
                    totalChars,
                    null,
                    chunks
            );
        }
    }

    private ExtractionResult extractHtml(byte[] bytes) throws Exception {
        String html = new String(bytes, StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(html);
        String title = doc.title();

        List<ExtractedChunk> chunks = new ArrayList<>();
        int seq = 1;
        int totalChars = 0;
        int totalParagraphs = 0;
        int totalTables = 0;
        String currentHeading = null;

        // Extract body elements
        Elements elements = doc.body().select("h1, h2, h3, h4, h5, h6, p, table");
        for (Element el : elements) {
            String tagName = el.tagName().toLowerCase();
            if (tagName.startsWith("h")) {
                currentHeading = el.text().trim();
            } else if (tagName.equals("p")) {
                String pText = el.text().trim();
                if (pText.length() >= MIN_CHUNK_CHARS) {
                    totalParagraphs++;
                    totalChars += pText.length();
                    chunks.add(new ExtractedChunk(
                            seq++,
                            pText,
                            null,
                            currentHeading,
                            null,
                            null
                    ));
                }
            } else if (tagName.equals("table")) {
                totalTables++;
                StringBuilder tableText = new StringBuilder();
                Elements rows = el.select("tr");
                for (Element row : rows) {
                    Elements cells = row.select("th, td");
                    List<String> cellTexts = cells.eachText();
                    if (!cellTexts.isEmpty()) {
                        tableText.append(String.join(" | ", cellTexts)).append("\n");
                    }
                }
                String tStr = tableText.toString().trim();
                if (tStr.length() >= MIN_CHUNK_CHARS) {
                    totalChars += tStr.length();
                    chunks.add(new ExtractedChunk(
                            seq++,
                            tStr,
                            null,
                            currentHeading != null ? currentHeading + " [Table]" : "[Table]",
                            null,
                            null
                    ));
                }
            }
        }

        Map<String, Object> meta = Map.of(
                "paragraphs", totalParagraphs,
                "tables", totalTables,
                "htmlTitle", title != null ? title : ""
        );

        return new ExtractionResult(
                chunks.isEmpty() ? ExtractionStatus.FAILED : ExtractionStatus.COMPLETED,
                EXTRACTOR_NAME,
                EXTRACTOR_VERSION,
                title,
                "en",
                objectMapper.writeValueAsString(meta),
                null,
                totalParagraphs,
                totalTables,
                totalChars,
                chunks.isEmpty() ? "No text elements found in HTML body." : null,
                chunks
        );
    }

    private ExtractionResult extractPlainText(byte[] bytes, String filename) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        List<ExtractedChunk> chunks = new ArrayList<>();
        int seq = 1;
        int totalChars = 0;
        int totalParagraphs = 0;

        String[] paragraphs = text.split("(?m)^\\s*$");
        String currentHeading = null;
        String docTitle = null;

        for (String para : paragraphs) {
            String clean = para.trim();
            if (clean.isBlank()) continue;

            // Check if markdown heading
            if (clean.startsWith("#")) {
                int headingLevel = 0;
                while (headingLevel < clean.length() && clean.charAt(headingLevel) == '#') {
                    headingLevel++;
                }
                String headingText = clean.substring(headingLevel).trim();
                currentHeading = headingText;
                if (headingLevel == 1 && docTitle == null) {
                    docTitle = headingText;
                }
            }

            if (clean.length() >= MIN_CHUNK_CHARS) {
                totalParagraphs++;
                totalChars += clean.length();
                chunks.add(new ExtractedChunk(
                        seq++,
                        clean,
                        null,
                        currentHeading,
                        null,
                        null
                ));
            }
        }

        return new ExtractionResult(
                chunks.isEmpty() ? ExtractionStatus.FAILED : ExtractionStatus.COMPLETED,
                EXTRACTOR_NAME,
                EXTRACTOR_VERSION,
                docTitle != null ? docTitle : filename,
                "en",
                "{\"type\":\"plain_text\"}",
                null,
                totalParagraphs,
                0,
                totalChars,
                chunks.isEmpty() ? "File contains no paragraphs with sufficient length." : null,
                chunks
        );
    }

    private String normalizeMimeType(String mimeType, String filename) {
        if (mimeType != null && !mimeType.isBlank() && !mimeType.equals("application/octet-stream")) {
            return mimeType.toLowerCase().split(";")[0].trim();
        }

        if (filename == null) {
            return "application/octet-stream";
        }

        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html";
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) return "text/markdown";
        if (lower.endsWith(".txt")) return "text/plain";

        return "application/octet-stream";
    }
}

