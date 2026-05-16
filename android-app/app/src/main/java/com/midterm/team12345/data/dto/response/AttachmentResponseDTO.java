package com.midterm.team12345.data.dto.response;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

public class AttachmentResponseDTO {
    @SerializedName("id")
    private Long id;

    @SerializedName("url")
    private String url;

    @SerializedName("type")
    private String type;

    @SerializedName("fileSize")
    private Integer fileSize;

    @SerializedName("publicId")
    private String publicId;

    @SerializedName("uploadedAt")
    private LocalDateTime uploadedAt;

    public AttachmentResponseDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
