package org.curriculumai.messages.dto;

import org.curriculumai.messages.Message;

public record MessageResponse(
        String id,
        String role,
        String content,
        String timestamp,
        String payloadType,
        String researchRunId,
        String selectedAlternativeId
) {
    public static MessageResponse fromEntity(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getRole(),
                m.getContent(),
                m.getCreatedAt().toString(),
                m.getPayloadType(),
                m.getResearchRunId(),
                m.getSelectedAlternativeId()
        );
    }
}

