package com.team12345.messenger.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignalingRequestDTO {

    @NotNull(message = "Call ID is required")
    private Long callId;

    @NotBlank(message = "Signal type is required")
    @Size(max = 50, message = "Signal type must not exceed 50 characters")
    private String signalType;

    @NotBlank(message = "Signal data is required")
    private String data;
}
