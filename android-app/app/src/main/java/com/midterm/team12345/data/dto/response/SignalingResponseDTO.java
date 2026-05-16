package com.midterm.team12345.data.dto.response;

import java.time.LocalDateTime;

public class SignalingResponseDTO {

    private Long id;
    private Long senderId;
    private String senderUsername;
    private String signalType;
    private String data;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getSignalType() {
        return signalType;
    }

    public String getData() {
        return data;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

