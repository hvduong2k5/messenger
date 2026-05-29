package com.team12345.messenger.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDTO {
    
    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
    private String password;

    private String oldPassword;

    @Size(max = 50)
    private String status;
}