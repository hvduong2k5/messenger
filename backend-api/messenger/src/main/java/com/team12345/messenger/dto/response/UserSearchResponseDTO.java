package com.team12345.messenger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponseDTO {
    private Long id;
    private String username;
    private String avatarUrl;
    private String friendshipStatus; // FRIEND, STRANGER, SENDER_PENDING, RECEIVER_PENDING
}

