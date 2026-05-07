package com.team12345.messenger.repository;

import com.team12345.messenger.entity.MessageStatus;
import com.team12345.messenger.entity.MessageStatusEnum;
import com.team12345.messenger.entity.MessageStatusEnum;
import com.team12345.messenger.entity.MessageStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, MessageStatusId> {

    long countById_ReceiverIdAndStatusNot(Long receiverId, MessageStatusEnum status);

    @Query("SELECT COUNT(ms) FROM MessageStatus ms WHERE ms.id.receiverId = :receiverId AND ms.message.conversation.id = :conversationId AND ms.status != :status")
    long countUnreadInConversation(@Param("receiverId") Long receiverId,
                                   @Param("conversationId") Long conversationId,
                                   @Param("status") MessageStatusEnum status);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE MessageStatus ms SET ms.status = :newStatus WHERE ms.id.receiverId = :receiverId AND ms.id.messageId = :messageId")
    int markAsReadByMessageId(@Param("receiverId") Long receiverId,
                              @Param("messageId") Long messageId,
                              @Param("newStatus") MessageStatusEnum newStatus);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE MessageStatus ms SET ms.status = :newStatus " +
           "WHERE ms.id.receiverId = :receiverId " +
           "AND ms.id.messageId IN (SELECT m.id FROM Message m WHERE m.conversation.id = :conversationId)")
    int markAsReadByConversationId(@Param("receiverId") Long receiverId,
                                   @Param("conversationId") Long conversationId,
                                   @Param("newStatus") MessageStatusEnum newStatus);
}
