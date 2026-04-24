package com.team12345.messenger.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class UserFriendJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    private String longPassword() {
        return "p".repeat(60); // create a dummy BCrypt-length password for tests
    }

    @Test
    public void testSaveUserFriend() {
        User user = User.builder()
                .username("user")
                .email("user@example.com")
                .password(longPassword())
                .build();
        entityManager.persist(user);

        User friend = User.builder()
                .username("friend")
                .email("friend@example.com")
                .password(longPassword())
                .build();
        entityManager.persist(friend);

        UserFriendId id = new UserFriendId(user.getId(), friend.getId());
        UserFriend userFriend = UserFriend.builder()
                .id(id)
                .user(user)
                .friend(friend)
                .build();

        UserFriend savedUserFriend = entityManager.persistAndFlush(userFriend);

        assertThat(savedUserFriend.getId()).isNotNull();
        assertThat(savedUserFriend.getUser().getUsername()).isEqualTo("user");
        assertThat(savedUserFriend.getFriend().getUsername()).isEqualTo("friend");
        assertThat(savedUserFriend.getCreatedAt()).isNotNull();
    }
}
