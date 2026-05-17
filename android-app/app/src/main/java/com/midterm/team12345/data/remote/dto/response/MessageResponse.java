package com.midterm.team12345.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;

public class MessageResponse {
    @SerializedName("messageId")
    private Long messageId;

    @SerializedName("conversationId")
    private Long conversationId;

    @SerializedName("senderId")
    private Long senderId;

    @SerializedName("senderUsername")
    private String senderUsername;

    @SerializedName("senderAvatarUrl")
    private String senderAvatarUrl;

    @SerializedName("content")
    private String content;

    @SerializedName("type")
    private String type;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private Long createdAt; // Chuyển về Long để xử lý ở UI

    @SerializedName("isDeleted")
    private Boolean isDeleted;

    @SerializedName("isEdited")
    private Boolean isEdited;

    @SerializedName("attachments")
    private List<AttachmentResponseDTO> attachments;

    public MessageResponse() {}

    public MessageResponse(Long messageId, Long senderId, String content, Long createdAt) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
        this.isDeleted = false;
    }

    // Getters and Setters
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getSenderAvatarUrl() { return senderAvatarUrl; }
    public void setSenderAvatarUrl(String senderAvatarUrl) { this.senderAvatarUrl = senderAvatarUrl; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Boolean getDeleted() { return isDeleted != null && isDeleted; }
    public void setDeleted(Boolean deleted) { isDeleted = deleted; }

    public Boolean getEdited() { return isEdited != null && isEdited; }
    public void setEdited(Boolean edited) { isEdited = edited; }

    public List<AttachmentResponseDTO> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentResponseDTO> attachments) { this.attachments = attachments; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageResponse that = (MessageResponse) o;
        return Objects.equals(messageId, that.messageId) &&
                Objects.equals(content, that.content) &&
                Objects.equals(status, that.status) &&
                Objects.equals(isDeleted, that.isDeleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, content, status, isDeleted);
    }
}
