package com.midterm.team12345.domain.model;

public class User {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private String presenceStatus;
    private Boolean isOnline;
    private Long lastSeenAt;
    private Boolean isFriend;
    private Long friendshipEstablishedAt;
    private Boolean isBlocked;

    public User() {}

    public User(Long id, String username, String email, String avatarUrl, String bio, String presenceStatus, Boolean isOnline, Long lastSeenAt, Boolean isFriend, Long friendshipEstablishedAt, Boolean isBlocked) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.presenceStatus = presenceStatus;
        this.isOnline = isOnline;
        this.lastSeenAt = lastSeenAt;
        this.isFriend = isFriend;
        this.friendshipEstablishedAt = friendshipEstablishedAt;
        this.isBlocked = isBlocked;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPresenceStatus() { return presenceStatus; }
    public void setPresenceStatus(String presenceStatus) { this.presenceStatus = presenceStatus; }

    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean online) { isOnline = online; }

    public Long getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Long lastSeenAt) { this.lastSeenAt = lastSeenAt; }

    public Boolean getIsFriend() { return isFriend; }
    public void setIsFriend(Boolean friend) { isFriend = friend; }

    public Long getFriendshipEstablishedAt() { return friendshipEstablishedAt; }
    public void setFriendshipEstablishedAt(Long friendshipEstablishedAt) { this.friendshipEstablishedAt = friendshipEstablishedAt; }

    public Boolean getIsBlocked() { return isBlocked; }
    public void setIsBlocked(Boolean blocked) { isBlocked = blocked; }
}
