package com.midterm.team12345.data.dto.response;

public class AuthResponseDTO {
    private String accessToken;
    private String tokenType;
    private UserDTO user;

    public AuthResponseDTO() {}

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
}
