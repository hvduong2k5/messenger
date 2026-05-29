package com.team12345.messenger.service;
import com.team12345.messenger.dto.response.NotificationResponseDTO;
import com.team12345.messenger.entity.Notification;
import com.team12345.messenger.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

/**
 * Service interface for managing notifications.
 */
public interface NotificationService {
    /**
     * Create and save a new notification.
     *
     * @param user    The recipient of the notification
     * @param type    The type of notification (e.g., "message", "friend_request")
     * @param content The content of the notification
     * @return The saved Notification entity
     */
        Notification createNotification(User user, String type, String content);
    /**
     * Mark all unseen notifications as seen for a specific user.
     *
     * @param userId The ID of the user
     * @return The number of notifications updated
     */
    int markAllAsSeen(Long userId);
    /**
     * Get a paginated slice of notifications for a user, ordered by creation date descending.
     *
     * @param userId   The ID of the user
     * @param pageable Pagination information
     * @return A slice of notification DTOs
     */
        Slice<NotificationResponseDTO> getUserNotifications(Long userId, Pageable pageable);
    /**
     * Counts the number of unseen notifications for a user.
     *
     * @param userId The ID of the user
     * @return the count of unseen notifications
     */
    long countUnseenNotifications(Long userId);

    /**
     * Mark a specific notification as read (seen).
     *
     * @param notificationId The ID of the notification
     * @param userId         The ID of the user performing the action
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * Delete a specific notification.
     *
     * @param notificationId The ID of the notification
     * @param userId         The ID of the user performing the action
     */
    void deleteNotification(Long notificationId, Long userId);
}