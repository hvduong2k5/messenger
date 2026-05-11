package com.team12345.messenger.controller;

import com.team12345.messenger.dto.response.FriendRequestResponseDTO;
import com.team12345.messenger.dto.response.UserResponseDTO;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendshipService friendshipService;

    @Operation(summary = "Send a friend request")
    @PostMapping("/request/{receiverId}")
    public ResponseEntity<FriendRequestResponseDTO> sendFriendRequest(@PathVariable Long receiverId) {
        Long currentUserId = getCurrentUserId();
        FriendRequestResponseDTO response = friendshipService.sendFriendRequest(currentUserId, receiverId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Accept a friend request")
    @PutMapping("/request/{senderId}/accept")
    public ResponseEntity<String> acceptFriendRequest(@PathVariable Long senderId) {
        Long currentUserId = getCurrentUserId();
        friendshipService.acceptFriendRequest(currentUserId, senderId);
        return ResponseEntity.ok("Friend request accepted successfully");
    }

    @Operation(summary = "Decline a friend request")
    @PutMapping("/request/{senderId}/decline")
    public ResponseEntity<Void> declineFriendRequest(@PathVariable Long senderId) {
        Long currentUserId = getCurrentUserId();
        friendshipService.declineFriendRequest(currentUserId, senderId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Unfriend a user")
    @DeleteMapping("/{friendId}")
    public ResponseEntity<String> unfriend(@PathVariable Long friendId) {
        Long currentUserId = getCurrentUserId();
        friendshipService.unfriend(currentUserId, friendId);
        return ResponseEntity.ok("Unfriended successfully");
    }

    @Operation(summary = "Get list of friends")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getFriendsList() {
        Long currentUserId = getCurrentUserId();
        List<UserResponseDTO> friends = friendshipService.getFriendsList(currentUserId);
        return ResponseEntity.ok(friends);
    }

    @Operation(summary = "Get pending friend requests")
    @GetMapping("/requests/pending")
    public ResponseEntity<List<FriendRequestResponseDTO>> getPendingRequests() {
        Long currentUserId = getCurrentUserId();
        List<FriendRequestResponseDTO> pendingRequests = friendshipService.getPendingRequests(currentUserId);
        return ResponseEntity.ok(pendingRequests);
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