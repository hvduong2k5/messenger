package com.midterm.team12345.data.dto.response;

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
    public String getName() { return name; }
    public Boolean getIsGroup() { return isGroup; }
    public String getUpdatedAt() { return updatedAt; }
    public String getLastMessageContent() { return lastMessageContent; }
    public String getLastMessageCreatedAt() { return lastMessageCreatedAt; }
    public Long getUnreadCount() { return unreadCount; }
    public String getAvatarUrl() { return avatarUrl; }
}
