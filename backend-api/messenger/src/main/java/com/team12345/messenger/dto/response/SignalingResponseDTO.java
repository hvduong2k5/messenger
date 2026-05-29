package com.team12345.messenger.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SignalingResponseDTO {

    private Long id;
    private Long senderId;
    private String senderUsername;
    private String signalType;
    private String data;
    private LocalDateTime createdAt;
}
