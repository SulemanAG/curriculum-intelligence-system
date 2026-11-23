package org.curriculumai.conversations;

import org.curriculumai.conversations.dto.ConversationResponse;
import org.curriculumai.conversations.dto.CreateConversationRequest;
import org.curriculumai.conversations.dto.UpdateConversationRequest;
import org.curriculumai.messages.dto.MessageResponse;
import org.curriculumai.messages.dto.SendMessageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ConversationServiceTest {

    @Autowired
    private ConversationService conversationService;

    private final String userId = "usr-test-faculty";

    @Test
    @DisplayName("Create conversation and auto-title on first user message")
    void testCreateAndAutoTitleConversation() {
        CreateConversationRequest createReq = new CreateConversationRequest(null, null);
        ConversationResponse created = conversationService.createConversation(userId, createReq);

        assertNotNull(created.id());
        assertEquals("New Course Design", created.title());
        assertFalse(created.isCustomTitle());

        // Append first message
        SendMessageRequest msgReq = new SendMessageRequest(
                "I need to design the course plan for Computer Vision",
                "user",
                "text"
        );
        MessageResponse msgResponse = conversationService.appendMessage(userId, created.id(), msgReq);
        assertNotNull(msgResponse.id());

        // Check that conversation title intelligently updated to 'Computer Vision'
        ConversationResponse updated = conversationService.getConversation(userId, created.id());
        assertEquals("Computer Vision", updated.title());
    }

    @Test
    @DisplayName("Manual rename locks custom title and prevents auto-title overwrite")
    void testManualRenameLocksCustomTitle() {
        CreateConversationRequest createReq = new CreateConversationRequest(null, null);
        ConversationResponse created = conversationService.createConversation(userId, createReq);

        // Manually rename
        UpdateConversationRequest renameReq = new UpdateConversationRequest("Custom Advanced Curriculum");
        conversationService.updateTitle(userId, created.id(), renameReq);

        ConversationResponse renamed = conversationService.getConversation(userId, created.id());
        assertEquals("Custom Advanced Curriculum", renamed.title());
        assertTrue(renamed.isCustomTitle());

        // Send a message that would otherwise trigger auto-title
        SendMessageRequest msgReq = new SendMessageRequest(
                "Create a curriculum for Machine Learning",
                "user",
                "text"
        );
        conversationService.appendMessage(userId, created.id(), msgReq);

        // Title must remain the locked custom title
        ConversationResponse afterMsg = conversationService.getConversation(userId, created.id());
        assertEquals("Custom Advanced Curriculum", afterMsg.title());
    }

    @Test
    @DisplayName("Message chronological ordering is preserved across user and assistant messages")
    void testMessageChronologicalOrdering() {
        CreateConversationRequest createReq = new CreateConversationRequest("Ordering Test", null);
        ConversationResponse created = conversationService.createConversation(userId, createReq);

        conversationService.appendMessage(userId, created.id(), new SendMessageRequest("Message 1", "user", "text"));
        conversationService.appendAssistantMessage(created.id(), "Message 2", "text", null, null);
        conversationService.appendMessage(userId, created.id(), new SendMessageRequest("Message 3", "user", "text"));

        List<MessageResponse> messages = conversationService.getConversationMessages(userId, created.id());
        assertEquals(3, messages.size());
        assertEquals("Message 1", messages.get(0).content());
        assertEquals("user", messages.get(0).role());
        assertEquals("Message 2", messages.get(1).content());
        assertEquals("assistant", messages.get(1).role());
        assertEquals("Message 3", messages.get(2).content());
        assertEquals("user", messages.get(2).role());
    }

    @Test
    @DisplayName("Reject client attempting to impersonate assistant role")
    void testRejectClientAssistantRoleImpersonation() {
        CreateConversationRequest createReq = new CreateConversationRequest("Impersonation Guard", null);
        ConversationResponse created = conversationService.createConversation(userId, createReq);

        SendMessageRequest spoofAssistant = new SendMessageRequest("Spoofed assistant response", "assistant", "text");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                conversationService.appendMessage(userId, created.id(), spoofAssistant)
        );

        assertTrue(ex.getMessage().contains("Clients are only permitted to submit messages with role 'user'"));
    }

    @Test
    @DisplayName("Reject client attempting to submit system role")
    void testRejectClientSystemRoleImpersonation() {
        CreateConversationRequest createReq = new CreateConversationRequest("System Guard", null);
        ConversationResponse created = conversationService.createConversation(userId, createReq);

        SendMessageRequest spoofSystem = new SendMessageRequest("Spoofed system directive", "system", "text");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                conversationService.appendMessage(userId, created.id(), spoofSystem)
        );

        assertTrue(ex.getMessage().contains("Clients are only permitted to submit messages with role 'user'"));
    }

    @Test
    @DisplayName("Internal appendAssistantMessage and appendSystemMessage succeed with correct roles")
    void testInternalAssistantAndSystemMessages() {
        CreateConversationRequest createReq = new CreateConversationRequest("Internal Messages", null);
        ConversationResponse created = conversationService.createConversation(userId, createReq);

        MessageResponse assistantMsg = conversationService.appendAssistantMessage(
                created.id(),
                "Synthesizing curriculum...",
                "text",
                "run-123",
                "ALT-1"
        );
        assertEquals("assistant", assistantMsg.role());
        assertEquals("run-123", assistantMsg.researchRunId());

        MessageResponse systemMsg = conversationService.appendSystemMessage(
                created.id(),
                "AURA coordinator dispatched research stages."
        );
        assertEquals("system", systemMsg.role());
        assertEquals("system_event", systemMsg.payloadType());
    }
}
