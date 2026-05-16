package com.midterm.team12345.data.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ConversationRequestDTO {
    @SerializedName("name")
    private String name;

    @SerializedName("isGroup")
    private Boolean isGroup;

    @SerializedName("participantIds")
    private List<Long> participantIds;

    public ConversationRequestDTO(String name, Boolean isGroup, List<Long> participantIds) {
        this.name = name;
        this.isGroup = isGroup;
        this.participantIds = participantIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(Boolean isGroup) {
        this.isGroup = isGroup;
    }

    public List<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<Long> participantIds) {
        this.participantIds = participantIds;
    }
}
