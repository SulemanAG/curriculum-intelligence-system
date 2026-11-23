package org.curriculumai.conversations;

import org.curriculumai.common.exceptions.ResourceNotFoundException;
import org.curriculumai.common.utils.DeterministicTitleExtractor;
import org.curriculumai.conversations.dto.ConversationResponse;
import org.curriculumai.conversations.dto.CreateConversationRequest;
import org.curriculumai.conversations.dto.UpdateConversationRequest;
import org.curriculumai.messages.Message;
import org.curriculumai.messages.MessageRepository;
import org.curriculumai.messages.dto.MessageResponse;
import org.curriculumai.messages.dto.SendMessageRequest;
import org.curriculumai.users.User;
import org.curriculumai.users.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ConversationService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            UserRepository userRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public ConversationResponse createConversation(String userId, CreateConversationRequest request) {
        User user = getOrCreateUser(userId);
        String convId = "conv-" + UUID.randomUUID();

        String initialTitle = (request != null && request.title() != null && !request.title().isBlank())
                ? request.title().trim()
                : "New Course Design";

        boolean customTitle = request != null && request.title() != null && !request.title().isBlank();

        if (!customTitle && request != null && request.initialPrompt() != null && !request.initialPrompt().isBlank()) {
            initialTitle = DeterministicTitleExtractor.extractTitle(request.initialPrompt());
        }

        Conversation conversation = new Conversation(convId, user, initialTitle);
        conversation.setCustomTitle(customTitle);

        Conversation saved = conversationRepository.save(conversation);

        if (request != null && request.initialPrompt() != null && !request.initialPrompt().isBlank()) {
            Message userMessage = new Message("msg-" + UUID.randomUUID(), saved, "user", request.initialPrompt());
            Message savedMsg = messageRepository.save(userMessage);
            saved.getMessages().add(savedMsg);
        }

        return ConversationResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> listUserConversations(String userId) {
        return conversationRepository.findAllActiveByUserId(userId).stream()
                .map(ConversationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(String userId, String conversationId) {
        Conversation conversation = conversationRepository.findByIdAndUserIdWithMessages(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));
        return ConversationResponse.fromEntity(conversation);
    }

    public ConversationResponse updateTitle(String userId, String conversationId, UpdateConversationRequest request) {
        Conversation conversation = conversationRepository.findByIdAndUserIdWithMessages(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        conversation.setTitle(request.title().trim());
        conversation.setCustomTitle(true);
        conversation.setUpdatedAt(Instant.now());

        Conversation saved = conversationRepository.save(conversation);
        return ConversationResponse.fromEntity(saved);
    }

    public void archiveConversation(String userId, String conversationId) {
        Conversation conversation = conversationRepository.findByIdAndUserIdWithMessages(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        conversation.setArchived(true);
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);
    }

    public MessageResponse appendMessage(String userId, String conversationId, SendMessageRequest request) {
        Conversation conversation = conversationRepository.findByIdAndUserIdWithMessages(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        // Enforce Message Role Integrity: Clients are restricted to role 'user'
        if (request.role() != null && !request.role().isBlank() && !"user".equalsIgnoreCase(request.role().trim())) {
            throw new IllegalArgumentException(
                    "Clients are only permitted to submit messages with role 'user'. Role '" + request.role() +
                    "' is restricted to internal system orchestration."
            );
        }

        String role = "user";
        String payloadType = (request.payloadType() != null && !request.payloadType().isBlank()) ? request.payloadType().trim() : "text";

        // Auto-title conversation on first user message if title has not been custom-set
        if (!conversation.isCustomTitle()) {
            long existingUserMessages = conversation.getMessages().stream()
                    .filter(m -> "user".equalsIgnoreCase(m.getRole()))
                    .count();
            if (existingUserMessages == 0 || "New Course Design".equals(conversation.getTitle())) {
                String intelligentTitle = DeterministicTitleExtractor.extractTitle(request.content());
                conversation.setTitle(intelligentTitle);
            }
        }

        Message message = new Message("msg-" + UUID.randomUUID(), conversation, role, request.content());
        message.setPayloadType(payloadType);
        Message saved = messageRepository.save(message);

        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        return MessageResponse.fromEntity(saved);
    }

    /**
     * Internal Coordinator / Specialist Swarm method to append assistant messages.
     * Restricted to trusted backend orchestration; not invokable by HTTP clients.
     */
    public MessageResponse appendAssistantMessage(String conversationId, String content, String payloadType, String researchRunId, String selectedAlternativeId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        Message message = new Message("msg-" + UUID.randomUUID(), conversation, "assistant", content);
        message.setPayloadType(payloadType != null ? payloadType : "text");
        message.setResearchRunId(researchRunId);
        message.setSelectedAlternativeId(selectedAlternativeId);

        Message saved = messageRepository.save(message);
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        return MessageResponse.fromEntity(saved);
    }

    /**
     * Internal system notification method to append system event messages.
     * Restricted to trusted backend orchestration; not invokable by HTTP clients.
     */
    public MessageResponse appendSystemMessage(String conversationId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        Message message = new Message("msg-" + UUID.randomUUID(), conversation, "system", content);
        message.setPayloadType("system_event");

        Message saved = messageRepository.save(message);
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        return MessageResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationMessages(String userId, String conversationId) {
        // Verify conversation belongs to user
        conversationRepository.findByIdAndUserIdWithMessages(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(MessageResponse::fromEntity)
                .toList();
    }

    private User getOrCreateUser(String userId) {
        return userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User(userId, "user_" + userId, userId + "@curriculumai.internal", "Faculty Member", "FACULTY");
            return userRepository.save(newUser);
        });
    }
}

