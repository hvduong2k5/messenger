package com.team12345.messenger.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;

@Entity
@Table(name = "participants")
// Bọc tất cả bên trong @NamedEntityGraphs
@NamedEntityGraphs({

// Graph 1: Như bạn đã viết (chỉ lấy Conversation)
@NamedEntityGraph(
    name = "Participant.conversation",
    attributeNodes = {
        @NamedAttributeNode("conversation")
    }
),

// Graph 2: Viết tiếp cái mới (Ví dụ: Chỉ lấy User)
@NamedEntityGraph(
    name = "Participant.user",
    attributeNodes = {
        @NamedAttributeNode("user")
    }
),

// Graph 3: Kết hợp lấy cả Conversation và User (Rất hay dùng)
@NamedEntityGraph(
    name = "Participant.fullDetail",
    attributeNodes = {
        @NamedAttributeNode("conversation"),
        @NamedAttributeNode("user")
    }
)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participant {

    @EmbeddedId
    private ParticipantId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ParticipantRole role = ParticipantRole.member;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;
}