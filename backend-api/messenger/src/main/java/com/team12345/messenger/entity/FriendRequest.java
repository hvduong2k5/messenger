package com.team12345.messenger.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friend_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequest {

    @EmbeddedId
    private FriendRequestId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("senderId")
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("receiverId")
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FriendRequestStatus status;
}