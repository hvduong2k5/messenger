package com.team12345.messenger.repository;

import com.team12345.messenger.entity.Participant;
import com.team12345.messenger.entity.ParticipantId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
@Repository
public interface ParticipantRepository extends JpaRepository<Participant, ParticipantId> {

    @EntityGraph(value = "Participant.conversation", type = EntityGraph.EntityGraphType.FETCH)
    List<Participant> findById_UserId(Long userId);

    @EntityGraph(value = "Participant.fullDetail", type = EntityGraph.EntityGraphType.FETCH)
    List<Participant> findById_ConversationId(Long conversationId);

    @EntityGraph(value = "Participant.fullDetail", type = EntityGraph.EntityGraphType.FETCH)
    Page<Participant> findById_ConversationIdAndUser_UsernameContainingIgnoreCase(Long conversationId, String keyword, org.springframework.data.domain.Pageable pageable);

    @EntityGraph(value = "Participant.fullDetail", type = EntityGraph.EntityGraphType.FETCH)
    Page<Participant> findById_ConversationId(Long conversationId, org.springframework.data.domain.Pageable pageable);

    long countById_ConversationIdAndRole(Long conversationId, com.team12345.messenger.entity.ParticipantRole role);
}

