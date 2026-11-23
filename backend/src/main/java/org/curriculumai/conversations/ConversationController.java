package org.curriculumai.conversations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.curriculumai.common.ApiResponse;
import org.curriculumai.conversations.dto.ConversationResponse;
import org.curriculumai.conversations.dto.CreateConversationRequest;
import org.curriculumai.conversations.dto.UpdateConversationRequest;
import org.curriculumai.messages.dto.MessageResponse;
import org.curriculumai.messages.dto.SendMessageRequest;
import org.curriculumai.security.UserIdentityResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@Tag(name = "Conversations", description = "Durable Conversation Management & Chat History API")
public class ConversationController {

    private final ConversationService conversationService;
    private final UserIdentityResolver userIdentityResolver;

    public ConversationController(ConversationService conversationService, UserIdentityResolver userIdentityResolver) {
        this.conversationService = conversationService;
        this.userIdentityResolver = userIdentityResolver;
    }

    private String resolveUserId(String userId) {
        return userIdentityResolver.resolveUserId(userId);
    }

    @PostMapping
    @Operation(summary = "Create a new conversation workspace")
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @RequestHeader(value = "X-User-Id", defaultValue = "usr-default-faculty") String userId,
            @RequestBody(required = false) CreateConversationRequest request
    ) {
        ConversationResponse response = conversationService.createConversation(resolveUserId(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Conversation created successfully."));
    }

    @GetMapping
    @Operation(summary = "List all active conversations for the authenticated user")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> listConversations(
            @RequestHeader(value = "X-User-Id", defaultValue = "usr-default-faculty") String userId
    ) {
        List<ConversationResponse> response = conversationService.listUserConversations(resolveUserId(userId));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a conversation by ID with its messages")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @RequestHeader(value = "X-User-Id", defaultValue = "usr-default-faculty") String userId,
            @PathVariable("id") String id
    ) {
        ConversationResponse response = conversationService.getConversation(resolveUserId(userId), id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Rename a conversation and lock custom title")
    public ResponseEntity<ApiResponse<ConversationResponse>> renameConversation(
            @RequestHeader(value = "X-User-Id", defaultValue = "usr-default-faculty") String userId,
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateConversationRequest request
    ) {
        ConversationResponse response = conversationService.updateTitle(resolveUserId(userId), id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Conversation title updated."));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Archive a conversation")
    public ResponseEntity<ApiResponse<Void>> archiveConversation(
            @RequestHeader(value = "X-User-Id", defaultValue = "usr-default-faculty") String userId,
            @PathVariable("id") String id
    ) {
        conversationService.archiveConversation(resolveUserId(userId), id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Conversation archived successfully."));
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "Append a message to an existing conversation")
    public ResponseEntity<ApiResponse<MessageResponse>> appendMessage(
            @RequestHeader(value = "X-User-Id", defaultValue = "usr-default-faculty") String userId,
            @PathVariable("id") String id,
            @Valid @RequestBody SendMessageRequest request
    ) {
        MessageResponse response = conversationService.appendMessage(resolveUserId(userId), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Message sent."));
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Retrieve messages for a conversation in chronological order")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @RequestHeader(value = "X-User-Id", defaultValue = "usr-default-faculty") String userId,
            @PathVariable("id") String id
    ) {
        List<MessageResponse> response = conversationService.getConversationMessages(resolveUserId(userId), id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

