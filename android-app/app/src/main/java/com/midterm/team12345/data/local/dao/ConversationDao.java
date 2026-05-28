package com.midterm.team12345.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.midterm.team12345.data.local.entity.ConversationEntity;
import java.util.List;

@Dao
public interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertConversation(ConversationEntity conversation);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertConversations(List<ConversationEntity> conversations);

    @Update
    void updateConversation(ConversationEntity conversation);

    @Query("SELECT * FROM conversations ORDER BY last_message_created_at DESC")
    LiveData<List<ConversationEntity>> getConversations();

    @Query("SELECT * FROM conversations WHERE name LIKE :searchQuery ORDER BY last_message_created_at DESC")
    LiveData<List<ConversationEntity>> searchConversations(String searchQuery);

    @Query("SELECT * FROM conversations ORDER BY last_message_created_at DESC")
    List<ConversationEntity> getConversationsSync();

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    LiveData<ConversationEntity> getConversationById(Long id);

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    ConversationEntity getConversationByIdSync(Long id);

    @Query("UPDATE conversations SET last_message_content = :content, last_message_sender_id = :senderId, last_message_created_at = :createdAt, updated_at = :createdAt WHERE id = :conversationId")
    void updateLastMessage(Long conversationId, String content, Long senderId, Long createdAt);

    @Query("UPDATE conversations SET unread_count = :count WHERE id = :conversationId")
    void updateUnreadCount(Long conversationId, Integer count);

    @Query("UPDATE conversations SET unread_count = unread_count + 1 WHERE id = :conversationId")
    void incrementUnreadCount(Long conversationId);

    @Query("UPDATE conversations SET unread_count = 0 WHERE id = :conversationId")
    void clearUnreadCount(Long conversationId);

    @Query("UPDATE conversations SET unread_count = 0 WHERE id = :conversationId")
    void resetUnreadCount(Long conversationId);

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    void deleteConversationById(Long conversationId);

    @Query("DELETE FROM conversations")
    void deleteAllConversations();
}
