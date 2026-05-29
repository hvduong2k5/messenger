package com.team12345.messenger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponseDTO {
    private Long id;
    private String name;
    private Boolean isGroup;
    private LocalDateTime updatedAt;
    private String lastMessageContent;
    private LocalDateTime lastMessageCreatedAt;
    private Long unreadCount;
    private String avatarUrl;
}
