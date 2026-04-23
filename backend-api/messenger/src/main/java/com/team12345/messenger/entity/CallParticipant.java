package com.team12345.messenger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "call_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallParticipant {

    @EmbeddedId
    private CallParticipantId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("callId")
    @JoinColumn(name = "call_id")
    private Call call;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;
}