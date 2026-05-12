package com.team12345.messenger.controller;

import com.team12345.messenger.dto.response.NotificationResponseDTO;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "Endpoints for managing user notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Lấy danh sách thông báo", description = "Lấy danh sách thông báo của người dùng hiện tại (hỗ trợ phân trang)")
    @GetMapping
    public ResponseEntity<Slice<NotificationResponseDTO>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Slice<NotificationResponseDTO> notifications = notificationService.getUserNotifications(userDetails.getId(), pageable);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Đánh dấu đã đọc một thông báo", description = "Đánh dấu một thông báo cụ thể là đã đọc")
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        try {
            notificationService.markAsRead(id, userDetails.getId());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Not authorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Đánh dấu tất cả đã đọc", description = "Đánh dấu tất cả thông báo của người dùng hiện tại là đã đọc")
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsSeen(userDetails.getId());
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    @Operation(summary = "Xóa thông báo", description = "Xóa một thông báo cụ thể")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        try {
            notificationService.deleteNotification(id, userDetails.getId());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Not authorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}
