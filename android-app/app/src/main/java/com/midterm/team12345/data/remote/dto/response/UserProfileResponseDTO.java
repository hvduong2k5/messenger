package com.midterm.team12345.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponseDTO {
    @SerializedName("id")
    private Long id;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("status")
    private String status;

    @SerializedName("friendshipStatus")
    private FriendshipStatus friendshipStatus;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getStatus() { return status; }
    public FriendshipStatus getFriendshipStatus() { return friendshipStatus; }
}
