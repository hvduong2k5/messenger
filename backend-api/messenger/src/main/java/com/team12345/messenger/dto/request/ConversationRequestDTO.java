package com.team12345.messenger.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRequestDTO {

    private String name;

    @NotNull(message = "isGroup cannot be null")
    private Boolean isGroup;

    @NotEmpty(message = "participantIds must not be empty")
    private List<Long> participantIds;
}
