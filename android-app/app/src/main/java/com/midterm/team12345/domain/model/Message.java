package com.midterm.team12345.domain.model;

import com.midterm.team12345.data.local.entity.SyncState;
import com.midterm.team12345.data.local.entity.DeliveryStatus;

public class Message {
    private Long localId;
    private String clientMessageId;
    private Long messageId;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String senderAvatarUrl;
    private String content;
    private String type;
    private SyncState syncState;
    private DeliveryStatus deliveryStatus;
    private Long localCreatedAt;
    private Long serverCreatedAt;
    private Long deletedAt;
    private Long editedAt;
    private Integer retryCount;
    private Long lastRetryAt;

    public Message() {}

    public Message(Long localId, String clientMessageId, Long messageId, Long conversationId, Long senderId, String senderUsername, String senderAvatarUrl, String content, String type, SyncState syncState, DeliveryStatus deliveryStatus, Long localCreatedAt, Long serverCreatedAt, Long deletedAt, Long editedAt, Integer retryCount, Long lastRetryAt) {
        this.localId = localId;
        this.clientMessageId = clientMessageId;
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.senderAvatarUrl = senderAvatarUrl;
        this.content = content;
        this.type = type;
        this.syncState = syncState;
        this.deliveryStatus = deliveryStatus;
        this.localCreatedAt = localCreatedAt;
        this.serverCreatedAt = serverCreatedAt;
        this.deletedAt = deletedAt;
        this.editedAt = editedAt;
        this.retryCount = retryCount;
        this.lastRetryAt = lastRetryAt;
    }

    public Long getLocalId() { return localId; }
    public void setLocalId(Long localId) { this.localId = localId; }

    public String getClientMessageId() { return clientMessageId; }
    public void setClientMessageId(String clientMessageId) { this.clientMessageId = clientMessageId; }

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

    public SyncState getSyncState() { return syncState; }
    public void setSyncState(SyncState syncState) { this.syncState = syncState; }

    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(DeliveryStatus deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public Long getLocalCreatedAt() { return localCreatedAt; }
    public void setLocalCreatedAt(Long localCreatedAt) { this.localCreatedAt = localCreatedAt; }

    public Long getServerCreatedAt() { return serverCreatedAt; }
    public void setServerCreatedAt(Long serverCreatedAt) { this.serverCreatedAt = serverCreatedAt; }

    public Long getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Long deletedAt) { this.deletedAt = deletedAt; }

    public Long getEditedAt() { return editedAt; }
    public void setEditedAt(Long editedAt) { this.editedAt = editedAt; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public Long getLastRetryAt() { return lastRetryAt; }
    public void setLastRetryAt(Long lastRetryAt) { this.lastRetryAt = lastRetryAt; }
}
