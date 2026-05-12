package com.team12345.messenger.repository;

import com.team12345.messenger.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.user.id = :userId ORDER BY c.updatedAt DESC")
    Page<Conversation> findConversationsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT c FROM Conversation c JOIN c.participants p1 JOIN c.participants p2 WHERE c.isGroup = false AND p1.user.id = :userId1 AND p2.user.id = :userId2")
    java.util.Optional<Conversation> findOneToOneConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
