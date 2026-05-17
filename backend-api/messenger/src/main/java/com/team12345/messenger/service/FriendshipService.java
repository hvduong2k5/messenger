package com.team12345.messenger.service;

import com.team12345.messenger.dto.response.FriendRequestResponseDTO;
import com.team12345.messenger.dto.response.UserResponseDTO;

import java.util.List;

public interface FriendshipService {

    FriendRequestResponseDTO sendFriendRequest(Long senderId, Long receiverId);

    void acceptFriendRequest(Long currentUserId, Long senderId);

    void declineFriendRequest(Long currentUserId, Long senderId);

    void unfriend(Long currentUserId, Long friendId);

    List<UserResponseDTO> getFriendsList(Long userId);

    List<FriendRequestResponseDTO> getPendingRequests(Long userId);

    void cancelFriendRequest(Long currentUserId, Long receiverId);

    String checkFriendshipStatus(Long userId1, Long userId2);
}