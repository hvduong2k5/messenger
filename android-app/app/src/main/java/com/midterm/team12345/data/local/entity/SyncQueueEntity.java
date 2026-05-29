package com.midterm.team12345.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sync_queue")
public class SyncQueueEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    @ColumnInfo(name = "operation_type")
    private SyncOperationType operationType; // SEND_MESSAGE, DELETE_MESSAGE, etc.

    @ColumnInfo(name = "payload")
    private String payload; // JSON data representing the request payload

    @ColumnInfo(name = "retry_count")
    private Integer retryCount;

    @ColumnInfo(name = "next_retry_at")
    private Long nextRetryAt;

    @ColumnInfo(name = "created_at")
    private Long createdAt;

    @ColumnInfo(name = "status")
    private SyncStatus status; // PENDING, PROCESSING, FAILED

    public SyncQueueEntity() {
        this.retryCount = 0;
        this.createdAt = System.currentTimeMillis();
        this.nextRetryAt = System.currentTimeMillis();
        this.status = SyncStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SyncOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(SyncOperationType operationType) {
        this.operationType = operationType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Long getNextRetryAt() {
        return nextRetryAt;
    }

    public void setNextRetryAt(Long nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public SyncStatus getStatus() {
        return status;
    }

    public void setStatus(SyncStatus status) {
        this.status = status;
    }
}
