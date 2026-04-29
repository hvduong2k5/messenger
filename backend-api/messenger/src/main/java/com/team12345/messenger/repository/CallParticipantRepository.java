package com.team12345.messenger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team12345.messenger.entity.CallParticipant;
import com.team12345.messenger.entity.CallParticipantId;

@Repository
public interface CallParticipantRepository extends JpaRepository<CallParticipant, CallParticipantId>{
    @EntityGraph(value = "CallParticipant.withUser", type = EntityGraph.EntityGraphType.FETCH)
    List<CallParticipant> findById_CallId(Long callId);
}
