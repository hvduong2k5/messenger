package com.team12345.messenger.repository;

import com.team12345.messenger.entity.Conversation;
import com.team12345.messenger.entity.Participant;
import com.team12345.messenger.entity.ParticipantId;
import com.team12345.messenger.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    void testFindConversationsByUserId() {
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
        Conversation conversation3 = Conversation.builder()
                .name("Conversation 3")
                .isGroup(false)
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();
        entityManager.persistAndFlush(conversation1);
        entityManager.persistAndFlush(conversation2);
        entityManager.persistAndFlush(conversation3);

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
                .id(new ParticipantId(conversation3.getId(), user1.getId()))
                .conversation(conversation3)
                .user(user1)
                .build();
        Participant participant4 = Participant.builder()
                .id(new ParticipantId(conversation2.getId(), user2.getId()))
                .conversation(conversation2)
                .user(user2)
                .build();
        entityManager.persistAndFlush(participant1);
        entityManager.persistAndFlush(participant2);
        entityManager.persistAndFlush(participant3);
        entityManager.persistAndFlush(participant4);

        // Test the query with pagination
        Pageable pageable = PageRequest.of(0, 10);
        Page<Conversation> conversationPage = conversationRepository.findConversationsByUserId(user1.getId(), pageable);

        // Verify results
        assertThat(conversationPage.getContent()).hasSize(3);
        assertThat(conversationPage.getTotalElements()).isEqualTo(3);
        assertThat(conversationPage.getContent().get(0).getId()).isEqualTo(conversation2.getId()); // Most recent first
        assertThat(conversationPage.getContent().get(1).getId()).isEqualTo(conversation3.getId());
        assertThat(conversationPage.getContent().get(2).getId()).isEqualTo(conversation1.getId());
    }

    @Test
    void testFindConversationsByUserIdWithPagination() {
        // Create users
        User user1 = User.builder()
                .username("user1")
                .email("user1@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(user1);

        // Create multiple conversations
        for (int i = 1; i <= 5; i++) {
            Conversation conversation = Conversation.builder()
                    .name("Conversation " + i)
                    .isGroup(false)
                    .updatedAt(LocalDateTime.now().minusDays(6 - i)) // Different dates for ordering
                    .build();
            entityManager.persistAndFlush(conversation);

            Participant participant = Participant.builder()
                    .id(new ParticipantId(conversation.getId(), user1.getId()))
                    .conversation(conversation)
                    .user(user1)
                    .build();
            entityManager.persistAndFlush(participant);
        }

        // Test pagination - page 0 with size 2
        Pageable pageable = PageRequest.of(0, 2);
        Page<Conversation> conversationPage = conversationRepository.findConversationsByUserId(user1.getId(), pageable);

        assertThat(conversationPage.getContent()).hasSize(2);
        assertThat(conversationPage.getTotalElements()).isEqualTo(5);
        assertThat(conversationPage.getTotalPages()).isEqualTo(3);
        assertThat(conversationPage.hasNext()).isTrue();

        // Test pagination - page 1 with size 2
        pageable = PageRequest.of(1, 2);
        conversationPage = conversationRepository.findConversationsByUserId(user1.getId(), pageable);

        assertThat(conversationPage.getContent()).hasSize(2);
        assertThat(conversationPage.getTotalElements()).isEqualTo(5);
        assertThat(conversationPage.hasNext()).isTrue();
        assertThat(conversationPage.hasPrevious()).isTrue();
    }
}
