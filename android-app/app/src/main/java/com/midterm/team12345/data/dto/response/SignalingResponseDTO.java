package com.midterm.team12345.data.dto.response;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

public class SignalingResponseDTO {

    @SerializedName("id")
    private Long id;

    @SerializedName("senderId")
    private Long senderId;

    @SerializedName("senderUsername")
    private String senderUsername;

    @SerializedName("signalType")
    private String signalType;

    @SerializedName("data")
    private String data;

    @SerializedName("createdAt")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSignalType() {
        return signalType;
    }

    public void setSignalType(String signalType) {
        this.signalType = signalType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
