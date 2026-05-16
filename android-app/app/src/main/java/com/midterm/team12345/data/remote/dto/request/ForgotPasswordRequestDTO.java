package com.midterm.team12345.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordRequestDTO {
    @SerializedName("email")
    private String email;

    public ForgotPasswordRequestDTO() {}

    public ForgotPasswordRequestDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
