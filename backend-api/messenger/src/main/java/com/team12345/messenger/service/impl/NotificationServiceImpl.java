package com.team12345.messenger.service.impl;
import com.team12345.messenger.dto.response.NotificationResponseDTO;
import com.team12345.messenger.entity.Notification;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.repository.NotificationRepository;
import com.team12345.messenger.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * Implementation of {@link NotificationService}.
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    @Override
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
    @Override
    @Transactional
    public int markAllAsSeen(Long userId) {
        return notificationRepository.markAllAsSeenByUserId(userId);
    }
    @Override
    @Transactional(readOnly = true)
    public Slice<NotificationResponseDTO> getUserNotifications(Long userId, Pageable pageable) {
        Slice<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(notification -> NotificationResponseDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .content(notification.getContent())
                .isSeen(notification.getIsSeen())
                .createdAt(notification.getCreatedAt())
                .build());
    }
    @Override
    @Transactional(readOnly = true)
    public long countUnseenNotifications(Long userId) {
        return notificationRepository.countByUserIdAndIsSeenFalse(userId);
    }
}