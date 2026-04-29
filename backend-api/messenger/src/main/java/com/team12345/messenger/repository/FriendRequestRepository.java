package com.team12345.messenger.repository;

import com.team12345.messenger.entity.FriendRequest;
import com.team12345.messenger.entity.FriendRequestId;
import com.team12345.messenger.entity.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, FriendRequestId> {
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, FriendRequestStatus status);
    List<FriendRequest> findBySenderIdAndStatus(Long senderId, FriendRequestStatus status);
}
