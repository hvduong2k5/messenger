package com.team12345.messenger.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_attachment",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class AttachmentJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testSaveAttachment() {
        User user = User.builder()
                .username("attachment_user")
                .email("attachment@example.com")
                .password("password12345678901234567890123456789012345678901234567890123456")
                .build();
        entityManager.persistAndFlush(user);

        Conversation conversation = Conversation.builder()
                .name("Attachment Chat")
                .isGroup(false)
                .build();
        entityManager.persistAndFlush(conversation);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(user)
                .content("Message for attachment")
                .build();
        entityManager.persistAndFlush(message);

        Attachment attachment = Attachment.builder()
                .fileUrl("http://example.com/test.png")
                .fileType("image")
                .fileSize(51200)
                .message(message)
                .build();

        Attachment savedAttachment = entityManager.persistAndFlush(attachment);

        assertThat(savedAttachment.getId()).isNotNull();
        assertThat(savedAttachment.getFileUrl()).isEqualTo("http://example.com/test.png");
        assertThat(savedAttachment.getFileType()).isEqualTo("image");
        assertThat(savedAttachment.getFileSize()).isEqualTo(51200);
        assertThat(savedAttachment.getUploadedAt()).isNotNull();
        assertThat(savedAttachment.getMessage().getId()).isEqualTo(message.getId());
    }

    @Test
    public void testPrePersistSetsUploadedAt() {
        Attachment attachment = Attachment.builder()
                .fileUrl("http://example.com/timestamp-test.dat")
                .fileType("file")
                .build();

        User user = User.builder().username("u").email("u@e.c").password("password12345678901234567890123456789012345678901234567890123456").build();
        entityManager.persistAndFlush(user);
        Conversation conv = Conversation.builder().name("c").isGroup(false).build();
        entityManager.persistAndFlush(conv);
        Message msg = Message.builder().conversation(conv).sender(user).content("m").build();
        entityManager.persistAndFlush(msg);
        
        attachment.setMessage(msg);

        assertThat(attachment.getUploadedAt()).isNull();

        Attachment savedAttachment = entityManager.persistAndFlush(attachment);

        assertThat(savedAttachment.getUploadedAt()).isNotNull();
        assertThat(savedAttachment.getUploadedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
