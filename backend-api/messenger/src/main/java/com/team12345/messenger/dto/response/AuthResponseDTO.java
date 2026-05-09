package com.team12345.messenger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String accessToken;
    private String tokenType = "Bearer";
    private UserResponseDTO user;

    public AuthResponseDTO(String accessToken, UserResponseDTO user) {
        this.accessToken = accessToken;
        this.user = user;
    }
}
