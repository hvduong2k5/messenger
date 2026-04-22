package com.team12345.messenger.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class ParticipantJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testSaveParticipant() {
        User user = User.builder()
                .username("jane_doe")
                .email("jane@example.com")
                .password("$2a$10$abcdefghijklmnopqrstuvxyz123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(user);

        Conversation conversation = Conversation.builder()
                .name("Test Group")
                .isGroup(true)
                .build();
        entityManager.persistAndFlush(conversation);

        ParticipantId participantId = new ParticipantId(conversation.getId(), user.getId());
        Participant participant = Participant.builder()
                .id(participantId)
                .conversation(conversation)
                .user(user)
                .role(ParticipantRole.admin)
                .build();

        Participant savedParticipant = entityManager.persistAndFlush(participant);

        assertThat(savedParticipant.getId()).isNotNull();
        assertThat(savedParticipant.getRole()).isEqualTo(ParticipantRole.admin);
        assertThat(savedParticipant.getJoinedAt()).isNotNull();
        assertThat(savedParticipant.getUser().getUsername()).isEqualTo("jane_doe");
        assertThat(savedParticipant.getConversation().getName()).isEqualTo("Test Group");
    }

    @Test
    public void testSaveParticipantDefaultValues() {
        User user = User.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("$2a$10$abcdefghijklmnopqrstuvxyz123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(user);

        Conversation conversation = Conversation.builder()
                .name("Another Group")
                .isGroup(true)
                .build();
        entityManager.persistAndFlush(conversation);

        ParticipantId participantId = new ParticipantId(conversation.getId(), user.getId());
        Participant participant = Participant.builder()
                .id(participantId)
                .conversation(conversation)
                .user(user)
                .build();

        Participant savedParticipant = entityManager.persistAndFlush(participant);

        assertThat(savedParticipant.getId()).isNotNull();
        assertThat(savedParticipant.getRole()).isEqualTo(ParticipantRole.member);
        assertThat(savedParticipant.getJoinedAt()).isNotNull();
    }
}