package com.team12345.messenger.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class FriendRequestJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    private String longPassword() {
        return "p".repeat(60); // create a dummy BCrypt-length password for tests
    }

    @Test
    public void testSaveFriendRequest() {
        User sender = User.builder()
                .username("sender")
                .email("sender@example.com")
                .password(longPassword())
                .build();
        entityManager.persist(sender);

        User receiver = User.builder()
                .username("receiver")
                .email("receiver@example.com")
                .password(longPassword())
                .build();
        entityManager.persist(receiver);

        FriendRequestId id = new FriendRequestId(sender.getId(), receiver.getId());
        FriendRequest friendRequest = FriendRequest.builder()
                .id(id)
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.pending)
                .build();

        FriendRequest savedFriendRequest = entityManager.persistAndFlush(friendRequest);

        assertThat(savedFriendRequest.getId()).isNotNull();
        assertThat(savedFriendRequest.getSender().getUsername()).isEqualTo("sender");
        assertThat(savedFriendRequest.getReceiver().getUsername()).isEqualTo("receiver");
        assertThat(savedFriendRequest.getStatus()).isEqualTo(FriendRequestStatus.pending);
    }
}
