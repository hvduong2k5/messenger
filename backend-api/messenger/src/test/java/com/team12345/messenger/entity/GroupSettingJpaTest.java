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
        "spring.datasource.url=jdbc:h2:mem:testdb_groupsetting",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class GroupSettingJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testSaveGroupSetting() {
        Conversation conversation = Conversation.builder()
                .name("Group Chat")
                .isGroup(true)
                .build();
        entityManager.persistAndFlush(conversation);

        GroupSetting setting = GroupSetting.builder()
                .settingName("allow_invite")
                .settingValue("true")
                .conversation(conversation)
                .build();

        GroupSetting savedSetting = entityManager.persistAndFlush(setting);

        assertThat(savedSetting.getId()).isNotNull();
        assertThat(savedSetting.getSettingName()).isEqualTo("allow_invite");
        assertThat(savedSetting.getSettingValue()).isEqualTo("true");
        assertThat(savedSetting.getUpdatedAt()).isNotNull();
        assertThat(savedSetting.getConversation().getId()).isEqualTo(conversation.getId());
    }

    @Test
    public void testUpdateGroupSettingSetsUpdatedAt() {
        Conversation conversation = Conversation.builder()
                .name("Group Chat")
                .isGroup(true)
                .build();
        entityManager.persistAndFlush(conversation);

        GroupSetting setting = GroupSetting.builder()
                .settingName("group_description")
                .settingValue("Initial description")
                .conversation(conversation)
                .build();

        GroupSetting savedSetting = entityManager.persistAndFlush(setting);
        LocalDateTime initialUpdatedAt = savedSetting.getUpdatedAt();
        assertThat(initialUpdatedAt).isNotNull();

        // Wait a bit to ensure the timestamp will be different
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        savedSetting.setSettingValue("Updated description");
        GroupSetting updatedSetting = entityManager.persistAndFlush(savedSetting);

        assertThat(updatedSetting.getUpdatedAt()).isAfter(initialUpdatedAt);
    }

    @Test
    public void testSaveMultipleSettingsForConversation() {
        Conversation conversation = Conversation.builder()
                .name("Settings Chat")
                .isGroup(true)
                .build();
        entityManager.persistAndFlush(conversation);

        GroupSetting setting1 = GroupSetting.builder()
                .settingName("theme")
                .settingValue("dark")
                .conversation(conversation)
                .build();

        GroupSetting setting2 = GroupSetting.builder()
                .settingName("notifications")
                .settingValue("enabled")
                .conversation(conversation)
                .build();

        entityManager.persist(setting1);
        entityManager.persist(setting2);
        entityManager.flush();

        assertThat(entityManager.find(GroupSetting.class, setting1.getId())).isNotNull();
        assertThat(entityManager.find(GroupSetting.class, setting2.getId())).isNotNull();
    }
}
