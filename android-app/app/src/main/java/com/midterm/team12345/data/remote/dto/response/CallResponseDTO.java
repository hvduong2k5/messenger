package com.midterm.team12345.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import com.midterm.team12345.domain.model.CallStatus;
import com.midterm.team12345.domain.model.CallType;

public class CallResponseDTO {

    @SerializedName("id")
    private Long id;

    @SerializedName("callerId")
    private Long callerId;

    @SerializedName("callerUsername")
    private String callerUsername;

    @SerializedName("receiverId")
    private Long receiverId;

    @SerializedName("receiverUsername")
    private String receiverUsername;

    @SerializedName("callType")
    private CallType callType;

    @SerializedName("status")
    private CallStatus status;

    @SerializedName("startedAt")
    private String startedAt;

    @SerializedName("endedAt")
    private String endedAt;

    @SerializedName("createdAt")
    private String createdAt;

    public CallResponseDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCallerId() {
        return callerId;
    }

    public void setCallerId(Long callerId) {
        this.callerId = callerId;
    }

    public String getCallerUsername() {
        return callerUsername;
    }

    public void setCallerUsername(String callerUsername) {
        this.callerUsername = callerUsername;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public CallType getCallType() {
        return callType;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }

    public CallStatus getStatus() {
        return status;
    }

    public void setStatus(CallStatus status) {
        this.status = status;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(String endedAt) {
        this.endedAt = endedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
