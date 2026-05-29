package com.team12345.messenger.controller;

import com.team12345.messenger.dto.response.AttachmentResponseDTO;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachment API", description = "Endpoints for managing attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Operation(summary = "Upload attachment", description = "Upload a file as an attachment to a message")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<AttachmentResponseDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "messageId", required = false) Long messageId) {
        
        // Pass messageId directly; it might be null for standalone uploads before saving the message
        AttachmentResponseDTO response = attachmentService.uploadAttachment(file, messageId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get attachment", description = "Get attachment details by ID")
    @GetMapping("/{attachmentId}")
    public ResponseEntity<AttachmentResponseDTO> getAttachment(@PathVariable Long attachmentId) {
        AttachmentResponseDTO response = attachmentService.getAttachmentById(attachmentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get attachments by message", description = "Get all attachments for a specific message")
    @GetMapping("/message/{messageId}")
    public ResponseEntity<List<AttachmentResponseDTO>> getAttachmentsByMessage(@PathVariable Long messageId) {
        List<AttachmentResponseDTO> responses = attachmentService.getAttachmentsByMessageId(messageId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Delete attachment", description = "Delete an attachment by ID")
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long attachmentId) {
        
        attachmentService.deleteAttachment(attachmentId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}