package com.midterm.team12345.domain.model;

public class Conversation {
    private Long id;
    private String name;
    private Boolean isGroup;
    private String avatarUrl;
    private Long updatedAt;
    private String lastMessageContent;
    private Long lastMessageSenderId;
    private Long lastMessageCreatedAt;
    private Integer unreadCount;

    public Conversation() {}

    public Conversation(Long id, String name, Boolean isGroup, String avatarUrl, Long updatedAt, String lastMessageContent, Long lastMessageSenderId, Long lastMessageCreatedAt, Integer unreadCount) {
        this.id = id;
        this.name = name;
        this.isGroup = isGroup;
        this.avatarUrl = avatarUrl;
        this.updatedAt = updatedAt;
        this.lastMessageContent = lastMessageContent;
        this.lastMessageSenderId = lastMessageSenderId;
        this.lastMessageCreatedAt = lastMessageCreatedAt;
        this.unreadCount = unreadCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getIsGroup() { return isGroup; }
    public void setIsGroup(Boolean group) { isGroup = group; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    public String getLastMessageContent() { return lastMessageContent; }
    public void setLastMessageContent(String lastMessageContent) { this.lastMessageContent = lastMessageContent; }

    public Long getLastMessageSenderId() { return lastMessageSenderId; }
    public void setLastMessageSenderId(Long lastMessageSenderId) { this.lastMessageSenderId = lastMessageSenderId; }

    public Long getLastMessageCreatedAt() { return lastMessageCreatedAt; }
    public void setLastMessageCreatedAt(Long lastMessageCreatedAt) { this.lastMessageCreatedAt = lastMessageCreatedAt; }

    public Integer getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }
}
