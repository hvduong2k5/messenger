package com.team12345.messenger.service;

import com.team12345.messenger.dto.response.UserProfileResponseDTO;
import com.team12345.messenger.dto.response.UserResponseDTO;
import com.team12345.messenger.dto.response.UserSearchResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserProfileResponseDTO getProfile(Long userId);
    
    UserProfileResponseDTO getProfileByUsername(String username);

    UserProfileResponseDTO updateStatus(Long userId, String status);

    UserProfileResponseDTO updateAvatar(Long userId, String avatarUrl);
    
    List<UserResponseDTO> searchUsers(String query, Long excludeUserId);

    Page<UserSearchResponseDTO> searchUsers(String query, Pageable pageable, Long currentUserId);
}