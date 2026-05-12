package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.response.AttachmentResponseDTO;
import com.team12345.messenger.entity.Attachment;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.exception.ResourceNotFoundException;
import com.team12345.messenger.repository.AttachmentRepository;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.service.AttachmentService;
import com.team12345.messenger.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final MediaService mediaService;
    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 50MB
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList("video/mp4", "video/mpeg", "video/quicktime");
    private static final List<String> ALLOWED_OTHER_TYPES = Arrays.asList("application/pdf", "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/plain");

    @Override
    @Transactional
    public AttachmentResponseDTO uploadAttachment(MultipartFile file, Long userId) {
        validateFile(file);

        Map<String, Object> uploadResult = mediaService.uploadFile(file);
        
        User uploader = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Attachment attachment = Attachment.builder()
                .fileUrl((String) uploadResult.get("url"))
                .fileType((String) uploadResult.get("type"))
                .fileSize((Integer) uploadResult.get("size"))
                .publicId((String) uploadResult.get("publicId"))
                .uploader(uploader)
                .build();

        Attachment savedAttachment = attachmentRepository.save(attachment);
        return mapToResponseDTO(savedAttachment);
    }

    @Override
    public AttachmentResponseDTO getAttachmentById(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
        return mapToResponseDTO(attachment);
    }

    @Override
    public List<AttachmentResponseDTO> getAttachmentsByMessageId(Long messageId) {
        return attachmentRepository.findByMessageId(messageId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAttachment(Long id, Long userId) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));

        // Ownership check: uploader or message sender
        boolean isOwner = (attachment.getUploader() != null && attachment.getUploader().getId().equals(userId)) ||
                (attachment.getMessage() != null && attachment.getMessage().getSender().getId().equals(userId));
        
        if (!isOwner) {
            throw new RuntimeException("You do not have permission to delete this attachment");
        }

        if (attachment.getPublicId() != null) {
            mediaService.deleteFile(attachment.getPublicId());
        }

        attachmentRepository.delete(attachment);
    }

    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        long size = file.getSize();

        if (contentType == null) {
            throw new IllegalArgumentException("File content type is missing");
        }

        if (ALLOWED_IMAGE_TYPES.contains(contentType)) {
            if (size > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException("Image size exceeds maximum limit of 10MB");
            }
        } else if (ALLOWED_VIDEO_TYPES.contains(contentType)) {
            if (size > MAX_VIDEO_SIZE) {
                throw new IllegalArgumentException("Video size exceeds maximum limit of 50MB");
            }
        } else if (!ALLOWED_OTHER_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        } else {
            // Default size limit for other files (e.g. 20MB)
            if (size > 20 * 1024 * 1024) {
                throw new IllegalArgumentException("File size exceeds maximum limit of 20MB");
            }
        }
    }

    private AttachmentResponseDTO mapToResponseDTO(Attachment attachment) {
        return AttachmentResponseDTO.builder()
                .id(attachment.getId())
                .url(attachment.getFileUrl())
                .type(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .publicId(attachment.getPublicId())
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }
}
