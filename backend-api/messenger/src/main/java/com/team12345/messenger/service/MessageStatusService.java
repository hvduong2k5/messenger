package com.team12345.messenger.service;

/**
 * Service interface for managing message delivery and read status.
 */
public interface MessageStatusService {

    /**
     * Marks all messages in a conversation as read for a specific user.
     *
     * @param userId         the ID of the user reading the messages
     * @param conversationId the ID of the conversation
     */
    void markAsRead(Long userId, Long conversationId);

    /**
     * Marks a specific message as read for a specific user.
     *
     * @param userId    the ID of the user reading the message
     * @param messageId the ID of the message
     */
    void markMessageAsRead(Long userId, Long messageId);

    /**
     * Counts the number of unread messages for a user in a specific conversation.
     *
     * @param userId         the ID of the user
     * @param conversationId the ID of the conversation
     * @return the count of unread messages
     */
    long countUnreadMessages(Long userId, Long conversationId);

    /**
     * Counts the total number of unread messages for a user across all conversations.
     *
     * @param userId the ID of the user
     * @return the total count of unread messages
     */
    long countTotalUnreadMessages(Long userId);
}
