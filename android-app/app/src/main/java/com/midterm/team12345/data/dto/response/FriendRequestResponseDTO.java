package com.midterm.team12345.data.dto.response;

public class FriendRequestResponseDTO {
    private Long senderId;
    private Long receiverId;
    private String senderUsername; // Typically needed when fetching pending requests
    private String senderAvatarUrl;
    private String status;

    public Long getSenderId() {
        return senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getSenderAvatarUrl() {
        return senderAvatarUrl;
    }

    public String getStatus() {
        return status;
    }
}

