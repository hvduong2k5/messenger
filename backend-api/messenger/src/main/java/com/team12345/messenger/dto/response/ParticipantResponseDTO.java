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
public class ParticipantResponseDTO {
    private Long userId;
    private String username;
    private String avatarUrl;
    private String role;
    private LocalDateTime joinedAt;
}
