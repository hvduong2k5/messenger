package com.midterm.team12345.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ResetPasswordRequestDTO {

    @SerializedName("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @SerializedName("otp")
    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;

    @SerializedName("newPassword")
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    private String newPassword;

    public ResetPasswordRequestDTO() {}

    public ResetPasswordRequestDTO(String email, String otp, String newPassword) {
        this.email = email;
        this.otp = otp;
        this.newPassword = newPassword;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
