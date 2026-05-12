package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.response.FriendRequestResponseDTO;
import com.team12345.messenger.dto.response.UserResponseDTO;
import com.team12345.messenger.entity.*;
import com.team12345.messenger.exception.ResourceNotFoundException;
import com.team12345.messenger.repository.FriendRequestRepository;
import com.team12345.messenger.repository.UserFriendRepository;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.service.FriendshipService;
// import com.team12345.messenger.service.NotificationService; // To be added when NotificationService is ready
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserFriendRepository userFriendRepository;
    // private final NotificationService notificationService;

    @Override
    @Transactional
    public FriendRequestResponseDTO sendFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalStateException("Cannot send a friend request to yourself");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        if (userFriendRepository.existsById_UserIdAndId_FriendId(senderId, receiverId)) {
            throw new IllegalStateException("Users are already friends");
        }

        Optional<FriendRequest> existingRequest = friendRequestRepository.findById_SenderIdAndId_ReceiverId(senderId, receiverId);
        if (existingRequest.isPresent() && existingRequest.get().getStatus() == FriendRequestStatus.pending) {
            throw new IllegalStateException("Friend request already pending");
        }

        // Also check if receiver already sent a request to sender
        Optional<FriendRequest> reverseRequest = friendRequestRepository.findById_SenderIdAndId_ReceiverId(receiverId, senderId);
        if (reverseRequest.isPresent() && reverseRequest.get().getStatus() == FriendRequestStatus.pending) {
             throw new IllegalStateException("User already sent you a friend request. Please accept it.");
        }

        FriendRequest friendRequest = FriendRequest.builder()
                .id(new FriendRequestId(senderId, receiverId))
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.pending)
                .build();

        FriendRequest savedRequest = friendRequestRepository.save(friendRequest);

        // notificationService.sendNotification(receiverId, "New friend request from " + sender.getUsername());

        return mapToFriendRequestResponse(savedRequest);
    }

    @Override
    @Transactional
    public void acceptFriendRequest(Long currentUserId, Long senderId) {
        FriendRequest friendRequest = friendRequestRepository.findById_SenderIdAndId_ReceiverId(senderId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        if (friendRequest.getStatus() != FriendRequestStatus.pending) {
            throw new IllegalStateException("Friend request is not pending");
        }

        friendRequest.setStatus(FriendRequestStatus.accepted);
        friendRequestRepository.save(friendRequest);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        UserFriend userFriend1 = UserFriend.builder()
                .id(new UserFriendId(currentUserId, senderId))
                .user(receiver)
                .friend(sender)
                .build();

        UserFriend userFriend2 = UserFriend.builder()
                .id(new UserFriendId(senderId, currentUserId))
                .user(sender)
                .friend(receiver)
                .build();

        userFriendRepository.saveAll(List.of(userFriend1, userFriend2));
    }

    @Override
    @Transactional
    public void declineFriendRequest(Long currentUserId, Long senderId) {
        FriendRequest friendRequest = friendRequestRepository.findById_SenderIdAndId_ReceiverId(senderId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        if (friendRequest.getStatus() != FriendRequestStatus.pending) {
            throw new IllegalStateException("Friend request is not pending");
        }

        friendRequest.setStatus(FriendRequestStatus.rejected);
        friendRequestRepository.save(friendRequest);
    }

    @Override
    @Transactional
    public void unfriend(Long currentUserId, Long friendId) {
        if (!userFriendRepository.existsById_UserIdAndId_FriendId(currentUserId, friendId)) {
             throw new IllegalStateException("Users are not friends");
        }

        userFriendRepository.deleteById(new UserFriendId(currentUserId, friendId));
        userFriendRepository.deleteById(new UserFriendId(friendId, currentUserId));

        // Optionally, reset or remove old friend requests
        friendRequestRepository.findById_SenderIdAndId_ReceiverId(currentUserId, friendId)
                .ifPresent(friendRequestRepository::delete);
        friendRequestRepository.findById_SenderIdAndId_ReceiverId(friendId, currentUserId)
                .ifPresent(friendRequestRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getFriendsList(Long userId) {
        List<UserFriend> userFriends = userFriendRepository.findById_UserId(userId);
        return userFriends.stream()
                .map(uf -> mapToUserResponse(uf.getFriend()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendRequestResponseDTO> getPendingRequests(Long userId) {
        List<FriendRequest> requests = friendRequestRepository.findByReceiverIdAndStatus(userId, FriendRequestStatus.pending);
        return requests.stream()
                .map(this::mapToFriendRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public String checkFriendshipStatus(Long userId1, Long userId2) {
        if (userId1.equals(userId2)) return "SELF";

        if (userFriendRepository.existsById_UserIdAndId_FriendId(userId1, userId2)) {
            return "FRIENDS";
        }

        Optional<FriendRequest> request1 = friendRequestRepository.findById_SenderIdAndId_ReceiverId(userId1, userId2);
        if (request1.isPresent() && request1.get().getStatus() == FriendRequestStatus.pending) {
            return "REQUEST_SENT";
        }

        Optional<FriendRequest> request2 = friendRequestRepository.findById_SenderIdAndId_ReceiverId(userId2, userId1);
        if (request2.isPresent() && request2.get().getStatus() == FriendRequestStatus.pending) {
            return "REQUEST_RECEIVED";
        }

        return "NONE";
    }

    private FriendRequestResponseDTO mapToFriendRequestResponse(FriendRequest request) {
        return FriendRequestResponseDTO.builder()
                .senderId(request.getSender().getId())
                .receiverId(request.getReceiver().getId())
                .senderUsername(request.getSender().getUsername())
                .senderAvatarUrl(request.getSender().getAvatarUrl())
                .status(request.getStatus().name())
                .build();
    }

    private UserResponseDTO mapToUserResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .isOnline(user.getIsOnline())
                .lastSeen(user.getLastSeen())
                .build();
    }
}