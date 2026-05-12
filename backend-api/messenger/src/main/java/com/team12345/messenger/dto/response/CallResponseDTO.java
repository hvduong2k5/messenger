package com.team12345.messenger.dto.response;

import com.team12345.messenger.entity.CallStatus;
import com.team12345.messenger.entity.CallType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CallResponseDTO {

    private Long id;
    private Long callerId;
    private String callerUsername;
    private Long receiverId;
    private String receiverUsername;
    private CallType callType;
    private CallStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;
}
