package com.midterm.team12345.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class AuthResponseDTO {
    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("tokenType")
    private String tokenType;

    @SerializedName("user")
    private UserResponseDTO user;

    public AuthResponseDTO() {}

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public UserResponseDTO getUser() { return user; }
    public void setUser(UserResponseDTO user) { this.user = user; }
}
