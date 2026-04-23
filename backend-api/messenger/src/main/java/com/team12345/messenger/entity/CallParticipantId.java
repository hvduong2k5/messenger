package com.team12345.messenger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallParticipantId implements Serializable {

    @Column(name = "call_id")
    private Long callId;

    @Column(name = "user_id")
    private Long userId;
}