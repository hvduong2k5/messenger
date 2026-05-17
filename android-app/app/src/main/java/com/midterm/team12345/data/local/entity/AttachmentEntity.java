package com.midterm.team12345.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "attachments")
public class AttachmentEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    private Long localId;

    @ColumnInfo(name = "id")
    private Long id; // Server ID (nullable until uploaded)

    @ColumnInfo(name = "client_message_id")
    private String clientMessageId;

    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "local_uri")
    private String localUri;

    @ColumnInfo(name = "mime_type")
    private String mimeType;

    @ColumnInfo(name = "file_size")
    private Long fileSize;

    @ColumnInfo(name = "public_id")
    private String publicId;

    @ColumnInfo(name = "uploaded_at")
    private Long uploadedAt;

    @ColumnInfo(name = "ui_state")
    private AttachmentUiState uiState; // UPLOADING, SUCCESS, FAILED

    public AttachmentEntity() {
        this.uiState = AttachmentUiState.UPLOADING;
    }

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientMessageId() {
        return clientMessageId;
    }

    public void setClientMessageId(String clientMessageId) {
        this.clientMessageId = clientMessageId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public Long getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Long uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public AttachmentUiState getUiState() {
        return uiState;
    }

    public void setUiState(AttachmentUiState uiState) {
        this.uiState = uiState;
    }
}
