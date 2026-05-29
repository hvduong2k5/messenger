package com.midterm.team12345.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class UserResponseDTO {
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

    @SerializedName("isOnline")
    private Boolean isOnline;

    @SerializedName("lastSeen")
    private String lastSeen; // Chuyển sang String để tránh crash khi nhận giá trị NULL

    public UserResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean online) { isOnline = online; }

    public String getLastSeen() { return lastSeen; }
    public void setLastSeen(String lastSeen) { this.lastSeen = lastSeen; }
}
