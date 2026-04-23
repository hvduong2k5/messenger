package com.team12345.messenger.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

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
public class CallSignalingJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testSaveCallSignaling() {
        User sender = User.builder()
                .username("signal_sender")
                .email("signal@example.com")
                .password("passbvbcvbcvbcvbcvbcvbvcbvcbvcbvcbvcbvcbcvbcbncvbcvbvcbvcbvcbc123")
                .build();
        entityManager.persistAndFlush(sender);

        String jsonData = "{\"type\":\"offer\",\"sdp\":\"v=0\\r\\n...\"}";

        CallSignaling signaling = CallSignaling.builder()
                .sender(sender)
                .signalType("offer")
                .data(jsonData)
                .build();

        CallSignaling savedSignaling = entityManager.persistAndFlush(signaling);

        assertThat(savedSignaling.getId()).isNotNull();
        assertThat(savedSignaling.getSender().getUsername()).isEqualTo("signal_sender");
        assertThat(savedSignaling.getSignalType()).isEqualTo("offer");
        assertThat(savedSignaling.getData()).isEqualTo(jsonData);
        assertThat(savedSignaling.getCreatedAt()).isNotNull();
    }
}