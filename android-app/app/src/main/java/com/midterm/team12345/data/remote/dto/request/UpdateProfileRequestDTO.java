package com.midterm.team12345.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequestDTO {
    @SerializedName("status")
    private String status;

    public UpdateProfileRequestDTO() {}

    public UpdateProfileRequestDTO(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
