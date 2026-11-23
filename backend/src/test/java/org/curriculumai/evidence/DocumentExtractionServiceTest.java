package org.curriculumai.evidence;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.curriculumai.evidence.domain.ExtractionStatus;
import org.curriculumai.evidence.service.DocumentExtractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class DocumentExtractionServiceTest {

    private DocumentExtractionService extractionService;

    @BeforeEach
    void setUp() {
        extractionService = new DocumentExtractionService();
    }

    @Test
    @DisplayName("Extracts plain text and markdown paragraphs with section headings")
    void testExtractMarkdown() {
        String md = """
                # ABET Accreditation Criteria
                
                The curriculum must provide adequate engineering knowledge and design capabilities.
                
                ## Computing Sciences Foundation
                
                Students must acquire strong foundational competency in algorithms, distributed systems, and computer architecture.
                """;

        DocumentExtractionService.ExtractionResult result = extractionService.extract(
                md.getBytes(StandardCharsets.UTF_8),
                "text/markdown",
                "abet.md"
        );

        assertEquals(ExtractionStatus.COMPLETED, result.status());
        assertEquals("ABET Accreditation Criteria", result.documentTitle());
        assertFalse(result.chunks().isEmpty());
        assertTrue(result.chunks().stream().anyMatch(c -> c.textContent().contains("engineering knowledge")));
        assertTrue(result.chunks().stream().anyMatch(c -> c.sectionHeading() != null && c.sectionHeading().contains("Computing Sciences Foundation")));
    }

    @Test
    @DisplayName("Extracts HTML documents preserving title, headings, and tables")
    void testExtractHtml() {
        String html = """
                <!DOCTYPE html>
                <html>
                <head><title>IEEE Software Engineering Curricula 2024</title></head>
                <body>
                  <h1>Knowledge Areas Overview</h1>
                  <p>Software engineering education requires rigorous verification and validation techniques.</p>
                  <h2>Course Allocation Table</h2>
                  <table>
                    <tr><th>Course Code</th><th>Title</th><th>Credits</th></tr>
                    <tr><td>SE301</td><td>Distributed Systems</td><td>4</td></tr>
                  </table>
                </body>
                </html>
                """;

        DocumentExtractionService.ExtractionResult result = extractionService.extract(
                html.getBytes(StandardCharsets.UTF_8),
                "text/html",
                "ieee_se2024.html"
        );

        assertEquals(ExtractionStatus.COMPLETED, result.status());
        assertEquals("IEEE Software Engineering Curricula 2024", result.documentTitle());
        assertEquals(1, result.totalTables());
        assertTrue(result.chunks().stream().anyMatch(c -> c.textContent().contains("verification and validation")));
        assertTrue(result.chunks().stream().anyMatch(c -> c.textContent().contains("SE301 | Distributed Systems | 4")));
    }

    @Test
    @DisplayName("Extracts Word DOCX documents with paragraphs and table structure")
    void testExtractDocx() throws Exception {
        byte[] docxBytes;
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph p1 = doc.createParagraph();
            p1.setStyle("Heading1");
            p1.createRun().setText("ACM/IEEE Computer Science Curricula");

            XWPFParagraph p2 = doc.createParagraph();
            p2.createRun().setText("Undergraduate students must master distributed consensus, scalability, and asynchronous systems programming.");

            XWPFTable table = doc.createTable(2, 2);
            table.getRow(0).getCell(0).setText("Core Area");
            table.getRow(0).getCell(1).setText("Min Hours");
            table.getRow(1).getCell(0).setText("Software Architecture");
            table.getRow(1).getCell(1).setText("40 Hours");

            doc.write(out);
            docxBytes = out.toByteArray();
        }

        DocumentExtractionService.ExtractionResult result = extractionService.extract(
                docxBytes,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "curricula.docx"
        );

        assertEquals(ExtractionStatus.COMPLETED, result.status());
        assertEquals(1, result.totalTables());
        assertTrue(result.chunks().stream().anyMatch(c -> c.textContent().contains("distributed consensus")));
        assertTrue(result.chunks().stream().anyMatch(c -> c.textContent().contains("Software Architecture | 40 Hours")));
    }

    @Test
    @DisplayName("Extracts PDF documents preserving page-level provenance")
    void testExtractPdf() throws Exception {
        byte[] pdfBytes;
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page1 = new PDPage();
            doc.addPage(page1);
            try (PDPageContentStream stream = new PDPageContentStream(doc, page1)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText("Page 1: Artificial Intelligence and Machine Learning Competencies.");
                stream.endText();
            }

            PDPage page2 = new PDPage();
            doc.addPage(page2);
            try (PDPageContentStream stream = new PDPageContentStream(doc, page2)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText("Page 2: Practical Lab Work Requirements for Autonomous Multi-Agent Systems.");
                stream.endText();
            }

            doc.save(out);
            pdfBytes = out.toByteArray();
        }

        DocumentExtractionService.ExtractionResult result = extractionService.extract(
                pdfBytes,
                "application/pdf",
                "ai_competencies.pdf"
        );

        assertEquals(ExtractionStatus.COMPLETED, result.status());
        assertEquals(2, result.totalPages());
        assertEquals(2, result.chunks().size());
        assertEquals(1, result.chunks().get(0).pageNumber());
        assertEquals(2, result.chunks().get(1).pageNumber());
        assertTrue(result.chunks().get(0).textContent().contains("Page 1"));
        assertTrue(result.chunks().get(1).textContent().contains("Page 2"));
    }

    @Test
    @DisplayName("Rejects unsupported MIME formats without fabricating text")
    void testRejectsUnsupportedFormat() {
        byte[] dummyPng = new byte[]{ (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        DocumentExtractionService.ExtractionResult result = extractionService.extract(
                dummyPng,
                "image/png",
                "diagram.png"
        );

        assertEquals(ExtractionStatus.FAILED, result.status());
        assertTrue(result.errorDetails().contains("Unsupported MIME type"));
        assertTrue(result.chunks().isEmpty());
    }

    @Test
    @DisplayName("Handles empty files gracefully")
    void testHandlesEmptyFile() {
        DocumentExtractionService.ExtractionResult result = extractionService.extract(
                new byte[0],
                "text/plain",
                "empty.txt"
        );

        assertEquals(ExtractionStatus.FAILED, result.status());
        assertTrue(result.errorDetails().contains("Empty document"));
        assertTrue(result.chunks().isEmpty());
    }
}

