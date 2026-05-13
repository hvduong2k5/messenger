package com.midterm.team12345.data.dto;

import java.util.List;

public class ConversationRequestDTO {
    private String name;
    private Boolean isGroup;
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
