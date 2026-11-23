package org.curriculumai.coordinator.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record FeedbackInput(
        String requestId,
        @NotBlank(message = "selectedAlternativeId must not be blank")
        String selectedAlternativeId,
        List<String> rejectedAlternativeIds,
        String facultyName,
        String facultyId,
        @NotBlank(message = "Rationale must not be blank")
        String rationale,
        String suggestedModifications,
        String submittedAt
) {
}

