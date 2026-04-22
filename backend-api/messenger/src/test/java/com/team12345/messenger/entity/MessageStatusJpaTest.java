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
public class MessageStatusJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testSaveMessageStatus() {
        User sender = User.builder()
                .username("sender")
                .email("sender@example.com")
                .password("$2a$10$abcdefghijklmnopqrstuvxyz123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(sender);

        User receiver = User.builder()
                .username("receiver")
                .email("receiver@example.com")
                .password("$2a$10$abcdefghijklmnopqrstuvxyz123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(receiver);

        Conversation conversation = Conversation.builder()
                .name("Test Group")
                .isGroup(true)
                .build();
        entityManager.persistAndFlush(conversation);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content("Hello receiver!")
                .build();
        entityManager.persistAndFlush(message);

        MessageStatusId statusId = new MessageStatusId(message.getId(), receiver.getId());
        MessageStatus messageStatus = MessageStatus.builder()
                .id(statusId)
                .message(message)
                .receiver(receiver)
                .status(MessageStatusEnum.delivered)
                .build();

        MessageStatus savedStatus = entityManager.persistAndFlush(messageStatus);

        assertThat(savedStatus.getId()).isNotNull();
        assertThat(savedStatus.getStatus()).isEqualTo(MessageStatusEnum.delivered);
        assertThat(savedStatus.getUpdatedAt()).isNotNull();
        assertThat(savedStatus.getReceiver().getUsername()).isEqualTo("receiver");
        assertThat(savedStatus.getMessage().getContent()).isEqualTo("Hello receiver!");
    }
}