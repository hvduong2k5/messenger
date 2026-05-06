package com.team12345.messenger.repository;

import com.team12345.messenger.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MessageStatusRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MessageStatusRepository messageStatusRepository;

    @Test
    void testCountUnreadByReceiverId() {
        // Create users
        User sender = User.builder()
                .username("sender")
                .email("sender@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        User receiver = User.builder()
                .username("receiver")
                .email("receiver@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(sender);
        entityManager.persistAndFlush(receiver);

        // Create conversation
        Conversation conversation = Conversation.builder()
                .name("Test Conversation")
                .isGroup(false)
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(conversation);

        // Create messages
        Message message1 = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content("Message 1")
                .build();
        Message message2 = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content("Message 2")
                .build();
        entityManager.persistAndFlush(message1);
        entityManager.persistAndFlush(message2);

        // Create message statuses
        MessageStatus status1 = MessageStatus.builder()
                .id(new MessageStatusId(message1.getId(), receiver.getId()))
                .message(message1)
                .receiver(receiver)
                .status(MessageStatusEnum.sent)
                .build();
        MessageStatus status2 = MessageStatus.builder()
                .id(new MessageStatusId(message2.getId(), receiver.getId()))
                .message(message2)
                .receiver(receiver)
                .status(MessageStatusEnum.delivered)
                .build();
        entityManager.persistAndFlush(status1);
        entityManager.persistAndFlush(status2);

        // Test count unread
        long unreadCount = messageStatusRepository.countById_ReceiverIdAndStatusNot(receiver.getId(), MessageStatusEnum.read);
        assertThat(unreadCount).isEqualTo(2); // Both sent and delivered are unread
    }

    @Test
    void testCountUnreadByReceiverIdAndConversationId() {
        // Create users
        User sender = User.builder()
                .username("sender")
                .email("sender@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        User receiver = User.builder()
                .username("receiver")
                .email("receiver@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(sender);
        entityManager.persistAndFlush(receiver);

        // Create conversations
        Conversation conversation1 = Conversation.builder()
                .name("Conversation 1")
                .isGroup(false)
                .updatedAt(LocalDateTime.now())
                .build();
        Conversation conversation2 = Conversation.builder()
                .name("Conversation 2")
                .isGroup(false)
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(conversation1);
        entityManager.persistAndFlush(conversation2);

        // Create messages
        Message message1 = Message.builder()
                .conversation(conversation1)
                .sender(sender)
                .content("Message 1")
                .build();
        Message message2 = Message.builder()
                .conversation(conversation2)
                .sender(sender)
                .content("Message 2")
                .build();
        entityManager.persistAndFlush(message1);
        entityManager.persistAndFlush(message2);

        // Create message statuses
        MessageStatus status1 = MessageStatus.builder()
                .id(new MessageStatusId(message1.getId(), receiver.getId()))
                .message(message1)
                .receiver(receiver)
                .status(MessageStatusEnum.sent)
                .build();
        MessageStatus status2 = MessageStatus.builder()
                .id(new MessageStatusId(message2.getId(), receiver.getId()))
                .message(message2)
                .receiver(receiver)
                .status(MessageStatusEnum.read)
                .build();
        entityManager.persistAndFlush(status1);
        entityManager.persistAndFlush(status2);

        // Test count unread by conversation
        long unreadCount1 = messageStatusRepository.countUnreadInConversation(receiver.getId(), conversation1.getId(), MessageStatusEnum.read);
        long unreadCount2 = messageStatusRepository.countUnreadInConversation(receiver.getId(), conversation2.getId(), MessageStatusEnum.read);

        assertThat(unreadCount1).isEqualTo(1); // sent status is unread
        assertThat(unreadCount2).isEqualTo(0); // read status is not unread
    }

    @Test
    void testMarkAsReadByReceiverIdAndMessageId() {
        // Create users
        User sender = User.builder()
                .username("sender")
                .email("sender@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        User receiver = User.builder()
                .username("receiver")
                .email("receiver@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(sender);
        entityManager.persistAndFlush(receiver);

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
                .content("Test Message")
                .build();
        entityManager.persistAndFlush(message);

        // Create message status
        MessageStatus status = MessageStatus.builder()
                .id(new MessageStatusId(message.getId(), receiver.getId()))
                .message(message)
                .receiver(receiver)
                .status(MessageStatusEnum.sent)
                .build();
        entityManager.persistAndFlush(status);

        // Mark as read
        int updatedRows = messageStatusRepository.markAsReadByMessageId(receiver.getId(), message.getId(), MessageStatusEnum.read);
        entityManager.flush();
        entityManager.clear();

        // Verify update
        assertThat(updatedRows).isEqualTo(1);
        MessageStatus updatedStatus = entityManager.find(MessageStatus.class, new MessageStatusId(message.getId(), receiver.getId()));
        assertThat(updatedStatus.getStatus()).isEqualTo(MessageStatusEnum.read);
    }

    @Test
    void testMarkAsReadByReceiverIdAndConversationId() {
        // Create users
        User sender = User.builder()
                .username("sender")
                .email("sender@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        User receiver = User.builder()
                .username("receiver")
                .email("receiver@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(sender);
        entityManager.persistAndFlush(receiver);

        // Create conversation
        Conversation conversation = Conversation.builder()
                .name("Test Conversation")
                .isGroup(false)
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(conversation);

        // Create messages
        Message message1 = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content("Message 1")
                .build();
        Message message2 = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content("Message 2")
                .build();
        entityManager.persistAndFlush(message1);
        entityManager.persistAndFlush(message2);

        // Create message statuses
        MessageStatus status1 = MessageStatus.builder()
                .id(new MessageStatusId(message1.getId(), receiver.getId()))
                .message(message1)
                .receiver(receiver)
                .status(MessageStatusEnum.sent)
                .build();
        MessageStatus status2 = MessageStatus.builder()
                .id(new MessageStatusId(message2.getId(), receiver.getId()))
                .message(message2)
                .receiver(receiver)
                .status(MessageStatusEnum.delivered)
                .build();
        entityManager.persistAndFlush(status1);
        entityManager.persistAndFlush(status2);

        // Mark all as read in conversation
        int updatedRows = messageStatusRepository.markAsReadByConversationId(receiver.getId(), conversation.getId(), MessageStatusEnum.read);
        entityManager.flush();
        entityManager.clear();

        // Verify updates
        assertThat(updatedRows).isEqualTo(2);
        MessageStatus updatedStatus1 = entityManager.find(MessageStatus.class, new MessageStatusId(message1.getId(), receiver.getId()));
        MessageStatus updatedStatus2 = entityManager.find(MessageStatus.class, new MessageStatusId(message2.getId(), receiver.getId()));
        assertThat(updatedStatus1.getStatus()).isEqualTo(MessageStatusEnum.read);
        assertThat(updatedStatus2.getStatus()).isEqualTo(MessageStatusEnum.read);
    }
}
