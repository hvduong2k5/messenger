package com.team12345.messenger.repository;

import com.team12345.messenger.entity.*;
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
class CallParticipantRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CallParticipantRepository callParticipantRepository;

    @Test
    void testFindByCallId() {
        // Create users
        User caller = User.builder()
                .username("caller")
                .email("caller@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        User participantUser = User.builder()
                .username("participant")
                .email("participant@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(caller);
        entityManager.persistAndFlush(participantUser);

        // Create a call
        Call call = Call.builder()
                .caller(caller)
                .callType(CallType.audio)
                .status(CallStatus.connected)
                .startedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(call);

        // Create call participants
        CallParticipant participant1 = CallParticipant.builder()
                .id(new CallParticipantId(call.getId(), caller.getId()))
                .call(call)
                .user(caller)
                .joinedAt(LocalDateTime.now())
                .build();
        
        CallParticipant participant2 = CallParticipant.builder()
                .id(new CallParticipantId(call.getId(), participantUser.getId()))
                .call(call)
                .user(participantUser)
                .joinedAt(LocalDateTime.now())
                .build();

        entityManager.persist(participant1);
        entityManager.persist(participant2);
        entityManager.flush();

        // Test the query
        List<CallParticipant> participants = callParticipantRepository.findById_CallId(call.getId());

        // Verify results
        assertThat(participants).hasSize(2);
        assertThat(participants).extracting("user").extracting("username")
                .containsExactlyInAnyOrder("caller", "participant");
    }

    @Test
    void testFindByCallId_NoParticipants() {
        // Create a user and a call without participants (initially)
        User caller = User.builder()
                .username("caller2")
                .email("caller2@example.com")
                .password("password123456789012345678901234567890123456789012345678901234567890")
                .build();
        entityManager.persistAndFlush(caller);

        Call call = Call.builder()
                .caller(caller)
                .callType(CallType.video)
                .status(CallStatus.ringing)
                .startedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(call);

        // Test the query
        List<CallParticipant> participants = callParticipantRepository.findById_CallId(call.getId());

        // Verify results
        assertThat(participants).isEmpty();
    }
}
