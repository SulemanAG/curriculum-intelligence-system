package org.curriculumai.coordinator.dto;

import jakarta.validation.constraints.NotBlank;

public record CourseRequestInput(
        @NotBlank(message = "Raw prompt must not be blank")
        String rawPrompt,
        String department,
        String degreeLevel,
        String conversationId
) {
}

