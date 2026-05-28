package com.midterm.team12345.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;

public class MessageResponseDTO {
    @SerializedName(value = "messageId", alternate = {"message_id", "id"})
    private Long messageId;

    @SerializedName(value = "clientMessageId", alternate = {"client_message_id"})
    private String clientMessageId;

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
    private List<AttachmentResponseDTO> attachments;

    public MessageResponseDTO() {}

    public MessageResponseDTO(Long messageId, Long senderId, String content, Long createdAt) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        setCreatedAt(createdAt);
        this.isDeleted = false;
        this.isEdited = false;
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

    // Specialized Getter & Setter for UI time processing
    public Long getCreatedAt() {
        if (createdAt == null || createdAt.isEmpty()) return null;
        try {
            return Long.parseLong(createdAt);
        } catch (NumberFormatException e) {
            return parseDateStringToLong(createdAt);
        }
    }

    public void setCreatedAt(Long time) {
        this.createdAt = time != null ? String.valueOf(time) : null;
    }

    public String getCreatedAtStr() { return createdAt; }
    public void setCreatedAtStr(String createdAtStr) { this.createdAt = createdAtStr; }

    // Dual support for both DTO and UI model methods
    public Boolean getIsDeleted() { return isDeleted != null && isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public Boolean getIsEdited() { return isEdited != null && isEdited; }
    public void setIsEdited(Boolean isEdited) { this.isEdited = isEdited; }

    public Boolean getDeleted() { return isDeleted != null && isDeleted; }
    public void setDeleted(Boolean deleted) { isDeleted = deleted; }

    public Boolean getEdited() { return isEdited != null && isEdited; }
    public void setEdited(Boolean edited) { isEdited = edited; }

    public List<AttachmentResponseDTO> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentResponseDTO> attachments) { this.attachments = attachments; }

    public String getClientMessageId() { return clientMessageId; }
    public void setClientMessageId(String clientMessageId) { this.clientMessageId = clientMessageId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageResponseDTO that = (MessageResponseDTO) o;
        return Objects.equals(messageId, that.messageId) &&
                Objects.equals(content, that.content) &&
                Objects.equals(status, that.status) &&
                Objects.equals(isDeleted, that.isDeleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, content, status, isDeleted);
    }

    private static Long parseDateStringToLong(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                return java.time.Instant.parse(timeStr).toEpochMilli();
            } catch (Exception ignored) {}
            try {
                return java.time.OffsetDateTime.parse(timeStr).toInstant().toEpochMilli();
            } catch (Exception ignored) {}
            try {
                return java.time.ZonedDateTime.parse(timeStr).toInstant().toEpochMilli();
            } catch (Exception ignored) {}
            try {
                return java.time.LocalDateTime.parse(timeStr)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();
            } catch (Exception ignored) {}
        }
        
        String[] formats = {
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ",
            "yyyy-MM-dd'T'HH:mm:ssZZZZZ",
            "yyyy-MM-dd'T'HH:mm:ss"
        };
        for (String format : formats) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format, java.util.Locale.getDefault());
                if (format.endsWith("'Z'")) {
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                }
                return sdf.parse(timeStr).getTime();
            } catch (Exception ignored) {}
        }
        
        try {
            return Long.parseLong(timeStr);
        } catch (NumberFormatException ignored) {}
        
        return null;
    }
}
