package com.midterm.team12345.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class FriendRequestResponseDTO {
    @SerializedName("senderId")
    private Long senderId;

    @SerializedName("receiverId")
    private Long receiverId;

    @SerializedName("senderUsername")
    private String senderUsername;

    @SerializedName("senderAvatarUrl")
    private String senderAvatarUrl;

    @SerializedName("status")
    private String status;

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
