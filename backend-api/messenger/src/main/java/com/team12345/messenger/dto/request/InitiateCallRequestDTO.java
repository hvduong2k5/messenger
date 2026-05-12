package com.team12345.messenger.dto.request;

import com.team12345.messenger.entity.CallType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InitiateCallRequestDTO {

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotNull(message = "Call type is required")
    private CallType callType;
}
