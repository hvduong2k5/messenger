package com.midterm.team12345.data.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class MessageResponse {
    private Long messageId;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String senderAvatarUrl;
    private String content;
    private String type;
    private String status;
    private Long createdAt; // Changed to Long for easier handling in Android if needed, or keep LocalDateTime
    private Boolean isDeleted;
    private Boolean isEdited;
    private List<Object> attachments;

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

    public Boolean getDeleted() { return isDeleted; }
    public void setDeleted(Boolean deleted) { isDeleted = deleted; }

    public Boolean getEdited() { return isEdited; }
    public void setEdited(Boolean edited) { isEdited = edited; }

    public List<Object> getAttachments() { return attachments; }
    public void setAttachments(List<Object> attachments) { this.attachments = attachments; }

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
