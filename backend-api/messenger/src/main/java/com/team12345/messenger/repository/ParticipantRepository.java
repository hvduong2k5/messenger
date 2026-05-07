package com.team12345.messenger.repository;

import com.team12345.messenger.entity.Participant;
import com.team12345.messenger.entity.ParticipantId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, ParticipantId> {

    @EntityGraph(value = "Participant.conversation", type = EntityGraph.EntityGraphType.FETCH)
    List<Participant> findById_UserId(Long userId);

    @EntityGraph(value = "Participant.fullDetail", type = EntityGraph.EntityGraphType.FETCH)
    List<Participant> findById_ConversationId(Long conversationId);

}

