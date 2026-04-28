package com.team12345.messenger.repository;

import com.team12345.messenger.entity.MessageStatus;
import com.team12345.messenger.entity.MessageStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, MessageStatusId> {

    @Query("SELECT COUNT(ms) FROM MessageStatus ms WHERE ms.id.receiverId = :receiverId AND ms.status != 'read'")
    long countUnreadByReceiverId(@Param("receiverId") Long receiverId);

    @Query("SELECT COUNT(ms) FROM MessageStatus ms WHERE ms.id.receiverId = :receiverId AND ms.message.conversation.id = :conversationId AND ms.status != 'read'")
    long countUnreadByReceiverIdAndConversationId(@Param("receiverId") Long receiverId, @Param("conversationId") Long conversationId);

    @Modifying
    @Query("UPDATE MessageStatus ms SET ms.status = 'read' WHERE ms.id.receiverId = :receiverId AND ms.id.messageId = :messageId")
    int markAsReadByReceiverIdAndMessageId(@Param("receiverId") Long receiverId, @Param("messageId") Long messageId);

    @Modifying
    @Query("UPDATE MessageStatus ms SET ms.status = 'read' WHERE ms.id.receiverId = :receiverId AND ms.message.conversation.id = :conversationId")
    int markAsReadByReceiverIdAndConversationId(@Param("receiverId") Long receiverId, @Param("conversationId") Long conversationId);
}
