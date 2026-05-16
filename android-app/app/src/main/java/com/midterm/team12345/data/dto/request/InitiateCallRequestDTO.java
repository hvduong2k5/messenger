package com.midterm.team12345.data.dto.request;

import javax.validation.constraints.NotNull;
import com.midterm.team12345.data.dto.CallType;

public class InitiateCallRequestDTO {

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotNull(message = "Call type is required")
    private CallType callType;

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

