package com.team12345.messenger.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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
public class CallParticipantJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testSaveCallParticipant() {
        // 1. Create a user
        User user = User.builder()
                .username("participant_user")
                .email("participant@example.com")
                .password("passdsaaaaaaaaaaaaaaaaaaaaaaaaaaadxxxxxxxxxxxxxxxxxxxxxxxxxxxxvxccccccccx123")
                .build();
        entityManager.persistAndFlush(user);

        // 2. Create a caller user and a call
        User caller = User.builder()
                .username("caller")
                .email("caller2@example.com")
                .password("pass1passdsaaaaaaaaaaaaaaaaaaaaaaaaaaadxxxxxxxxxxxxxxxxxxxxxxxxxxxxvxccccccccx12323")
                .build();
        entityManager.persistAndFlush(caller);

        Call call = Call.builder()
                .caller(caller)
                .callType(CallType.audio)
                .status(CallStatus.connected)
                .startedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(call);

        // 3. Create CallParticipant
        CallParticipantId participantId = new CallParticipantId(call.getId(), user.getId());
        CallParticipant callParticipant = CallParticipant.builder()
                .id(participantId)
                .call(call)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .leftAt(LocalDateTime.now().plusMinutes(10))
                .build();

        CallParticipant savedParticipant = entityManager.persistAndFlush(callParticipant);

        // 4. Verify
        assertThat(savedParticipant.getId()).isNotNull();
        assertThat(savedParticipant.getCall().getId()).isEqualTo(call.getId());
        assertThat(savedParticipant.getUser().getUsername()).isEqualTo("participant_user");
        assertThat(savedParticipant.getJoinedAt()).isNotNull();
        assertThat(savedParticipant.getLeftAt()).isNotNull();
    }
}