package com.team12345.messenger.service;

import com.team12345.messenger.dto.response.AttachmentResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {
    AttachmentResponseDTO uploadAttachment(MultipartFile file, Long userId);
    AttachmentResponseDTO getAttachmentById(Long id);
    List<AttachmentResponseDTO> getAttachmentsByMessageId(Long messageId);
    void deleteAttachment(Long id, Long userId);
}
