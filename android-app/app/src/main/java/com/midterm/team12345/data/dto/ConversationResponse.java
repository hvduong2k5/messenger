package com.midterm.team12345.data.dto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.List;

public class ConversationResponse {
    private Long conversationId;
    private String conversationName;
    private String lastMessage;
    private String avatarUrl;
    private Long updatedAt; // epoch millis
    private Integer unreadCount;
    private Boolean isDeleted;
    private Boolean isEdited;
    private List<Object> attachments;

    public ConversationResponse(Long conversationId, String conversationName, String lastMessage, String avatarUrl, Long updatedAt, Integer unreadCount, Boolean isDeleted, Boolean isEdited) {
        this.conversationId = conversationId;
        this.conversationName = conversationName;
        this.lastMessage = lastMessage;
        this.avatarUrl = avatarUrl;
        this.updatedAt = updatedAt;
        this.unreadCount = unreadCount;
        this.isDeleted = isDeleted;
        this.isEdited = isEdited;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public String getConversationName() {
        return conversationName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public Boolean getEdited() {
        return isEdited;
    }

    public List<Object> getAttachments() { return attachments; }

    public String getFormattedTimestamp() {
        if (updatedAt == null || updatedAt == 0L) return "";
        Date date = new Date(updatedAt);
        SimpleDateFormat fmt = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return fmt.format(date);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConversationResponse that = (ConversationResponse) o;
        return Objects.equals(conversationId, that.conversationId) &&
                Objects.equals(conversationName, that.conversationName) &&
                Objects.equals(lastMessage, that.lastMessage) &&
                Objects.equals(avatarUrl, that.avatarUrl) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(unreadCount, that.unreadCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId, conversationName, lastMessage, avatarUrl, updatedAt, unreadCount);
    }
}
