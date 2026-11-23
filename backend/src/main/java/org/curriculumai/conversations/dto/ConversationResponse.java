package org.curriculumai.conversations.dto;

import org.curriculumai.conversations.Conversation;
import org.curriculumai.messages.dto.MessageResponse;

import java.util.List;

public record ConversationResponse(
        String id,
        String title,
        boolean isCustomTitle,
        List<MessageResponse> messages,
        String createdAt,
        String updatedAt
) {
    public static ConversationResponse fromEntity(Conversation c) {
        List<MessageResponse> messageResponses = c.getMessages() != null
                ? c.getMessages().stream().map(MessageResponse::fromEntity).toList()
                : List.of();

        return new ConversationResponse(
                c.getId(),
                c.getTitle(),
                c.isCustomTitle(),
                messageResponses,
                c.getCreatedAt().toString(),
                c.getUpdatedAt().toString()
        );
    }
}

