package com.midterm.team12345.data.dto.response;

import java.time.LocalDateTime;

public class NotificationResponseDTO {
    private Long id;
    private String type;
    private String content;
    private Boolean isSeen;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public Boolean getIsSeen() {
        return isSeen;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

