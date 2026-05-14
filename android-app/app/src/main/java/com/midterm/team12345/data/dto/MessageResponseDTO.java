package com.midterm.team12345.data.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MessageResponseDTO {
    private Long messageId;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String senderAvatarUrl;
    private String content;
    private String type;
    private String status;
    private LocalDateTime createdAt;
    private Boolean isDeleted;
    private Boolean isEdited;
    private List<Object> attachments;

    public Long getMessageId() { return messageId; }
    public Long getConversationId() { return conversationId; }
    public Long getSenderId() { return senderId; }
    public String getSenderUsername() { return senderUsername; }
    public String getSenderAvatarUrl() { return senderAvatarUrl; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Boolean getIsDeleted() { return isDeleted; }
    public Boolean getIsEdited() { return isEdited; }
    public List<Object> getAttachments() { return attachments; }
}
