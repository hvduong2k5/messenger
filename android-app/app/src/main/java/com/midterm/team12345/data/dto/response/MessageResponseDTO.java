package com.midterm.team12345.data.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MessageResponseDTO {
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
    private String createdAt;

    @SerializedName("isDeleted")
    private Boolean isDeleted;

    @SerializedName("isEdited")
    private Boolean isEdited;

    @SerializedName("attachments")
    private List<Object> attachments;

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

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public Boolean getIsEdited() { return isEdited; }
    public void setIsEdited(Boolean isEdited) { this.isEdited = isEdited; }

    public List<Object> getAttachments() { return attachments; }
    public void setAttachments(List<Object> attachments) { this.attachments = attachments; }
}
