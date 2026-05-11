package com.team12345.messenger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestResponseDTO {
    private Long senderId;
    private Long receiverId;
    private String senderUsername; // Typically needed when fetching pending requests
    private String senderAvatarUrl;
    private String status;
}