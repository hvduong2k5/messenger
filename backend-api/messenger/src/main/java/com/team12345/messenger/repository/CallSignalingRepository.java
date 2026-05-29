package com.team12345.messenger.repository;

import com.team12345.messenger.entity.CallSignaling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallSignalingRepository extends JpaRepository<CallSignaling, Long> {

    List<CallSignaling> findBySender_IdOrderByCreatedAtAsc(Long senderId);

    List<CallSignaling> findByCall_IdOrderByCreatedAtAsc(Long callId);
}
