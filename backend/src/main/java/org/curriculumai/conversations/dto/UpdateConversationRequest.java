package org.curriculumai.conversations.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateConversationRequest(
        @NotBlank(message = "Title must not be blank")
        String title
) {
}

