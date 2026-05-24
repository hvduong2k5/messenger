package com.midterm.team12345.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequestDTO {
    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("oldPassword")
    private String oldPassword;

    @SerializedName("status")
    private String status;

    public UpdateProfileRequestDTO() {}

    public UpdateProfileRequestDTO(String email, String password, String oldPassword, String status) {
        this.email = email;
        this.password = password;
        this.oldPassword = oldPassword;
        this.status = status;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
