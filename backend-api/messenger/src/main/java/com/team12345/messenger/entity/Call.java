package com.team12345.messenger.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "calls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "Call.withCaller",
        attributeNodes = { @NamedAttributeNode("caller") }
    ),
    @NamedEntityGraph(
        name = "Call.withReceiver",
        attributeNodes = { @NamedAttributeNode("receiver") }
    ),
    @NamedEntityGraph(
        name = "Call.withParticipants",
        attributeNodes = { @NamedAttributeNode("participants") }
    )
})
public class Call {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id")
    private User caller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @OneToMany(mappedBy = "call", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CallParticipant> participants = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "call_type")
    private CallType callType;

    @Enumerated(EnumType.STRING)
    @Column(name= "status")
    private CallStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}