package com.team12345.messenger.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

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

    @TestConfiguration
    @EnableJpaAuditing
    static class AuditConfig {}

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

    @Test
    public void testSaveMessageWithAttachments() {
        User user = User.builder()
                .username("attachment_sender")
                .email("attachment@example.com")
                .password("password12345678901234567890123456789012345678901234567890123456")
                .build();
        entityManager.persistAndFlush(user);

        Conversation conversation = Conversation.builder()
                .name("Attachment Test Chat")
                .isGroup(false)
                .build();
        entityManager.persistAndFlush(conversation);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(user)
                .content("Message with attachments")
                .build();
        
        Attachment attachment1 = Attachment.builder()
                .fileUrl("http://example.com/file1.jpg")
                .fileType("image")
                .fileSize(1024)
                .message(message)
                .build();
        
        Attachment attachment2 = Attachment.builder()
                .fileUrl("http://example.com/file2.pdf")
                .fileType("file")
                .fileSize(2048)
                .message(message)
                .build();

        message.setAttachments(java.util.Arrays.asList(attachment1, attachment2));

        Message savedMessage = entityManager.persistAndFlush(message);
        entityManager.clear();

        Message foundMessage = entityManager.find(Message.class, savedMessage.getId());
        assertThat(foundMessage.getAttachments()).hasSize(2);
        assertThat(foundMessage.getAttachments()).extracting(Attachment::getFileUrl)
                .containsExactlyInAnyOrder("http://example.com/file1.jpg", "http://example.com/file2.pdf");
    }

    @Test
    public void testSaveMessageWithLongContent() {
        User user = User.builder()
                .username("long_content_user")
                .email("long@example.com")
                .password("password12345678901234567890123456789012345678901234567890123456")
                .build();
        entityManager.persistAndFlush(user);

        Conversation conversation = Conversation.builder()
                .name("Long Content Chat")
                .isGroup(false)
                .build();
        entityManager.persistAndFlush(conversation);

        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longContent.append("Word ");
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(user)
                .content(longContent.toString())
                .build();

        Message savedMessage = entityManager.persistAndFlush(message);
        
        assertThat(savedMessage.getId()).isNotNull();
        assertThat(savedMessage.getContent()).isEqualTo(longContent.toString());
    }
}