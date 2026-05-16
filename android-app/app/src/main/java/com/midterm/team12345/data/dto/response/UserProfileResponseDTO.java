package com.midterm.team12345.data.dto.response;

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

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getStatus() { return status; }
}
