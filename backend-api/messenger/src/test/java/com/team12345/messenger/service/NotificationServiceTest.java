package com.team12345.messenger.service;

import com.team12345.messenger.entity.Notification;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .build();
    }

    @Test
    void testCreateNotification() {
        // Arrange
        String type = "message";
        String content = "Hello World";
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .content(content)
                .isSeen(false)
                .build();
        
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        Notification result = notificationService.createNotification(user, type, content);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getContent()).isEqualTo(content);
        assertThat(result.getUser()).isEqualTo(user);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testMarkAllAsSeen() {
        // Arrange
        when(notificationRepository.markAllAsSeenByUserId(1L)).thenReturn(5);

        // Act
        int result = notificationService.markAllAsSeen(1L);

        // Assert
        assertThat(result).isEqualTo(5);
        verify(notificationRepository, times(1)).markAllAsSeenByUserId(1L);
    }

    @Test
    void testGetUserNotifications() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Notification notification = Notification.builder()
                .id(1L)
                .user(user)
                .content("Test Content")
                .build();
        Slice<Notification> slice = new SliceImpl<>(List.of(notification));
        
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(slice);

        // Act
        Slice<Notification> result = notificationService.getUserNotifications(1L, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Test Content");
        verify(notificationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L, pageable);
    }
}
