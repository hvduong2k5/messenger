package com.team12345.messenger.repository;

import com.team12345.messenger.entity.Conversation;
import com.team12345.messenger.entity.GroupSetting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GroupSettingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GroupSettingRepository groupSettingRepository;

    @Test
    void testFindByConversationId() {
        // Create a conversation
        Conversation conversation = Conversation.builder()
                .name("Group Chat")
                .isGroup(true)
                .build();
        entityManager.persistAndFlush(conversation);

        // Create group settings
        GroupSetting setting1 = GroupSetting.builder()
                .settingName("group_name")
                .settingValue("Group Chat Updated")
                .conversation(conversation)
                .build();
        GroupSetting setting2 = GroupSetting.builder()
                .settingName("allow_invite")
                .settingValue("true")
                .conversation(conversation)
                .build();
        
        entityManager.persist(setting1);
        entityManager.persist(setting2);
        entityManager.flush();

        // Test the query
        List<GroupSetting> settings = groupSettingRepository.findByConversationId(conversation.getId());

        // Verify results
        assertThat(settings).hasSize(2);
        assertThat(settings).extracting("settingName").containsExactlyInAnyOrder("group_name", "allow_invite");
        assertThat(settings.get(0).getConversation().getId()).isEqualTo(conversation.getId());
    }

    @Test
    void testFindByConversationId_NoSettings() {
        // Create a conversation without settings
        Conversation conversation = Conversation.builder()
                .name("Empty Group")
                .isGroup(true)
                .build();
        entityManager.persistAndFlush(conversation);

        // Test the query
        List<GroupSetting> settings = groupSettingRepository.findByConversationId(conversation.getId());

        // Verify results
        assertThat(settings).isEmpty();
    }
}
