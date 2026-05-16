package com.midterm.team12345.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.List;

public class ConversationResponse implements Serializable {
    @SerializedName("conversationId")
    private Long conversationId;

    @SerializedName("conversationName")
    private String conversationName;

    @SerializedName("lastMessage")
    private String lastMessage;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("unreadCount")
    private Integer unreadCount;

    @SerializedName("isDeleted")
    private Boolean isDeleted;

    @SerializedName("isEdited")
    private Boolean isEdited;

    @SerializedName("isGroup")
    private Boolean isGroup;

    @SerializedName("attachments")
    private List<Object> attachments;

    public ConversationResponse(Long conversationId, String conversationName, String lastMessage, String avatarUrl, String updatedAt, Integer unreadCount, Boolean isDeleted, Boolean isEdited, Boolean isGroup) {
        this.conversationId = conversationId;
        this.conversationName = conversationName;
        this.lastMessage = lastMessage;
        this.avatarUrl = avatarUrl;
        this.updatedAt = updatedAt;
        this.unreadCount = unreadCount;
        this.isDeleted = isDeleted;
        this.isEdited = isEdited;
        this.isGroup = isGroup;
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

    public String getUpdatedAt() {
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

    public Boolean getGroup() {
        return isGroup != null && isGroup;
    }

    public List<Object> getAttachments() { return attachments; }

    public String getFormattedTimestamp() {
        if (updatedAt == null || updatedAt.isEmpty()) return "";
        try {
            long ts;
            try {
                ts = Long.parseLong(updatedAt);
            } catch (NumberFormatException e) {
                // If it's not a long, it might be ISO string. 
                // For simplicity in this helper, we try to handle common case.
                // In a real app, this should use the same logic as Repository.
                return updatedAt; 
            }
            Date date = new Date(ts);
            SimpleDateFormat fmt = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return fmt.format(date);
        } catch (Exception e) {
            return updatedAt;
        }
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
