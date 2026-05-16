package com.midterm.team12345.data.dto.request;

import com.google.gson.annotations.SerializedName;

public class ConversationUpdateDTO {
    @SerializedName("name")
    private String name;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    public ConversationUpdateDTO(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
