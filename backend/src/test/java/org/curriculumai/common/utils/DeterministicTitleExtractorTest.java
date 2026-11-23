package org.curriculumai.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeterministicTitleExtractorTest {

    @ParameterizedTest(name = "[{index}] \"{0}\" -> \"{1}\"")
    @CsvSource(delimiter = '|', value = {
            "I need to design the course plan for Computer Vision | Computer Vision",
            "Create a curriculum for Machine Learning | Machine Learning",
            "Help me prepare a course plan for Deep Learning | Deep Learning",
            "I want to analyse industry requirements for Generative AI | Generative AI",
            "Compare the syllabus of Data Structures across IITs | Data Structures",
            "Design a course plan for Natural Language Processing | Natural Language Processing",
            "Course plan for Natural Language Processing for 6th semester B.Tech. | Natural Language Processing",
            "Prepare syllabus for Agentic AI Systems aligned with ACM and APNI standards. | Agentic AI Systems",
            "Design an undergraduate course on Edge Computing and IoT Networks. | Edge Computing and IoT Networks",
            "Computer Vision (UG, Core, Sem 5, 3:0:2) | Computer Vision",
            "Hello there | Hello There"
    })
    @DisplayName("Deterministic title extraction test suite")
    void testTitleExtraction(String prompt, String expected) {
        String actual = DeterministicTitleExtractor.extractTitle(prompt);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Null or blank prompt returns default title")
    void testNullOrBlank() {
        assertEquals("New Course Design", DeterministicTitleExtractor.extractTitle(null));
        assertEquals("New Course Design", DeterministicTitleExtractor.extractTitle("   "));
    }
}

