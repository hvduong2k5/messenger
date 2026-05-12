package com.team12345.messenger.repository;

import com.team12345.messenger.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.id.new_generator_mappings=true"
})
class ConversationRepositoryTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private ConversationRepository conversationRepository;

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    private User persistUser(String username, String email) {
        User user = User.builder()
                .username(username).email(email)
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .isOnline(false) // Required non-null field
                .build();
        return entityManager.persistAndFlush(user);
    }

    private Conversation persistConversation(String name, LocalDateTime updatedAt) {
        Conversation c = Conversation.builder().name(name).isGroup(false).build();
        c = conversationRepository.saveAndFlush(c);
        
        // Use native query to explicitly bypass JPA Auditing and enforce the exact updatedAt timestamp we want
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE conversations SET updated_at = :date WHERE id = :id")
                .setParameter("date", updatedAt)
                .setParameter("id", c.getId())
                .executeUpdate();
                
        entityManager.refresh(c);
        return c;
    }

    private void persistParticipant(Conversation c, User u) {
        Participant p = Participant.builder()
                .id(new ParticipantId(c.getId(), u.getId()))
                .conversation(c).user(u).build();
        entityManager.persistAndFlush(p);
    }

    @Test
    void testFindConversationsByUserId() {
        User user1 = persistUser("user1_testFindConversationsByUserId", "user1_testFindConversationsByUserId@example.com");
        User user2 = persistUser("user2_testFindConversationsByUserId", "user2_testFindConversationsByUserId@example.com");

        LocalDateTime base = LocalDateTime.now();
        Conversation c1 = persistConversation("Conversation 1", base.minusMinutes(2));
        Conversation c2 = persistConversation("Conversation 2", base);              // newest
        Conversation c3 = persistConversation("Conversation 3", base.minusMinutes(1));

        persistParticipant(c1, user1);
        persistParticipant(c2, user1);
        persistParticipant(c3, user1);
        persistParticipant(c2, user2);

        Page<Conversation> page = conversationRepository.findConversationsByUserId(user1.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(3);
        
        List<Conversation> conversations = page.getContent();
        
        // The expected order is by updatedAt DESC, so c2 (newest) > c3 > c1 (oldest)
        assertThat(conversations.get(0).getId()).isEqualTo(c2.getId());
        assertThat(conversations.get(1).getId()).isEqualTo(c3.getId());
        assertThat(conversations.get(2).getId()).isEqualTo(c1.getId());
    }

    @Test
    void testFindConversationsByUserIdWithPagination() {
        User user1 = persistUser("user1_testFindConversationsByUserIdWithPagination", "user1_testFindConversationsByUserIdWithPagination@example.com");

        LocalDateTime base = LocalDateTime.now();
        for (int i = 1; i <= 5; i++) {
            Conversation c = persistConversation("Conversation " + i, base.minusMinutes(i));
            persistParticipant(c, user1);
        }

        Pageable p0 = PageRequest.of(0, 2);
        Page<Conversation> page0 = conversationRepository.findConversationsByUserId(user1.getId(), p0);
        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getTotalElements()).isEqualTo(5);
        assertThat(page0.getTotalPages()).isEqualTo(3);
        assertThat(page0.hasNext()).isTrue();

        Pageable p1 = PageRequest.of(1, 2);
        Page<Conversation> page1 = conversationRepository.findConversationsByUserId(user1.getId(), p1);
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.hasNext()).isTrue();
        assertThat(page1.hasPrevious()).isTrue();
    }
}