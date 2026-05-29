package com.team12345.messenger.repository;
import com.team12345.messenger.entity.Notification;
import com.team12345.messenger.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class NotificationRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private NotificationRepository notificationRepository;
    @Test
    void testFindByUserId() {
        User user = User.builder()
                .username("notify_user")
                .email("notify@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(user);
        Notification n1 = Notification.builder()
                .user(user)
                .type("message")
                .content("New message from Alice")
                .isSeen(false)
                .build();
        
        entityManager.persist(n1);
        entityManager.flush();
        Slice<Notification> notificationSlice = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 10));
        assertThat(notificationSlice.getContent()).hasSize(1);
        assertThat(notificationSlice.getContent().get(0).getContent()).isEqualTo("New message from Alice");
    }
    @Test
    void testMarkAllAsSeenByUserId() {
        User user = User.builder()
                .username("seen_user")
                .email("seen@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(user);
        Notification n1 = Notification.builder()
                .user(user)
                .type("message")
                .content("Unseen 1")
                .isSeen(false)
                .build();
        
        entityManager.persist(n1);
        entityManager.flush();
        int updatedCount = notificationRepository.markAllAsSeenByUserId(user.getId());
        entityManager.clear();
        assertThat(updatedCount).isEqualTo(1);
        Slice<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 10));
        assertThat(notifications.getContent().get(0).getIsSeen()).isTrue();
    }
}