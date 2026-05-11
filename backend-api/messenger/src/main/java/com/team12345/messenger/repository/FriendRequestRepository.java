package com.team12345.messenger.repository;

import com.team12345.messenger.entity.FriendRequest;
import com.team12345.messenger.entity.FriendRequestId;
import com.team12345.messenger.entity.FriendRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, FriendRequestId> {

    @EntityGraph(value = "FriendRequest.withSender")
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, FriendRequestStatus status);
    
    @EntityGraph(value = "FriendRequest.withSender")
    Page<FriendRequest> findByReceiver_IdAndStatus(Long receiverId, FriendRequestStatus status, Pageable pageable);
    
    Optional<FriendRequest> findById_SenderIdAndId_ReceiverId(Long senderId, Long receiverId);
}