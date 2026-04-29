package com.team12345.messenger.repository;

import com.team12345.messenger.entity.User;
import com.team12345.messenger.entity.UserFriend;
import com.team12345.messenger.entity.UserFriendId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
class UserFriendRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserFriendRepository userFriendRepository;

    @Test
    void testFindByUserId() {
        // Create users
        String commonPassword = "password123456789012345678901234567890123456789012345678901234567890";
        User user = User.builder()
                .username("main_user")
                .email("main@example.com")
                .password(commonPassword)
                .build();
        User friend1 = User.builder()
                .username("friend1")
                .email("f1@example.com")
                .password(commonPassword)
                .build();
        User friend2 = User.builder()
                .username("friend2")
                .email("f2@example.com")
                .password(commonPassword)
                .build();
        
        entityManager.persistAndFlush(user);
        entityManager.persistAndFlush(friend1);
        entityManager.persistAndFlush(friend2);

        // Create friendships
        UserFriend uf1 = UserFriend.builder()
                .id(new UserFriendId(user.getId(), friend1.getId()))
                .user(user)
                .friend(friend1)
                .build();
        UserFriend uf2 = UserFriend.builder()
                .id(new UserFriendId(user.getId(), friend2.getId()))
                .user(user)
                .friend(friend2)
                .build();
        
        entityManager.persist(uf1);
        entityManager.persist(uf2);
        entityManager.flush();

        // Test the query
        Page<UserFriend> friendsPage = userFriendRepository.findById_UserId(user.getId(), PageRequest.of(0, 10));
        List<UserFriend> friends = friendsPage.getContent();

        // Verify results
        assertThat(friends).hasSize(2);
        assertThat(friends).extracting("friend").extracting("username")
                .containsExactlyInAnyOrder("friend1", "friend2");
    }

    @Test
    void testFindByUserId_NoFriends() {
        // Create a user with no friends
        User user = User.builder()
                .username("lonely_user")
                .email("lonely@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(user);

        // Test the query
        Page<UserFriend> friendsPage = userFriendRepository.findById_UserId(user.getId(), PageRequest.of(0, 10));
        List<UserFriend> friends = friendsPage.getContent();

        // Verify results
        assertThat(friends).isEmpty();
    }
}
