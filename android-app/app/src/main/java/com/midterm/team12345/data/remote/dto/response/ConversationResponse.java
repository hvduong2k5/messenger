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
    private Long updatedAt; // Chuyển về Long để xử lý ở UI (milliseconds)

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

    public ConversationResponse(Long conversationId, String conversationName, String lastMessage, String avatarUrl, Long updatedAt, Integer unreadCount, Boolean isDeleted, Boolean isEdited, Boolean isGroup) {
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

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public Boolean getDeleted() {
        return isDeleted != null && isDeleted;
    }

    public Boolean getEdited() {
        return isEdited != null && isEdited;
    }

    public Boolean getGroup() {
        return isGroup != null && isGroup;
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
