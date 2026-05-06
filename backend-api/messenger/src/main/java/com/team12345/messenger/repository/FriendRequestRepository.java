package com.team12345.messenger.repository;

import com.team12345.messenger.entity.FriendRequest;
import com.team12345.messenger.entity.FriendRequestId;
import com.team12345.messenger.entity.FriendRequestStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, FriendRequestId> {
    @EntityGraph(value = "FriendRequest.withReceiver", type = EntityGraph.EntityGraphType.FETCH)
    Page<FriendRequest> findBySender_IdAndStatus(Long senderId, FriendRequestStatus status, Pageable pageable);
    @EntityGraph(value = "FriendRequest.withSender", type = EntityGraph.EntityGraphType.FETCH)
    Page<FriendRequest> findByReceiver_IdAndStatus(Long receiverId, FriendRequestStatus status, Pageable pageable);
}
