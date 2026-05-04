package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.response.UserProfileResponseDTO;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.exception.ResourceNotFoundException;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
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

    private UserProfileResponseDTO mapToResponse(User user) {
        return UserProfileResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .build();
    }
}