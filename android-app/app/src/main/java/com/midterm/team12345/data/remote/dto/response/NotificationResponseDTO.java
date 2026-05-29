package com.midterm.team12345.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

public class NotificationResponseDTO {
    @SerializedName("id")
    private Long id;

    @SerializedName("type")
    private String type;

    @SerializedName("content")
    private String content;

    @SerializedName("isSeen")
    private Boolean isSeen;

    @SerializedName("createdAt")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsSeen() {
        return isSeen;
    }

    public void setIsSeen(Boolean seen) {
        isSeen = seen;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
