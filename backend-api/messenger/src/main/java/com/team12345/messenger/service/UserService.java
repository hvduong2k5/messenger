package com.team12345.messenger.service;

import com.team12345.messenger.dto.response.UserProfileResponseDTO;

public interface UserService {

    UserProfileResponseDTO getProfile(Long userId);

    UserProfileResponseDTO updateStatus(Long userId, String status);

    UserProfileResponseDTO updateAvatar(Long userId, String avatarUrl);
}