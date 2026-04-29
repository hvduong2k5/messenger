package com.team12345.messenger.repository;

import com.team12345.messenger.entity.FriendRequest;
import com.team12345.messenger.entity.FriendRequestId;
import com.team12345.messenger.entity.FriendRequestStatus;
import com.team12345.messenger.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
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
class FriendRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Test
    void testFindPendingRequests() {
        // Create users
        String commonPassword = "password123456789012345678901234567890123456789012345678901234567890";
        User sender1 = User.builder().username("sender1").email("s1@example.com").password(commonPassword).build();
        User sender2 = User.builder().username("sender2").email("s2@example.com").password(commonPassword).build();
        User receiver = User.builder().username("receiver").email("r@example.com").password(commonPassword).build();
        
        entityManager.persistAndFlush(sender1);
        entityManager.persistAndFlush(sender2);
        entityManager.persistAndFlush(receiver);

        // Create friend requests
        FriendRequest fr1 = FriendRequest.builder()
                .id(new FriendRequestId(sender1.getId(), receiver.getId()))
                .sender(sender1)
                .receiver(receiver)
                .status(FriendRequestStatus.pending)
                .build();
        
        FriendRequest fr2 = FriendRequest.builder()
                .id(new FriendRequestId(sender2.getId(), receiver.getId()))
                .sender(sender2)
                .receiver(receiver)
                .status(FriendRequestStatus.accepted)
                .build();
        
        entityManager.persist(fr1);
        entityManager.persist(fr2);
        entityManager.flush();

        // Test finding pending requests
        List<FriendRequest> pendingRequests = friendRequestRepository.findByReceiverIdAndStatus(receiver.getId(), FriendRequestStatus.pending);

        // Verify
        assertThat(pendingRequests).hasSize(1);
        assertThat(pendingRequests.get(0).getSender().getUsername()).isEqualTo("sender1");
    }

    @Test
    void testUpdateStatus() {
        // Create users
        String commonPassword = "password123456789012345678901234567890123456789012345678901234567890";
        User sender = User.builder().username("sender_u").email("su@example.com").password(commonPassword).build();
        User receiver = User.builder().username("receiver_u").email("ru@example.com").password(commonPassword).build();
        
        entityManager.persistAndFlush(sender);
        entityManager.persistAndFlush(receiver);

        // Create a pending request
        FriendRequestId id = new FriendRequestId(sender.getId(), receiver.getId());
        FriendRequest fr = FriendRequest.builder()
                .id(id)
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.pending)
                .build();
        
        entityManager.persistAndFlush(fr);

        // Update status
        Optional<FriendRequest> foundFr = friendRequestRepository.findById(id);
        assertThat(foundFr).isPresent();
        foundFr.get().setStatus(FriendRequestStatus.accepted);
        friendRequestRepository.saveAndFlush(foundFr.get());

        // Verify updated status
        Optional<FriendRequest> updatedFr = friendRequestRepository.findById(id);
        assertThat(updatedFr).isPresent();
        assertThat(updatedFr.get().getStatus()).isEqualTo(FriendRequestStatus.accepted);
    }
}
