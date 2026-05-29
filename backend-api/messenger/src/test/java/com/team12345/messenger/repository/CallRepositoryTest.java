package com.team12345.messenger.repository;

import com.team12345.messenger.entity.Call;
import com.team12345.messenger.entity.CallStatus;
import com.team12345.messenger.entity.CallType;
import com.team12345.messenger.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

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
class CallRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CallRepository callRepository;

    @Test
    void testFindByCallerId() {
        // Create a user
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(user);

        // Create calls
        Call call1 = Call.builder()
                .caller(user)
                .callType(CallType.audio)
                .status(CallStatus.connected)
                .startedAt(LocalDateTime.now().minusMinutes(10))
                .build();
        
        Call call2 = Call.builder()
                .caller(user)
                .callType(CallType.video)
                .status(CallStatus.ended)
                .startedAt(LocalDateTime.now().minusHours(1))
                .endedAt(LocalDateTime.now().minusMinutes(50))
                .build();

        entityManager.persist(call1);
        entityManager.persist(call2);
        entityManager.flush();

        // Test the query
        Slice<Call> calls = callRepository.findByCaller_Id(user.getId(), PageRequest.of(0, 10));

        // Verify results
        assertThat(calls.getContent()).hasSize(2);
        assertThat(calls.getContent()).extracting("callType").containsExactlyInAnyOrder(CallType.audio, CallType.video);
        assertThat(calls.getContent().get(0).getCaller().getId()).isEqualTo(user.getId());
    }

    @Test
    void testFindByCallerId_NoCalls() {
        // Create a user without calls
        User user = User.builder()
                .username("nocalls")
                .email("nocalls@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(user);

        // Test the query
        Slice<Call> calls = callRepository.findByCaller_Id(user.getId(), PageRequest.of(0, 10));

        // Verify results
        assertThat(calls.getContent()).isEmpty();
    }
    @Test
    void testFindByCallerIdOrReceiverId() {
        // Create users
        User user1 = User.builder()
                .username("user1")
                .email("u1@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        User user2 = User.builder()
                .username("user2")
                .email("u2@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        // Create calls
        // Call 1: user1 is caller, user2 is receiver
        Call call1 = Call.builder()
                .caller(user1)
                .receiver(user2)
                .callType(CallType.audio)
                .status(CallStatus.connected)
                .startedAt(LocalDateTime.now())
                .build();
        
        // Call 2: user2 is caller, user1 is receiver
        Call call2 = Call.builder()
                .caller(user2)
                .receiver(user1)
                .callType(CallType.video)
                .status(CallStatus.ended)
                .startedAt(LocalDateTime.now())
                .build();

        entityManager.persist(call1);
        entityManager.persist(call2);
        entityManager.flush();

        // Test finding calls where user1 is either caller or receiver
        Slice<Call> calls = callRepository.findByCaller_IdOrReceiver_Id(user1.getId(), user1.getId(), PageRequest.of(0, 10));

        // Verify results
        assertThat(calls.getContent()).hasSize(2);
    }
}
