package com.team12345.messenger.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_notification",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class NotificationJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testSaveNotification() {
        // 1. Tạo và lưu một User trước (vì Notification cần user_id)
        User user = User.builder()
                .username("tuan_dev")
                .email("tuan@example.com")
                .password("$2a$12$R9h/cIPz0gi.URQHeNH5E.4PzN8v7/6Tux0XTMk.Y5.v4.Dq7.D/G") // 60 chars BCrypt hash
                .build();
        entityManager.persist(user);

        // 2. Tạo Notification gắn với User đó
        Notification notification = Notification.builder()
                .user(user)
                .type("MESSAGE")
                .content("Bạn có tin nhắn mới từ Tùng")
                .isSeen(false)
                .build();

        Notification savedNotification = entityManager.persistAndFlush(notification);

        // 3. Kiểm tra các giá trị
        assertThat(savedNotification.getId()).isNotNull();
        assertThat(savedNotification.getUser().getUsername()).isEqualTo("tuan_dev");
        assertThat(savedNotification.getContent()).isEqualTo("Bạn có tin nhắn mới từ Tùng");
        assertThat(savedNotification.getIsSeen()).isFalse();
        assertThat(savedNotification.getCreatedAt()).isNotNull(); // Kiểm tra xem @CreationTimestamp có hoạt động không
    }

    @Test
    public void testNotificationDefaultValues() {
        Notification notification = new Notification();
        // Giả sử content và type có thể null, nhưng isSeen mặc định là false
        Notification saved = entityManager.persistAndFlush(notification);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIsSeen()).isFalse(); // Kiểm tra default value FALSE
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}