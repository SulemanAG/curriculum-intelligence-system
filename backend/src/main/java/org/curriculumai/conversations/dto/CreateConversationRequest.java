package org.curriculumai.conversations.dto;

public record CreateConversationRequest(
        String title,
        String initialPrompt
) {
}

