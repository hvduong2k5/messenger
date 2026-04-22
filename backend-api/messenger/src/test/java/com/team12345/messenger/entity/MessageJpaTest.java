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
public class MessageJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testSaveMessage() {
        User user = User.builder()
                .username("sender_user")
                .email("sender@example.com")
                .password("$2a$10$abcdefghijklmnopqrstuvxyz123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(user);

        Conversation conversation = Conversation.builder()
                .name("Chat with sender")
                .isGroup(false)
                .build();
        entityManager.persistAndFlush(conversation);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(user)
                .content("Hello, this is a test message!")
                .clientMessageId("client-uuid-12345")
                .build();

        Message savedMessage = entityManager.persistAndFlush(message);

        assertThat(savedMessage.getId()).isNotNull();
        assertThat(savedMessage.getContent()).isEqualTo("Hello, this is a test message!");
        assertThat(savedMessage.getClientMessageId()).isEqualTo("client-uuid-12345");
        assertThat(savedMessage.getCreatedAt()).isNotNull();
        assertThat(savedMessage.getSender().getUsername()).isEqualTo("sender_user");
        assertThat(savedMessage.getConversation().getName()).isEqualTo("Chat with sender");
    }
}