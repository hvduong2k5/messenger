package com.team12345.messenger.controller;

import com.team12345.messenger.dto.response.MessageStatusResponseDTO;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.MessageStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/message-statuses")
@RequiredArgsConstructor
@Tag(name = "Message Status", description = "Endpoints for managing message read receipts and delivery statuses")
public class MessageStatusController {

    private final MessageStatusService messageStatusService;

    @Operation(summary = "Mark entire conversation as read", description = "Marks all messages in a conversation as READ for the current user and triggers an MQTT notification to the sender.")
    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markConversationAsRead(@PathVariable Long conversationId) {
        Long currentUserId = getCurrentUserId();
        messageStatusService.markConversationAsRead(conversationId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update status of a specific message", description = "Updates the status of a specific message to DELIVERED or READ for the current user and triggers an MQTT notification.")
    @PatchMapping("/messages/{messageId}")
    public ResponseEntity<Void> updateMessageStatus(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> requestBody) {
        
        String status = requestBody.get("status");
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status must be provided");
        }

        Long currentUserId = getCurrentUserId();
        messageStatusService.updateMessageStatus(messageId, currentUserId, status);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get read statuses for a specific message", description = "Returns a list of users who have received or read the message, along with the timestamp.")
    @GetMapping("/messages/{messageId}")
    public ResponseEntity<List<MessageStatusResponseDTO>> getMessageStatuses(@PathVariable Long messageId) {
        return ResponseEntity.ok(messageStatusService.getMessageStatuses(messageId));
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId();
        }
        throw new RuntimeException("Could not extract user from Security Context");
    }
}
