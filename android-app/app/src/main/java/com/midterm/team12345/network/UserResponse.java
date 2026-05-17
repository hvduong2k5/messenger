package com.midterm.team12345.network;

public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String status;

    public UserResponse() {}

    public UserResponse(Long id, String username, String status) {
        this.id = id;
        this.username = username;
        this.status = status;
    }

    // Getters and Setters
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
}
