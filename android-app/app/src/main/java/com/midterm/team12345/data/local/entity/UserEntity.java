package com.midterm.team12345.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    @NonNull
    private Long id;

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "avatar_url")
    private String avatarUrl;

    @ColumnInfo(name = "bio")
    private String bio;

    @ColumnInfo(name = "presence_status")
    private String presenceStatus;

    @ColumnInfo(name = "is_online")
    private Boolean isOnline;

    @ColumnInfo(name = "last_seen_at")
    private Long lastSeenAt;

    @ColumnInfo(name = "is_friend")
    private Boolean isFriend;

    @ColumnInfo(name = "friendship_established_at")
    private Long friendshipEstablishedAt;

    @ColumnInfo(name = "is_blocked")
    private Boolean isBlocked;

    public UserEntity() {
        this.isFriend = false;
        this.isBlocked = false;
    }

    @Ignore
    public UserEntity(@NonNull Long id, String username, String email, String avatarUrl, String bio, String presenceStatus, Boolean isOnline, Long lastSeenAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.presenceStatus = presenceStatus;
        this.isOnline = isOnline;
        this.lastSeenAt = lastSeenAt;
        this.isFriend = false;
        this.isBlocked = false;
    }

    @Ignore
    public UserEntity(@NonNull Long id, String username, String email, String avatarUrl, String bio, String presenceStatus, Boolean isOnline, Long lastSeenAt, Boolean isFriend, Long friendshipEstablishedAt, Boolean isBlocked) {
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

    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPresenceStatus() {
        return presenceStatus;
    }

    public void setPresenceStatus(String presenceStatus) {
        this.presenceStatus = presenceStatus;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean online) {
        isOnline = online;
    }

    public Long getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Long lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Boolean getIsFriend() {
        return isFriend != null && isFriend;
    }

    public void setIsFriend(Boolean friend) {
        isFriend = friend;
    }

    public Long getFriendshipEstablishedAt() {
        return friendshipEstablishedAt;
    }

    public void setFriendshipEstablishedAt(Long friendshipEstablishedAt) {
        this.friendshipEstablishedAt = friendshipEstablishedAt;
    }

    public Boolean getIsBlocked() {
        return isBlocked != null && isBlocked;
    }

    public void setIsBlocked(Boolean blocked) {
        isBlocked = blocked;
    }
}
