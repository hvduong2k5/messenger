package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.response.UserProfileResponseDTO;
import com.team12345.messenger.dto.response.UserResponseDTO;
import com.team12345.messenger.dto.response.UserSearchResponseDTO;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.entity.FriendRequest;
import com.team12345.messenger.entity.FriendRequestStatus;
import com.team12345.messenger.exception.ResourceNotFoundException;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.repository.UserFriendRepository;
import com.team12345.messenger.repository.FriendRequestRepository;
import com.team12345.messenger.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserFriendRepository userFriendRepository;
    private final FriendRequestRepository friendRequestRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToResponse(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDTO getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponseDTO updateStatus(Long userId, String status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setStatus(status);

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserProfileResponseDTO updateAvatar(Long userId, String avatarUrl) {
        if (avatarUrl == null) {
            throw new IllegalArgumentException("Avatar URL cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setAvatarUrl(avatarUrl);

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> searchUsers(String query, Long excludeUserId) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
        return users.stream()
                .filter(u -> !u.getId().equals(excludeUserId))
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSearchResponseDTO> searchUsers(String query, Pageable pageable, Long currentUserId) {
        Page<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, pageable);
        return users.map(user -> {
            if (user.getId().equals(currentUserId)) {
                return null; // sẽ filter sau
            }
            String status = "STRANGER";
            if (userFriendRepository.existsById_UserIdAndId_FriendId(currentUserId, user.getId()) ||
                userFriendRepository.existsById_UserIdAndId_FriendId(user.getId(), currentUserId)) {
                status = "FRIEND";
            } else if (friendRequestRepository.findById_SenderIdAndId_ReceiverId(currentUserId, user.getId())
                    .filter(fr -> fr.getStatus() == FriendRequestStatus.pending).isPresent()) {
                status = "SENDER_PENDING";
            } else if (friendRequestRepository.findById_SenderIdAndId_ReceiverId(user.getId(), currentUserId)
                    .filter(fr -> fr.getStatus() == FriendRequestStatus.pending).isPresent()) {
                status = "RECEIVER_PENDING";
            }
            return UserSearchResponseDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .avatarUrl(user.getAvatarUrl())
                    .friendshipStatus(status)
                    .build();
        }).filter(dto -> dto != null);
    }

    private UserProfileResponseDTO mapToResponse(User user) {
        return UserProfileResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .build();
    }
    
    private UserResponseDTO mapToUserResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .build();
    }
}