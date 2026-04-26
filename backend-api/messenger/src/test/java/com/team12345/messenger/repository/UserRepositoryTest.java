package com.team12345.messenger.repository;

import com.team12345.messenger.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

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
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void testFindByEmail() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("passwpasswo5555555555555555555555555555555555555555555555555555555rdord")
                .build();
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    public void testFindByUsername() {
        User user = User.builder()
                .username("johndoe")
                .email("johndoe@example.com")
                .password("passwo5555555555555555555555555555555555555555555555555555555rd")
                .build();
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername("johndoe");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("johndoe@example.com");
    }

    @Test
    public void testExistsByEmail() {
        User user = User.builder()
                .username("alice")
                .email("alice@example.com")
                .password("passwo5555555555555555555555555555555555555555555555555555555rd")
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("alice@example.com");
        boolean notExists = userRepository.existsByEmail("bob@example.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    public void testExistsByUsername() {
        User user = User.builder()
                .username("bob")
                .email("bob@example.com")
                .password("passpasswo5555555555555555555555555555555555555555555555555555555rdword")
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByUsername("bob");
        boolean notExists = userRepository.existsByUsername("alice");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}