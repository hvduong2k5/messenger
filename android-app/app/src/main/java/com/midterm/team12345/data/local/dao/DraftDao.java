package com.midterm.team12345.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.midterm.team12345.data.local.entity.DraftMessageEntity;

@Dao
public interface DraftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDraft(DraftMessageEntity draft);

    @Query("SELECT * FROM draft_messages WHERE conversation_id = :conversationId LIMIT 1")
    LiveData<DraftMessageEntity> getDraftByConversationId(Long conversationId);

    @Query("SELECT * FROM draft_messages WHERE conversation_id = :conversationId LIMIT 1")
    DraftMessageEntity getDraftByConversationIdSync(Long conversationId);

    @Query("DELETE FROM draft_messages WHERE conversation_id = :conversationId")
    void deleteDraftByConversationId(Long conversationId);

    @Query("DELETE FROM draft_messages")
    void deleteAllDrafts();
}
