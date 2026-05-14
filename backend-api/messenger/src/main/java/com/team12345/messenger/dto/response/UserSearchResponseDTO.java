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
public class UserSearchResponseDTO {
    private Long id;
    private String username;
    private String avatarUrl;
    private String friendshipStatus;
    private Boolean isOnline;
    private LocalDateTime lastSeen;
}
