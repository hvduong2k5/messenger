package com.midterm.team12345.data.dto.response;

import java.time.LocalDateTime;

public class UserSearchResponseDTO {
    private Long id;
    private String username;
    private String avatarUrl;
    private String friendshipStatus;
    private Boolean isOnline;
    private LocalDateTime lastSeen;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getFriendshipStatus() {
        return friendshipStatus;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
}

