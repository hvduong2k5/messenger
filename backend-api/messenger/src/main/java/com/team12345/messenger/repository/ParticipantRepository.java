package com.team12345.messenger.repository;

import com.team12345.messenger.entity.Participant;
import com.team12345.messenger.entity.ParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, ParticipantId> {

    @Query("SELECT p FROM Participant p WHERE p.id.userId = :userId")
    List<Participant> findByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Participant p WHERE p.id.conversationId = :conversationId")
    List<Participant> findByConversationId(@Param("conversationId") Long conversationId);
}
