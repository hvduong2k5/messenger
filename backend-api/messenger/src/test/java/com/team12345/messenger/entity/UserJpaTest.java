package com.team12345.messenger.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class UserJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    private String longPassword() {
        return "p".repeat(60); // create a dummy BCrypt-length password for tests
    }

    @Test
    public void testSaveUser() {
        User user = User.builder()
                .username("johndoe")
                .email("john.doe@example.com")
                .password(longPassword())
                .status("ACTIVE")
                .build();

        User savedUser = entityManager.persistAndFlush(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("johndoe");
    }

    @Test
    public void testUniqueUsernameConstraint() {
        User user1 = User.builder()
                .username("uniqueuser")
                .email("user1@example.com")
                .password(longPassword())
                .build();
        entityManager.persistAndFlush(user1);

        User user2 = User.builder()
                .username("uniqueuser") // Trùng username
                .email("user2@example.com")
                .password(longPassword())
                .build();

        assertThatThrownBy(() -> entityManager.persistAndFlush(user2))
                .isInstanceOfAny(
                        jakarta.persistence.PersistenceException.class,
                        org.springframework.dao.DataIntegrityViolationException.class,
                        org.hibernate.exception.ConstraintViolationException.class
                );
    }

    @Test
    public void testUniqueEmailConstraint() {
        User user1 = User.builder()
                .username("user1")
                .email("unique@example.com")
                .password(longPassword())
                .build();
        entityManager.persistAndFlush(user1);

        User user2 = User.builder()
                .username("user2")
                .email("unique@example.com") // Trùng email
                .password(longPassword())
                .build();

        assertThatThrownBy(() -> entityManager.persistAndFlush(user2))
                .isInstanceOfAny(
                        jakarta.persistence.PersistenceException.class,
                        org.springframework.dao.DataIntegrityViolationException.class,
                        org.hibernate.exception.ConstraintViolationException.class
                );
    }
}