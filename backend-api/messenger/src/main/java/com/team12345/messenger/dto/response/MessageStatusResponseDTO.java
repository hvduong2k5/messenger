package com.team12345.messenger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatusResponseDTO {
    private Long userId;
    private String username;
    private String avatarUrl;
    private String status;
    private LocalDateTime updatedAt;
}
