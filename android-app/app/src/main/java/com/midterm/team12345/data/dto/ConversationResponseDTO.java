package com.midterm.team12345.data.dto;

import java.time.LocalDateTime;

public class ConversationResponseDTO {
    private Long id;
    private String name;
    private Boolean isGroup;
    private LocalDateTime updatedAt;
    private String lastMessageContent;
    private LocalDateTime lastMessageCreatedAt;
    private Long unreadCount;
    private String avatarUrl;

    public Long getId() { return id; }
    public String getName() { return name; }
    public Boolean getIsGroup() { return isGroup; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getLastMessageContent() { return lastMessageContent; }
    public LocalDateTime getLastMessageCreatedAt() { return lastMessageCreatedAt; }
    public Long getUnreadCount() { return unreadCount; }
    public String getAvatarUrl() { return avatarUrl; }
}
