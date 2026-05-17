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

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("isDeleted")
    private Boolean isDeleted;

    @SerializedName("isEdited")
    private Boolean isEdited;

    // Original MqttMessageDTO fields (for backward compatibility)
    @SerializedName("type")
    private String type;

    @SerializedName("payload")
    private String payload;

    @SerializedName("sender")
    private String sender;

    @SerializedName("timestamp")
    private Long timestamp;

    public MqttMessageDTO() {}

    public String getType() {
        if (action != null) return action;
        if (type != null) return type;
        return "NEW_MESSAGE";
    }

    public void setType(String type) { this.type = type; }

    public String getPayload() {
        if (payload != null) return payload;
        if (content != null) return content;
        if (data != null && data.getContent() != null) return data.getContent();
        return null;
    }

    public void setPayload(String payload) { this.payload = payload; }

    public String getSender() {
        if (sender != null) return sender;
        if (senderId != null) return senderId.toString();
        if (data != null && data.getSenderId() != null) return data.getSenderId().toString();
        return null;
    }

    public void setSender(String sender) { this.sender = sender; }

    public Long getTimestamp() {
        if (timestamp != null) return timestamp;
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

    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public Long getConversationId() {
        if (conversationId != null) return conversationId;
        if (data != null) return data.getConversationId();
        try {
            if (sender != null) return Long.parseLong(sender);
            if (senderId != null) return senderId;
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
            dto.setConversationId(conversationId);
            dto.setSenderId(senderId);
            dto.setSenderUsername(senderUsername);
            dto.setSenderAvatarUrl(senderAvatarUrl);
            dto.setContent(content);
            dto.setType(type != null ? type : "text");
            dto.setStatus(status != null ? status : "SENT");
            dto.setCreatedAtStr(createdAt);
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
