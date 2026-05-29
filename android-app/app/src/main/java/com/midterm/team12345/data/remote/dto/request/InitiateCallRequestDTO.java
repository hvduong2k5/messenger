package com.midterm.team12345.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import javax.validation.constraints.NotNull;
import com.midterm.team12345.domain.model.CallType;

public class InitiateCallRequestDTO {

    @SerializedName("receiverId")
    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @SerializedName("callType")
    @NotNull(message = "Call type is required")
    private CallType callType;

    public InitiateCallRequestDTO() {}

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public CallType getCallType() {
        return callType;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }
}
