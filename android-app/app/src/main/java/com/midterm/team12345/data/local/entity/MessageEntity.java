package com.midterm.team12345.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "messages",
    indices = {
        @Index(value = "conversation_id"),
        @Index(value = "client_message_id", unique = true),
        @Index(value = "message_id", unique = true)
    }
)
public class MessageEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    private Long localId;

    @NonNull
    @ColumnInfo(name = "client_message_id")
    private String clientMessageId;

    @ColumnInfo(name = "message_id")
    private Long messageId; // Server message ID (nullable until synced)

    @ColumnInfo(name = "conversation_id")
    private Long conversationId;

    @ColumnInfo(name = "sender_id")
    private Long senderId;

    @ColumnInfo(name = "sender_username")
    private String senderUsername;

    @ColumnInfo(name = "sender_avatar_url")
    private String senderAvatarUrl;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "type")
    private String type; // e.g. TEXT, IMAGE, FILE, CALL

    @ColumnInfo(name = "sync_state")
    private SyncState syncState; // PENDING, SENT, FAILED

    @ColumnInfo(name = "delivery_status")
    private DeliveryStatus deliveryStatus; // PENDING, SENT, DELIVERED, READ

    @ColumnInfo(name = "local_created_at")
    private Long localCreatedAt;

    @ColumnInfo(name = "server_created_at")
    private Long serverCreatedAt;

    @ColumnInfo(name = "deleted_at")
    private Long deletedAt;

    @ColumnInfo(name = "edited_at")
    private Long editedAt;

    @ColumnInfo(name = "retry_count")
    private Integer retryCount;

    @ColumnInfo(name = "last_retry_at")
    private Long lastRetryAt;

    public MessageEntity() {
        this.syncState = SyncState.PENDING;
        this.deliveryStatus = DeliveryStatus.PENDING;
        this.retryCount = 0;
    }

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    @NonNull
    public String getClientMessageId() {
        return clientMessageId;
    }

    public void setClientMessageId(@NonNull String clientMessageId) {
        this.clientMessageId = clientMessageId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderAvatarUrl() {
        return senderAvatarUrl;
    }

    public void setSenderAvatarUrl(String senderAvatarUrl) {
        this.senderAvatarUrl = senderAvatarUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SyncState getSyncState() {
        return syncState;
    }

    public void setSyncState(SyncState syncState) {
        this.syncState = syncState;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public Long getLocalCreatedAt() {
        return localCreatedAt;
    }

    public void setLocalCreatedAt(Long localCreatedAt) {
        this.localCreatedAt = localCreatedAt;
    }

    public Long getServerCreatedAt() {
        return serverCreatedAt;
    }

    public void setServerCreatedAt(Long serverCreatedAt) {
        this.serverCreatedAt = serverCreatedAt;
    }

    public Long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Long deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(Long editedAt) {
        this.editedAt = editedAt;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Long getLastRetryAt() {
        return lastRetryAt;
    }

    public void setLastRetryAt(Long lastRetryAt) {
        this.lastRetryAt = lastRetryAt;
    }
}
