 package com.team12345.messenger.controller;

import com.team12345.messenger.dto.response.AttachmentResponseDTO;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "Attachment", description = "Endpoints for managing file attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Operation(summary = "Upload a file", description = "Uploads a file to Cloudinary and returns attachment info. Use form-data with key 'file'.")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<AttachmentResponseDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(attachmentService.uploadAttachment(file, currentUserId));
    }

    @Operation(summary = "Get attachment info")
    @GetMapping("/{id}")
    public ResponseEntity<AttachmentResponseDTO> getAttachment(@PathVariable Long id) {
        return ResponseEntity.ok(attachmentService.getAttachmentById(id));
    }

    @Operation(summary = "Get attachments by message ID")
    @GetMapping("/message/{messageId}")
    public ResponseEntity<List<AttachmentResponseDTO>> getAttachmentsByMessage(@PathVariable Long messageId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByMessageId(messageId));
    }

    @Operation(summary = "Delete an attachment")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        attachmentService.deleteAttachment(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId();
        }
        throw new RuntimeException("Could not extract user from Security Context");
    }
}
