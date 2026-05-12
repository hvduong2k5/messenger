package com.team12345.messenger.dto.response;

import com.team12345.messenger.entity.Participant;

import java.util.List;

public record SaveMessageResult(MessageResponseDTO message, List<Participant> participants) {}
