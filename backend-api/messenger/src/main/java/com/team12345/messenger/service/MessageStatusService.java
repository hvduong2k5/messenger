package com.team12345.messenger.service;

/**
 * Service interface for managing message delivery and read status.
 */
public interface MessageStatusService {

    /**
     * Marks all messages in a conversation as read for a specific user.
     *
     * @param conversationId the ID of the conversation
     * @param userId         the ID of the user reading the messages
     */
    void markConversationAsRead(Long conversationId, Long userId);

    /**
     * Updates the status of a specific message for a user.
     *
     * @param messageId the ID of the message
     * @param userId    the ID of the user reading/receiving the message
     * @param status    the new status (DELIVERED or READ)
     */
    void updateMessageStatus(Long messageId, Long userId, String status);

    /**
     * Gets the statuses of a specific message (for group chat read receipts).
     *
     * @param messageId the ID of the message
     * @return list of status DTOs
     */
    java.util.List<com.team12345.messenger.dto.response.MessageStatusResponseDTO> getMessageStatuses(Long messageId);

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
