package com.team12345.messenger.controller;

import com.team12345.messenger.dto.request.ConversationRequestDTO;
import com.team12345.messenger.dto.request.ConversationUpdateDTO;
import com.team12345.messenger.dto.response.ConversationResponseDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.entity.Conversation;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.ConversationService;
import com.team12345.messenger.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversation API", description = "Endpoints for managing conversations and participants")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    @Operation(summary = "Lấy lịch sử tin nhắn", description = "Lấy danh sách tin nhắn của một hội thoại")
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<Page<MessageResponseDTO>> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long conversationId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<MessageResponseDTO> messages = messageService.getMessagesByConversation(conversationId, userDetails.getId(), pageable);
        return ResponseEntity.ok(messages);
    }
    @Operation(summary = "Get list of conversations", description = "Get a paginated list of conversations for the current user")
    @GetMapping
    public ResponseEntity<Page<ConversationResponseDTO>> getConversations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ConversationResponseDTO> result = conversationService.getUserConversations(userDetails.getId(), pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get conversation details", description = "Get detailed info of a conversation. User must be a participant.")
    @GetMapping("/{id}")
    public ResponseEntity<?> getConversationDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        try {
            ConversationResponseDTO result = conversationService.getConversationDetails(id, userDetails.getId());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not a participant")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Create conversation", description = "Create a new 1-1 or group conversation")
    @PostMapping
    public ResponseEntity<ConversationResponseDTO> createConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ConversationRequestDTO request) {
        ConversationResponseDTO conversation = conversationService.createConversation(
                userDetails.getId(),
                request.getName(),
                request.getIsGroup(),
                request.getParticipantIds()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
    }

    @Operation(summary = "Add participant", description = "Add a new member to the conversation (Admin only for groups)")
    @PostMapping("/{id}/participants")
    public ResponseEntity<?> addParticipant(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long userIdToAdd = body.get("userId");
        if (userIdToAdd == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "userId is required"));
        }
        try {
            conversationService.addParticipant(id, userDetails.getId(), userIdToAdd);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not a participant") || e.getMessage().contains("Only admins")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Remove participant", description = "Leave group or remove a member (Admin only for removing others)")
    @DeleteMapping("/{id}/participants/{userId}")
    public ResponseEntity<?> removeParticipant(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @PathVariable Long userId) {
        try {
            conversationService.removeParticipant(id, userDetails.getId(), userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not a participant") || e.getMessage().contains("Only admins")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Update conversation", description = "Update group name or avatar (Admin only for groups)")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody ConversationUpdateDTO request) {
        try {
            Conversation updated = conversationService.updateConversation(
                    id, 
                    userDetails.getId(), 
                    request.getName(), 
                    request.getAvatarUrl()
            );
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not a participant") || e.getMessage().contains("Only admins")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Get participants", description = "Get a paginated list of participants with optional search keyword")
    @GetMapping("/{id}/participants")
    public ResponseEntity<?> getParticipants(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<com.team12345.messenger.dto.response.ParticipantResponseDTO> participants = conversationService.getParticipants(id, userDetails.getId(), keyword, pageable);
            return ResponseEntity.ok(participants);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Leave conversation", description = "Current user leaves a group conversation")
    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leaveConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        try {
            conversationService.leaveConversation(id, userDetails.getId());
            return ResponseEntity.ok(Map.of("message", "Left conversation successfully"));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}
