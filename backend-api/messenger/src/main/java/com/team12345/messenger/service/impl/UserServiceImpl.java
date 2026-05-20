package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.request.UpdateProfileRequestDTO;
import com.team12345.messenger.dto.response.UserProfileResponseDTO;
import com.team12345.messenger.dto.response.UserResponseDTO;
import com.team12345.messenger.dto.response.UserSearchResponseDTO;
import com.team12345.messenger.dto.response.FriendshipStatus;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.entity.FriendRequestStatus;
import com.team12345.messenger.exception.InvalidCredentialsException;
import com.team12345.messenger.exception.ResourceNotFoundException;
import com.team12345.messenger.exception.UserAlreadyExistsException;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.repository.UserFriendRepository;
import com.team12345.messenger.repository.FriendRequestRepository;
import com.team12345.messenger.service.UserService;
import com.team12345.messenger.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserFriendRepository userFriendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final MediaService mediaService;

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
    @Transactional
    public UserProfileResponseDTO updateProfile(Long userId, MultipartFile avatar, UpdateProfileRequestDTO request) {
        // Step 1: Find the current user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Step 2: Security Check - Validate old password if email or password is being changed
        if (request != null && (
                (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) ||
                (request.getPassword() != null && !request.getPassword().isEmpty())
        )) {
            if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
                throw new IllegalArgumentException("Mật khẩu cũ là bắt buộc");
            }

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new InvalidCredentialsException("Mật khẩu cũ không chính xác");
            }
        }

        // Step 3: Update Email if provided
        if (request != null && request.getEmail() != null && !request.getEmail().isEmpty()) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equals(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new UserAlreadyExistsException("Email is already registered: " + newEmail);
                }
                user.setEmail(newEmail);
            }
        }

        // Step 4: Update Password if provided
        if (request != null && request.getPassword() != null && !request.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            user.setPassword(encodedPassword);
        }

        // Step 5: Upload Avatar if provided
        if (avatar != null && !avatar.isEmpty()) {
            Map<String, Object> uploadResult = mediaService.uploadFile(avatar);
            String avatarUrl = (String) uploadResult.get("url");
            user.setAvatarUrl(avatarUrl);
        }

        // Step 6: Save and return response
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
        // Loại bỏ bản thân khỏi kết quả bằng stream, rồi tạo lại Page
        List<User> filteredList = users.getContent().stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .collect(Collectors.toList());
        Page<User> filtered = new org.springframework.data.domain.PageImpl<>(filteredList, pageable, users.getTotalElements() - 1);
        return filtered.map(user -> {
            FriendshipStatus status = FriendshipStatus.STRANGER;
            if (userFriendRepository.existsById_UserIdAndId_FriendId(currentUserId, user.getId()) ||
                userFriendRepository.existsById_UserIdAndId_FriendId(user.getId(), currentUserId)) {
                status = FriendshipStatus.FRIEND;
            } else if (friendRequestRepository.findById_SenderIdAndId_ReceiverId(currentUserId, user.getId())
                    .filter(fr -> fr.getStatus() == FriendRequestStatus.pending).isPresent()) {
                status = FriendshipStatus.SENDER_PENDING;
            } else if (friendRequestRepository.findById_SenderIdAndId_ReceiverId(user.getId(), currentUserId)
                    .filter(fr -> fr.getStatus() == FriendRequestStatus.pending).isPresent()) {
                status = FriendshipStatus.RECEIVER_PENDING;
            }
            return UserSearchResponseDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .avatarUrl(user.getAvatarUrl())
                    .friendshipStatus(status)
                    .isOnline(user.getIsOnline())
                    .lastSeen(user.getLastSeen())
                    .build();
        });
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
                .isOnline(user.getIsOnline())
                .lastSeen(user.getLastSeen())
                .build();
    }
}