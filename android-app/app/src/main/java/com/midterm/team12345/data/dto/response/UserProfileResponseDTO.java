package com.midterm.team12345.data.dto.response;

public class UserProfileResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String status;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getStatus() { return status; }
}
