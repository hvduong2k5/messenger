package com.team12345.messenger.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.team12345.messenger.entity.Call;

@Repository
public interface CallRepository extends JpaRepository<Call, Long>{
    @EntityGraph(value = "Call.withReceiver", type = EntityGraph.EntityGraphType.FETCH)
    Slice<Call> findByCaller_Id(Long callerId, Pageable pageable);

    @EntityGraph(value = "Call.withParticipants", type = EntityGraph.EntityGraphType.FETCH)
    Slice<Call> findByCaller_IdOrReceiver_Id(Long callerId, Long receiverId, Pageable pageable);
}
