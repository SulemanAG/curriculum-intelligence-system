package org.curriculumai.messages;

import jakarta.persistence.*;
import org.curriculumai.conversations.Conversation;

import java.time.Instant;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(nullable = false, length = 20)
    private String role; // 'user', 'assistant', 'system'

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "payload_type", length = 50)
    private String payloadType = "text";

    @Column(name = "research_run_id", length = 64)
    private String researchRunId;

    @Column(name = "selected_alternative_id", length = 64)
    private String selectedAlternativeId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Message() {
    }

    public Message(String id, Conversation conversation, String role, String content) {
        this.id = id;
        this.conversation = conversation;
        this.role = role;
        this.content = content;
        this.payloadType = "text";
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }

    public String getResearchRunId() {
        return researchRunId;
    }

    public void setResearchRunId(String researchRunId) {
        this.researchRunId = researchRunId;
    }

    public String getSelectedAlternativeId() {
        return selectedAlternativeId;
    }

    public void setSelectedAlternativeId(String selectedAlternativeId) {
        this.selectedAlternativeId = selectedAlternativeId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

