package com.team12345.messenger.repository;

import com.team12345.messenger.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @EntityGraph(value = "Message.withSender")
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    @EntityGraph(value = "Message.withSender")
    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);

    @EntityGraph(value = "Message.withSender")
    Page<Message> findByConversationIdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(Long conversationId, String keyword, Pageable pageable);

    @EntityGraph(value = "Message.withSender")
    Page<Message> findByContentContainingIgnoreCaseOrderByCreatedAtDesc(String keyword, Pageable pageable);
}
