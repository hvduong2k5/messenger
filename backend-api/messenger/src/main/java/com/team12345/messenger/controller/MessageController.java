package com.team12345.messenger.controller;

import com.team12345.messenger.dto.request.MessageRequestDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Tag(name = "Message API", description = "Endpoints for managing messages")
public class MessageController {

    private final MessageService messageService;



    @Operation(summary = "Gửi tin nhắn", description = "Gửi tin nhắn văn bản hoặc tệp đính kèm vào cuộc hội thoại")
    @PostMapping
    public ResponseEntity<MessageResponseDTO> sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute MessageRequestDTO requestDTO) {
        
        requestDTO.setSenderId(userDetails.getId());
        var result = messageService.saveMessage(requestDTO);
        MessageResponseDTO savedMessage = result.message();
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMessage);
    }

    @Operation(summary = "Thu hồi/Xóa tin nhắn", description = "Xóa mềm một tin nhắn (chỉ người gửi mới có quyền)")
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> revokeMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long messageId) {
        
        messageService.revokeMessage(messageId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Chỉnh sửa tin nhắn", description = "Cập nhật nội dung tin nhắn (chỉ người gửi mới có quyền)")
    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponseDTO> editMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long messageId,
            @Valid @RequestBody MessageRequestDTO requestDTO) {
        
        MessageResponseDTO updatedMessage = messageService.editMessage(messageId, userDetails.getId(), requestDTO.getContent());
        return ResponseEntity.ok(updatedMessage);
    }

    @Operation(summary = "Tìm kiếm tin nhắn", description = "Tìm kiếm nội dung tin nhắn theo từ khóa (bắt buộc truyền conversationId để đảm bảo quyền bảo mật)")
    @GetMapping("/search")
    public ResponseEntity<Page<MessageResponseDTO>> searchMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String keyword,
            @RequestParam Long conversationId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<MessageResponseDTO> results = messageService.searchMessages(keyword, conversationId, userDetails.getId(), pageable);
        return ResponseEntity.ok(results);
    }
}
