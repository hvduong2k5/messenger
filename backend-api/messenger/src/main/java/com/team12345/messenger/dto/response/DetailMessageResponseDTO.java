package com.team12345.messenger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailMessageResponseDTO {
    private Long messageId;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String senderAvatarUrl;
    private String content;
    private String type; // Could be deduced from content vs attachments
    private String status; // Sent/Delivered/Read etc. (Derived logic typically)
    private LocalDateTime createdAt;
    private List<AttachmentResponseDTO> attachments;
}