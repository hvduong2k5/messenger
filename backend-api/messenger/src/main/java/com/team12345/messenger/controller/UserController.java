package com.team12345.messenger.controller;

import com.team12345.messenger.dto.request.UpdateProfileRequestDTO;
import com.team12345.messenger.dto.response.UserProfileResponseDTO;
import com.team12345.messenger.dto.response.UserSearchResponseDTO;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.MediaService;
import com.team12345.messenger.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MediaService mediaService;

    @Operation(summary = "Get personal profile")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getMyProfile() {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(userService.getProfile(currentUserId));
    }

    @Operation(summary = "Get other user's profile")
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponseDTO> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }

    @Operation(summary = "Update profile")
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponseDTO> updateProfile(@Valid @RequestBody UpdateProfileRequestDTO request) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(userService.updateStatus(currentUserId, request.getStatus()));
    }

    @Operation(summary = "Update avatar")
    @PatchMapping("/avatar")
    public ResponseEntity<UserProfileResponseDTO> updateAvatar(@RequestParam("file") MultipartFile file) {
        Long currentUserId = getCurrentUserId();
        
        Map<String, Object> uploadResult = mediaService.uploadFile(file);
        String avatarUrl = (String) uploadResult.get("url");

        return ResponseEntity.ok(userService.updateAvatar(currentUserId, avatarUrl));
    }

    @Operation(summary = "Update profile with avatar, email, and password")
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponseDTO> updateProfileComplete(
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @RequestPart(value = "data", required = false) @Valid UpdateProfileRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Long userId = currentUser != null ? currentUser.getId() : getCurrentUserId();
        UserProfileResponseDTO response = userService.updateProfile(userId, avatar, request);
        return ResponseEntity.ok(response);
    }

    // Keep only one mapping for /users/search
    @Operation(summary = "Search users with friendship status")
    @GetMapping("/search/paged")
    public ResponseEntity<Page<UserSearchResponseDTO>> searchUsersV2(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.badRequest().build();
        }
        Long currentUserId = getCurrentUserId();
        Page<UserSearchResponseDTO> result = userService.searchUsers(query.trim(), PageRequest.of(page, size), currentUserId);
        return ResponseEntity.ok(result);
    }

    // Helper method to extract ID from SecurityContext
    private Long getCurrentUserId() {
         Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         if (principal instanceof CustomUserDetails) {
             return ((CustomUserDetails) principal).getId();
         }
         throw new RuntimeException("Could not extract user from Security Context");
    }
}