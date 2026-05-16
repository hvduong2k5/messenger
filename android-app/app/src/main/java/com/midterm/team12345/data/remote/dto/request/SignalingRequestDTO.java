package com.midterm.team12345.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SignalingRequestDTO {

    @SerializedName("callId")
    @NotNull(message = "Call ID is required")
    private Long callId;

    @SerializedName("signalType")
    @NotBlank(message = "Signal type is required")
    @Size(max = 50, message = "Signal type must not exceed 50 characters")
    private String signalType;

    @SerializedName("data")
    @NotBlank(message = "Signal data is required")
    private String data;

    public Long getCallId() {
        return callId;
    }

    public void setCallId(Long callId) {
        this.callId = callId;
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
}
