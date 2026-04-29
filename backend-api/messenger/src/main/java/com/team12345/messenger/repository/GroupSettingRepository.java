package com.team12345.messenger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team12345.messenger.entity.GroupSetting;

@Repository
public interface GroupSettingRepository extends JpaRepository<GroupSetting, Long>{
    public List<GroupSetting> findByConversationId(long conversationId);
}
