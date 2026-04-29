package com.team12345.messenger.repository;

import com.team12345.messenger.entity.Call;
import com.team12345.messenger.entity.CallStatus;
import com.team12345.messenger.entity.CallType;
import com.team12345.messenger.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
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
        List<Call> calls = callRepository.findByCallerId(user.getId());

        // Verify results
        assertThat(calls).hasSize(2);
        assertThat(calls).extracting("callType").containsExactlyInAnyOrder(CallType.audio, CallType.video);
        assertThat(calls.get(0).getCaller().getId()).isEqualTo(user.getId());
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
        List<Call> calls = callRepository.findByCallerId(user.getId());

        // Verify results
        assertThat(calls).isEmpty();
    }
}
