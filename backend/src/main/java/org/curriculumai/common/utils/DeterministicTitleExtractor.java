package org.curriculumai.common.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class DeterministicTitleExtractor {

    private static final String DEFAULT_TITLE = "New Course Design";

    private static final Pattern PAREN_PATTERN = Pattern.compile("^([^(]+)\\s*\\([^)]+\\)$");

    private static final Pattern PREFIX_PATTERN = Pattern.compile(
            "^(?:(?:please|can you|could you|help me|i need to|i want to|i would like to|we need to|kindly)\\s+)?" +
            "(?:(?:create|design|prepare|build|generate|develop|compare|analyse|analyze|review|draft|synthesize)\\s+)?" +
            "(?:(?:a|an|the)\\s+)?" +
            "(?:(?:undergraduate|postgraduate|ug|pg)\\s+)?" +
            "(?:(?:course\\s+plan|curriculum|syllabus|course\\s+structure|instruction\\s+plan|course\\s+outline|course)\\s+(?:for|of|on|in|across)\\s+|" +
            "(?:syllabus|curriculum)\\s+of\\s+|" +
            "(?:industry\\s+requirements|industry\\s+demand)\\s+for\\s+|" +
            "(?:course)\\s+(?:on|for|in)\\s+)?",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TRAILING_PATTERN = Pattern.compile(
            "\\s+(?:across\\s+(?:iits?|nits?|universities|different\\s+institutions|colleges|institutions?)|" +
            "for\\s+(?:\\d+(?:st|nd|rd|th)\\s+semester|b\\.?tech|m\\.?tech|my\\s+university|our\\s+department|university|students?|engineering).*|" +
            "aligned\\s+with\\s+.*|" +
            "based\\s+on\\s+.*|" +
            "according\\s+to\\s+.*|" +
            "with\\s+(?:hands-on|industry|lab).*)$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Set<String> ACRONYMS = new HashSet<>(Arrays.asList(
            "AI", "ML", "NLP", "CV", "IoT", "LLM", "CNN", "RNN", "VLSI", "OS",
            "DBMS", "API", "AWS", "GCP", "IIT", "NIT", "ACM", "IEEE", "GPU", "CPU"
    ));

    private static final Set<String> MINOR_WORDS = new HashSet<>(Arrays.asList(
            "and", "or", "of", "in", "on", "for", "with", "to", "at", "by"
    ));

    private static final Set<String> GENERIC_TITLES = new HashSet<>(Arrays.asList(
            "Course Plan", "Curriculum", "Syllabus", "New Course", "Course", "Plan"
    ));

    private DeterministicTitleExtractor() {
    }

    public static String extractTitle(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return DEFAULT_TITLE;
        }

        String cleaned = prompt.trim().replaceAll("^[\"\'`]+|[\"\'`]+$", "");

        Matcher parenMatcher = PAREN_PATTERN.matcher(cleaned);
        if (parenMatcher.matches()) {
            String candidate = parenMatcher.group(1).trim();
            if (candidate.length() > 2) {
                cleaned = candidate;
            }
        }

        cleaned = PREFIX_PATTERN.matcher(cleaned).replaceFirst("").trim();
        cleaned = TRAILING_PATTERN.matcher(cleaned).replaceFirst("").trim();
        cleaned = cleaned.replaceAll("[.?!,:;]+$", "").trim();

        if (cleaned.length() < 2) {
            return DEFAULT_TITLE;
        }

        String[] rawWords = cleaned.split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < Math.min(rawWords.length, 5); i++) {
            String word = rawWords[i];
            String upper = word.toUpperCase();
            if (i > 0) sb.append(" ");

            if (ACRONYMS.contains(upper)) {
                sb.append(upper);
            } else if (i > 0 && MINOR_WORDS.contains(word.toLowerCase())) {
                sb.append(word.toLowerCase());
            } else if (Character.isUpperCase(word.charAt(0)) && !word.substring(1).equals(word.substring(1).toUpperCase())) {
                sb.append(word);
            } else {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
            }
        }

        String title = sb.toString().trim();
        if (GENERIC_TITLES.contains(title)) {
            return DEFAULT_TITLE;
        }

        return title;
    }
}

