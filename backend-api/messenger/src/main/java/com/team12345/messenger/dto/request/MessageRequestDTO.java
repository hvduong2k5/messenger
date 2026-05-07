package com.team12345.messenger.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequestDTO {
    private Long senderId;
    private Long conversationId;
    private String content;
    private String clientMessageId;
    private List<MultipartFile> files; // For attachments
}