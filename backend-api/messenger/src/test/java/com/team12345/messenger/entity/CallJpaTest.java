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
public class CallJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testSaveCall() {
        User caller = User.builder()
                .username("caller_user")
                .email("caller@example.com")
                .password("pass1gddgdfvxgssxsxsxsxsxsxsxsxsxsaxaxaxaxasxasxaxasxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxdgdsgfsdgsdfgsgsdfgsdg23")
                .build();
        entityManager.persistAndFlush(caller);

        LocalDateTime now = LocalDateTime.now();

        Call call = Call.builder()
                .caller(caller)
                .callType(CallType.video)
                .status(CallStatus.connected)
                .startedAt(now)
                .endedAt(now.plusMinutes(5))
                .build();

        Call savedCall = entityManager.persistAndFlush(call);

        assertThat(savedCall.getId()).isNotNull();
        assertThat(savedCall.getCaller().getUsername()).isEqualTo("caller_user");
        assertThat(savedCall.getCallType()).isEqualTo(CallType.video);
        assertThat(savedCall.getStatus()).isEqualTo(CallStatus.connected);
        assertThat(savedCall.getStartedAt()).isNotNull();
        assertThat(savedCall.getEndedAt()).isNotNull();
        assertThat(savedCall.getCreatedAt()).isNotNull();
    }
}