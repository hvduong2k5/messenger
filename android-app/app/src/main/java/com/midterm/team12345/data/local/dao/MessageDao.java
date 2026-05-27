package com.midterm.team12345.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.midterm.team12345.data.local.entity.DeliveryStatus;
import com.midterm.team12345.data.local.entity.MessageEntity;
import com.midterm.team12345.data.local.entity.SyncState;
import java.util.List;

@Dao
public interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertMessage(MessageEntity message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessages(List<MessageEntity> messages);

    @Update
    void updateMessage(MessageEntity message);

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY local_created_at DESC")
    LiveData<List<MessageEntity>> getMessagesByConversationId(Long conversationId);

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY local_created_at DESC")
    List<MessageEntity> getMessagesByConversationIdSync(Long conversationId);

    @Query("SELECT * FROM messages WHERE client_message_id = :clientMessageId LIMIT 1")
    MessageEntity getMessageByClientMessageId(String clientMessageId);

    @Query("SELECT * FROM messages WHERE message_id = :messageId LIMIT 1")
    MessageEntity getMessageByServerId(Long messageId);

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId AND sender_id = :senderId AND sync_state = 'PENDING' AND content = :content ORDER BY local_created_at ASC LIMIT 1")
    MessageEntity getPendingMessage(Long conversationId, Long senderId, String content);

    @Query("SELECT * FROM messages WHERE sync_state = :syncState")
    List<MessageEntity> getMessagesBySyncState(SyncState syncState);

    @Query("UPDATE messages SET message_id = :messageId, server_created_at = :serverCreatedAt, sync_state = :syncState, delivery_status = :deliveryStatus WHERE client_message_id = :clientMessageId")
    void updateSyncSuccess(String clientMessageId, Long messageId, Long serverCreatedAt, SyncState syncState, DeliveryStatus deliveryStatus);

    @Query("UPDATE messages SET sync_state = :syncState, retry_count = retry_count + 1, last_retry_at = :lastRetryAt WHERE client_message_id = :clientMessageId")
    void updateSyncFailure(String clientMessageId, SyncState syncState, Long lastRetryAt);

    @Query("DELETE FROM messages WHERE local_id = :localId")
    void deleteMessageByLocalId(Long localId);

    @Query("DELETE FROM messages WHERE message_id = :messageId")
    void deleteMessageByServerId(Long messageId);

    @Query("DELETE FROM messages WHERE conversation_id = :conversationId")
    void deleteMessagesByConversationId(Long conversationId);

    @Query("DELETE FROM messages")
    void deleteAllMessages();
}
