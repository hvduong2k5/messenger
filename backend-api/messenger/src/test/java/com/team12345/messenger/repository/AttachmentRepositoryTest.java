package com.team12345.messenger.repository;

import com.team12345.messenger.entity.Attachment;
import com.team12345.messenger.entity.Conversation;
import com.team12345.messenger.entity.Message;
import com.team12345.messenger.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AttachmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Test
    void testFindByMessageId() {
        // Create users
        User sender = User.builder()
                .username("sender")
                .email("sender@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(sender);

        // Create conversation
        Conversation conversation = Conversation.builder()
                .name("Test Conversation")
                .isGroup(false)
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(conversation);

        // Create message
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content("Test message with attachments")
                .build();
        entityManager.persistAndFlush(message);

        // Create attachments
        Attachment attachment1 = Attachment.builder()
                .message(message)
                .fileUrl("https://example.com/file1.jpg")
                .fileType("image")
                .fileSize(1024000)
                .build();
        Attachment attachment2 = Attachment.builder()
                .message(message)
                .fileUrl("https://example.com/file2.pdf")
                .fileType("file")
                .fileSize(2048000)
                .build();
        entityManager.persistAndFlush(attachment1);
        entityManager.persistAndFlush(attachment2);

        // Test findByMessageId
        List<Attachment> attachments = attachmentRepository.findByMessageId(message.getId());

        // Verify results
        assertThat(attachments).hasSize(2);
        assertThat(attachments.get(0).getFileUrl()).isEqualTo("https://example.com/file1.jpg");
        assertThat(attachments.get(1).getFileUrl()).isEqualTo("https://example.com/file2.pdf");
        assertThat(attachments.get(0).getFileType()).isEqualTo("image");
        assertThat(attachments.get(1).getFileType()).isEqualTo("file");
    }

    @Test
    void testFindByMessageId_NoAttachments() {
        // Create users
        User sender = User.builder()
                .username("sender")
                .email("sender@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(sender);

        // Create conversation
        Conversation conversation = Conversation.builder()
                .name("Test Conversation")
                .isGroup(false)
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(conversation);

        // Create message without attachments
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content("Test message without attachments")
                .build();
        entityManager.persistAndFlush(message);

        // Test findByMessageId with no attachments
        List<Attachment> attachments = attachmentRepository.findByMessageId(message.getId());

        // Verify results
        assertThat(attachments).isEmpty();
    }

    @Test
    void testFindByMessageId_MultipleMessages() {
        // Create users
        User sender = User.builder()
                .username("sender")
                .email("sender@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(sender);

        // Create conversation
        Conversation conversation = Conversation.builder()
                .name("Test Conversation")
                .isGroup(false)
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(conversation);

        // Create first message with attachments
        Message message1 = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content("First message")
                .build();
        entityManager.persistAndFlush(message1);

        // Create second message with different attachments
        Message message2 = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content("Second message")
                .build();
        entityManager.persistAndFlush(message2);

        // Create attachments for first message
        Attachment attachment1 = Attachment.builder()
                .message(message1)
                .fileUrl("https://example.com/file1.jpg")
                .fileType("image")
                .fileSize(1024000)
                .build();
        entityManager.persistAndFlush(attachment1);

        // Create attachments for second message
        Attachment attachment2 = Attachment.builder()
                .message(message2)
                .fileUrl("https://example.com/file2.pdf")
                .fileType("file")
                .fileSize(2048000)
                .build();
        Attachment attachment3 = Attachment.builder()
                .message(message2)
                .fileUrl("https://example.com/file3.mp4")
                .fileType("video")
                .fileSize(5120000)
                .build();
        entityManager.persistAndFlush(attachment2);
        entityManager.persistAndFlush(attachment3);

        // Test findByMessageId for first message
        List<Attachment> attachments1 = attachmentRepository.findByMessageId(message1.getId());
        assertThat(attachments1).hasSize(1);
        assertThat(attachments1.get(0).getFileUrl()).isEqualTo("https://example.com/file1.jpg");

        // Test findByMessageId for second message
        List<Attachment> attachments2 = attachmentRepository.findByMessageId(message2.getId());
        assertThat(attachments2).hasSize(2);
        assertThat(attachments2.get(0).getFileUrl()).isEqualTo("https://example.com/file2.pdf");
        assertThat(attachments2.get(1).getFileUrl()).isEqualTo("https://example.com/file3.mp4");
    }
}
