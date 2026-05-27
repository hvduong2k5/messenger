package com.midterm.team12345.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;

public class MqttMessageDTO {
    // Wrapper action-based fields
    @SerializedName("action")
    private String action;

    @SerializedName("data")
    private MessageResponseDTO data;

    // Direct MessageResponseDTO fields (if published directly)
    @SerializedName(value = "messageId", alternate = {"message_id", "id"})
    private Long messageId;

    @SerializedName(value = "conversationId", alternate = {"conversation_id"})
    private Long conversationId;

    @SerializedName(value = "senderId", alternate = {"sender_id"})
    private Long senderId;

    @SerializedName(value = "senderUsername", alternate = {"sender_username"})
    private String senderUsername;

    @SerializedName(value = "senderAvatarUrl", alternate = {"sender_avatar_url"})
    private String senderAvatarUrl;

    @SerializedName(value = "content", alternate = {"payload"})
    private String content;

    @SerializedName("status")
    private String status;

    @SerializedName(value = "createdAt", alternate = {"created_at", "timestamp"})
    private String createdAt;

    @SerializedName(value = "isDeleted", alternate = {"is_deleted"})
    private Boolean isDeleted;

    @SerializedName(value = "isEdited", alternate = {"is_edited"})
    private Boolean isEdited;

    // Original MqttMessageDTO fields (for backward compatibility)
    @SerializedName("type")
    private String type;

    @SerializedName("sender")
    private String sender;

    public MqttMessageDTO() {}

    public String getType() {
        if (action != null) return action;
        if (type != null) return type;
        return "NEW_MESSAGE";
    }

    public void setType(String type) { this.type = type; }

    public String getPayload() {
        if (content != null) return content;
        if (data != null && data.getContent() != null) return data.getContent();
        return null;
    }

    public void setPayload(String payload) { this.content = payload; }

    public String getSender() {
        if (sender != null) return sender;
        if (senderId != null) return senderId.toString();
        if (data != null && data.getSenderId() != null) return data.getSenderId().toString();
        return null;
    }

    public void setSender(String sender) { this.sender = sender; }

    public Long getSenderId() {
        if (senderId != null) return senderId;
        if (data != null) return data.getSenderId();
        if (sender != null) {
            try {
                return Long.parseLong(sender);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getTimestamp() {
        if (createdAt != null) {
            try {
                return Long.parseLong(createdAt);
            } catch (NumberFormatException e) {
                return parseDateStringToLong(createdAt);
            }
        }
        if (data != null && data.getCreatedAt() != null) return data.getCreatedAt();
        return null;
    }

    public void setTimestamp(Long timestamp) {
        if (timestamp != null) {
            this.createdAt = timestamp.toString();
        } else {
            this.createdAt = null;
        }
    }

    public Long getConversationId() {
        if (conversationId != null) return conversationId;
        if (data != null) return data.getConversationId();
        try {
            String resolvedSender = getSender();
            if (resolvedSender != null) return Long.parseLong(resolvedSender);
        } catch (NumberFormatException ignored) {}
        return null;
    }

    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public MessageResponseDTO getData() { return data; }
    public void setData(MessageResponseDTO data) { this.data = data; }

    public MessageResponseDTO toMessageResponseDTO() {
        if (data != null) {
            return data;
        }
        if (messageId != null) {
            MessageResponseDTO dto = new MessageResponseDTO();
            dto.setMessageId(messageId);
            dto.setConversationId(getConversationId());
            dto.setSenderId(getSenderId());
            dto.setSenderUsername(senderUsername);
            dto.setSenderAvatarUrl(senderAvatarUrl);
            dto.setContent(getPayload());
            dto.setType(type != null ? type : "text");
            dto.setStatus(status != null ? status : "SENT");
            if (createdAt != null) {
                dto.setCreatedAtStr(createdAt);
            } else if (getTimestamp() != null) {
                dto.setCreatedAt(getTimestamp());
            }
            dto.setIsDeleted(isDeleted);
            dto.setIsEdited(isEdited);
            return dto;
        }
        return null;
    }

    private static Long parseDateStringToLong(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                return java.time.LocalDateTime.parse(timeStr)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();
            } else {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                return sdf.parse(timeStr).getTime();
            }
        } catch (Exception e) {
            try {
                return Long.parseLong(timeStr);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
