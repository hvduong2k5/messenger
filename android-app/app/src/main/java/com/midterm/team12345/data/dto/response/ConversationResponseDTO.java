package com.midterm.team12345.data.dto.response;

public class ConversationResponseDTO {
    private Long id;
    private String name;
    private Boolean isGroup;
    private String updatedAt; // Đổi sang String để tránh lỗi parse LocalDateTime
    private String lastMessageContent;
    private String lastMessageCreatedAt; // Đổi sang String
    private Long unreadCount;
    private String avatarUrl;

    public Long getId() { return id; }
    public String getName() { return name; }
    public Boolean getIsGroup() { return isGroup; }
    public String getUpdatedAt() { return updatedAt; }
    public String getLastMessageContent() { return lastMessageContent; }
    public String getLastMessageCreatedAt() { return lastMessageCreatedAt; }
    public Long getUnreadCount() { return unreadCount; }
    public String getAvatarUrl() { return avatarUrl; }
}
