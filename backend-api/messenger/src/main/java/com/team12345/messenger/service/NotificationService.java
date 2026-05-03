package com.team12345.messenger.service;

import com.team12345.messenger.entity.Notification;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Create and save a new notification.
     *
     * @param user    The recipient of the notification
     * @param type    The type of notification (e.g., "message", "friend_request")
     * @param content The content of the notification
     * @return The saved Notification entity
     */
    @Transactional
    public Notification createNotification(User user, String type, String content) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .content(content)
                .isSeen(false)
                .build();
        return notificationRepository.save(notification);
    }

    /**
     * Mark all unseen notifications as seen for a specific user.
     *
     * @param userId The ID of the user
     * @return The number of notifications updated
     */
    @Transactional
    public int markAllAsSeen(Long userId) {
        return notificationRepository.markAllAsSeenByUserId(userId);
    }

    /**
     * Get a paginated slice of notifications for a user, ordered by creation date descending.
     *
     * @param userId   The ID of the user
     * @param pageable Pagination information
     * @return A slice of notifications
     */
    @Transactional(readOnly = true)
    public Slice<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
