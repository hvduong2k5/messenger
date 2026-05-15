package com.midterm.team12345.data.dto.response;

import java.time.LocalDateTime;

public class MessageStatusResponseDTO {
    private Long userId;
    private String username;
    private String avatarUrl;
    private String status;
    private LocalDateTime updatedAt;

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

