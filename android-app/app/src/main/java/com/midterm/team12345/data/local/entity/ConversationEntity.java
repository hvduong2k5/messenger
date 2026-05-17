package com.midterm.team12345.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "conversations")
public class ConversationEntity {

    @PrimaryKey
    @NonNull
    private Long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "is_group")
    private Boolean isGroup;

    @ColumnInfo(name = "avatar_url")
    private String avatarUrl;

    @ColumnInfo(name = "updated_at")
    private Long updatedAt;

    @ColumnInfo(name = "last_message_content")
    private String lastMessageContent;

    @ColumnInfo(name = "last_message_sender_id")
    private Long lastMessageSenderId;

    @ColumnInfo(name = "last_message_created_at")
    private Long lastMessageCreatedAt;

    @ColumnInfo(name = "unread_count")
    private Integer unreadCount;

    public ConversationEntity() {}

    @Ignore
    public ConversationEntity(@NonNull Long id, String name, Boolean isGroup, String avatarUrl, Long updatedAt, String lastMessageContent, Long lastMessageSenderId, Long lastMessageCreatedAt, Integer unreadCount) {
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

    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(Boolean group) {
        isGroup = group;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public Long getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(Long lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public Long getLastMessageCreatedAt() {
        return lastMessageCreatedAt;
    }

    public void setLastMessageCreatedAt(Long lastMessageCreatedAt) {
        this.lastMessageCreatedAt = lastMessageCreatedAt;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
}
