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
public class ConversationJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testSaveConversation() {
        Conversation conversation = Conversation.builder()
                .name("General Chat")
                .isGroup(true)
                .build();

        Conversation savedConversation = entityManager.persistAndFlush(conversation);

        assertThat(savedConversation.getId()).isNotNull();
        assertThat(savedConversation.getName()).isEqualTo("General Chat");
        assertThat(savedConversation.getIsGroup()).isTrue();
        assertThat(savedConversation.getCreatedAt()).isNotNull();
    }

    @Test
    public void testSaveConversationDefaultValues() {
        Conversation conversation = new Conversation();

        Conversation savedConversation = entityManager.persistAndFlush(conversation);

        assertThat(savedConversation.getId()).isNotNull();
        assertThat(savedConversation.getIsGroup()).isFalse();
        assertThat(savedConversation.getCreatedAt()).isNotNull();
        assertThat(savedConversation.getName()).isNull();
    }
}