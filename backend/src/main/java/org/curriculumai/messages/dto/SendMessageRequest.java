package org.curriculumai.messages.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        @NotBlank(message = "Message content must not be blank")
        String content,
        String role,
        String payloadType
) {
}

