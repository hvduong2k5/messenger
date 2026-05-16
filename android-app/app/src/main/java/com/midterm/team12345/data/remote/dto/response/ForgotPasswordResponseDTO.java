package com.midterm.team12345.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordResponseDTO {
    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private boolean success;

    public ForgotPasswordResponseDTO() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
