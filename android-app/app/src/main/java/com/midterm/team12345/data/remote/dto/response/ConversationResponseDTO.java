package com.midterm.team12345.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class ConversationResponseDTO {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("isGroup")
    private Boolean isGroup;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("lastMessageContent")
    private String lastMessageContent;

    @SerializedName("lastMessageCreatedAt")
    private String lastMessageCreatedAt;

    @SerializedName("unreadCount")
    private Long unreadCount;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getIsGroup() { return isGroup; }
    public void setIsGroup(Boolean isGroup) { this.isGroup = isGroup; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getLastMessageContent() { return lastMessageContent; }
    public void setLastMessageContent(String lastMessageContent) { this.lastMessageContent = lastMessageContent; }

    public String getLastMessageCreatedAt() { return lastMessageCreatedAt; }
    public void setLastMessageCreatedAt(String lastMessageCreatedAt) { this.lastMessageCreatedAt = lastMessageCreatedAt; }

    public Long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
