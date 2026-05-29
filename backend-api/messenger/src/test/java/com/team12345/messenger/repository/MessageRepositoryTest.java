package com.team12345.messenger.repository;

import com.team12345.messenger.entity.Conversation;
import com.team12345.messenger.entity.Message;
import com.team12345.messenger.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@DataJpaTest
class MessageRepositoryTest {

    @TestConfiguration
    @EnableJpaAuditing
    static class AuditConfig {}

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void testFindByConversationIdOrderByCreatedAtDesc() {
        // Create users
        User sender1 = User.builder()
                .username("sender1")
                .email("sender1@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        User sender2 = User.builder()
                .username("sender2")
                .email("sender2@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(sender1);
        entityManager.persistAndFlush(sender2);

        // Create conversation
        Conversation conversation = Conversation.builder()
                .name("Test Conversation")
                .isGroup(false)
                .build();
        entityManager.persistAndFlush(conversation);

        // Create messages with different timestamps
        Message message1 = Message.builder()
                .conversation(conversation)
                .sender(sender1)
                .content("Message 1")
                .build();
        entityManager.persistAndFlush(message1);
        try { Thread.sleep(10); } catch (InterruptedException e) {}

        Message message2 = Message.builder()
                .conversation(conversation)
                .sender(sender2)
                .content("Message 2")
                .build();
        entityManager.persistAndFlush(message2);
        try { Thread.sleep(10); } catch (InterruptedException e) {}

        Message message3 = Message.builder()
                .conversation(conversation)
                .sender(sender1)
                .content("Message 3")
                .build();
        entityManager.persistAndFlush(message3);

        // Test with pagination (page 0, size 2)
        Pageable pageable = PageRequest.of(0, 2);
        Page<Message> page = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversation.getId(), pageable);

        // Verify results - should be ordered by created_at DESC
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getId()).isEqualTo(message3.getId()); // Most recent first
        assertThat(page.getContent().get(1).getId()).isEqualTo(message2.getId());
    }

    @Test
    void testFindByConversationIdOrderByCreatedAtDescPagination() {
        // Create user
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
                .build();
        entityManager.persistAndFlush(conversation);

        // Create 5 messages
        for (int i = 1; i <= 5; i++) {
            Message message = Message.builder()
                    .conversation(conversation)
                    .sender(sender)
                    .content("Message " + i)
                    .build();
            entityManager.persistAndFlush(message);
            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }

        // Test first page (page 0, size 2)
        Pageable pageable1 = PageRequest.of(0, 2);
        Page<Message> page1 = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversation.getId(), pageable1);

        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getTotalPages()).isEqualTo(3);

        // Test second page
        Pageable pageable2 = PageRequest.of(1, 2);
        Page<Message> page2 = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversation.getId(), pageable2);

        assertThat(page2.getContent()).hasSize(2);

        // Test third page
        Pageable pageable3 = PageRequest.of(2, 2);
        Page<Message> page3 = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversation.getId(), pageable3);

        assertThat(page3.getContent()).hasSize(1);
    }

    @Test
    void testFindFirstByConversationIdOrderByCreatedAtDesc() {
        // Create user
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
                .build();
        entityManager.persistAndFlush(conversation);

        // Create messages with different timestamps
        Message message1 = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content("Message 1")
                .build();
        entityManager.persistAndFlush(message1);
        try { Thread.sleep(10); } catch (InterruptedException e) {}

        Message message2 = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content("Message 2")
                .build();
        entityManager.persistAndFlush(message2);
        try { Thread.sleep(10); } catch (InterruptedException e) {}

        Message message3 = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content("Message 3 - Last")
                .build();
        entityManager.persistAndFlush(message3);

        // Test finding last message
        Optional<Message> lastMessage = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId());

        assertThat(lastMessage).isPresent();
        assertThat(lastMessage.get().getId()).isEqualTo(message3.getId());
        assertThat(lastMessage.get().getContent()).isEqualTo("Message 3 - Last");
    }

    @Test
    void testFindFirstByConversationIdOrderByCreatedAtDescWhenEmpty() {
        // Create conversation without messages
        Conversation conversation = Conversation.builder()
                .name("Empty Conversation")
                .isGroup(false)
                .build();
        entityManager.persistAndFlush(conversation);

        // Test finding last message in empty conversation
        Optional<Message> lastMessage = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId());

        assertThat(lastMessage).isEmpty();
    }

    @Test
    void testFindByConversationIdOrderByCreatedAtDescEmpty() {
        // Create conversation without messages
        Conversation conversation = Conversation.builder()
                .name("Empty Conversation")
                .isGroup(false)
                .build();
        entityManager.persistAndFlush(conversation);

        // Test querying empty conversation
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> page = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversation.getId(), pageable);

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void testFindByConversationIdOrderByCreatedAtDescMultipleConversations() {
        // Create users
        User sender1 = User.builder()
                .username("sender1")
                .email("sender1@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        User sender2 = User.builder()
                .username("sender2")
                .email("sender2@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(sender1);
        entityManager.persistAndFlush(sender2);

        // Create two conversations
        Conversation conversation1 = Conversation.builder()
                .name("Conversation 1")
                .isGroup(false)
                .build();
        Conversation conversation2 = Conversation.builder()
                .name("Conversation 2")
                .isGroup(false)
                .build();
        entityManager.persistAndFlush(conversation1);
        entityManager.persistAndFlush(conversation2);

        // Create messages for conversation 1
        Message msg1c1 = Message.builder()
                .conversation(conversation1)
                .sender(sender1)
                .content("Message 1 in Conv1")
                .build();
        entityManager.persistAndFlush(msg1c1);
        try { Thread.sleep(10); } catch (InterruptedException e) {}

        Message msg2c1 = Message.builder()
                .conversation(conversation1)
                .sender(sender2)
                .content("Message 2 in Conv1")
                .build();
        entityManager.persistAndFlush(msg2c1);
        try { Thread.sleep(10); } catch (InterruptedException e) {}

        // Create messages for conversation 2
        Message msg1c2 = Message.builder()
                .conversation(conversation2)
                .sender(sender1)
                .content("Message 1 in Conv2")
                .build();
        entityManager.persistAndFlush(msg1c2);
        try { Thread.sleep(10); } catch (InterruptedException e) {}

        Message msg2c2 = Message.builder()
                .conversation(conversation2)
                .sender(sender2)
                .content("Message 2 in Conv2")
                .build();
        entityManager.persistAndFlush(msg2c2);
        try { Thread.sleep(10); } catch (InterruptedException e) {}

        Message msg3c2 = Message.builder()
                .conversation(conversation2)
                .sender(sender1)
                .content("Message 3 in Conv2")
                .build();
        entityManager.persistAndFlush(msg3c2);

        // Test conversation 1 - should have 2 messages
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> page1 = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversation1.getId(), pageable);

        assertThat(page1.getTotalElements()).isEqualTo(2);
        assertThat(page1.getContent()).allMatch(m -> m.getConversation().getId().equals(conversation1.getId()));

        // Test conversation 2 - should have 3 messages
        Page<Message> page2 = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversation2.getId(), pageable);

        assertThat(page2.getTotalElements()).isEqualTo(3);
        assertThat(page2.getContent()).allMatch(m -> m.getConversation().getId().equals(conversation2.getId()));
        assertThat(page2.getContent().get(0).getId()).isEqualTo(msg3c2.getId()); // Most recent first
    }
}

