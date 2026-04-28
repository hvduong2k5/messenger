package com.team12345.messenger.repository;

import com.team12345.messenger.entity.Conversation;
import com.team12345.messenger.entity.Participant;
import com.team12345.messenger.entity.ParticipantId;
import com.team12345.messenger.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ConversationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ConversationRepository conversationRepository;

    @Test
    void testFindByUserIdOrderByUpdatedAtDesc() {
        // Create users
        User user1 = User.builder()
                .username("user1")
                .email("user1@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        // Create conversations
        Conversation conversation1 = Conversation.builder()
                .name("Conversation 1")
                .isGroup(false)
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
        Conversation conversation2 = Conversation.builder()
                .name("Conversation 2")
                .isGroup(false)
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(conversation1);
        entityManager.persistAndFlush(conversation2);

        // Create participants
        Participant participant1 = Participant.builder()
                .id(new ParticipantId(conversation1.getId(), user1.getId()))
                .conversation(conversation1)
                .user(user1)
                .build();
        Participant participant2 = Participant.builder()
                .id(new ParticipantId(conversation2.getId(), user1.getId()))
                .conversation(conversation2)
                .user(user1)
                .build();
        Participant participant3 = Participant.builder()
                .id(new ParticipantId(conversation2.getId(), user2.getId()))
                .conversation(conversation2)
                .user(user2)
                .build();
        entityManager.persistAndFlush(participant1);
        entityManager.persistAndFlush(participant2);
        entityManager.persistAndFlush(participant3);

        // Test the query
        List<Conversation> conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(user1.getId());

        // Verify results
        assertThat(conversations).hasSize(2);
        assertThat(conversations.get(0).getId()).isEqualTo(conversation2.getId()); // Most recent first
        assertThat(conversations.get(1).getId()).isEqualTo(conversation1.getId());
    }
}
