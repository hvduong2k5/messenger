package com.midterm.team12345.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.midterm.team12345.data.local.entity.ConversationParticipantEntity;
import java.util.List;

@Dao
public interface ConversationParticipantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertParticipant(ConversationParticipantEntity participant);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertParticipants(List<ConversationParticipantEntity> participants);

    @Query("SELECT * FROM conversation_participants WHERE conversation_id = :conversationId")
    LiveData<List<ConversationParticipantEntity>> getParticipantsForConversation(Long conversationId);

    @Query("SELECT * FROM conversation_participants WHERE conversation_id = :conversationId")
    List<ConversationParticipantEntity> getParticipantsForConversationSync(Long conversationId);

    @Query("DELETE FROM conversation_participants WHERE conversation_id = :conversationId AND user_id = :userId")
    void deleteParticipant(Long conversationId, Long userId);

    @Query("DELETE FROM conversation_participants WHERE conversation_id = :conversationId")
    void deleteParticipantsByConversationId(Long conversationId);

    @Query("DELETE FROM conversation_participants")
    void deleteAllParticipants();
}
