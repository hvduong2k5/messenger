package com.team12345.messenger.controller;

import com.team12345.messenger.dto.request.InitiateCallRequestDTO;
import com.team12345.messenger.dto.request.SignalingRequestDTO;
import com.team12345.messenger.dto.response.CallResponseDTO;
import com.team12345.messenger.dto.response.SignalingResponseDTO;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.CallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/calls")
@RequiredArgsConstructor
@Tag(name = "Call API", description = "Endpoints for managing voice and video calls")
public class CallController {

    private final CallService callService;

    @Operation(summary = "Khởi tạo cuộc gọi", description = "Tạo mới một cuộc gọi voice hoặc video tới người nhận")
    @PostMapping
    public ResponseEntity<CallResponseDTO> initiateCall(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody InitiateCallRequestDTO request) {

        CallResponseDTO response = callService.initiateCall(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Chấp nhận cuộc gọi", description = "Chấp nhận cuộc gọi đang đổ chuông")
    @PutMapping("/{id}/answer")
    public ResponseEntity<CallResponseDTO> answerCall(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        CallResponseDTO response = callService.answerCall(id, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Từ chối cuộc gọi", description = "Từ chối cuộc gọi đang đổ chuông")
    @PutMapping("/{id}/reject")
    public ResponseEntity<CallResponseDTO> rejectCall(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        CallResponseDTO response = callService.rejectCall(id, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Kết thúc cuộc gọi", description = "Kết thúc cuộc gọi đang diễn ra")
    @PutMapping("/{id}/end")
    public ResponseEntity<CallResponseDTO> endCall(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        CallResponseDTO response = callService.endCall(id, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Gửi tín hiệu WebRTC", description = "Gửi dữ liệu signaling cho WebRTC (SDP Offer/Answer, ICE Candidates)")
    @PostMapping("/{id}/signaling")
    public ResponseEntity<SignalingResponseDTO> sendSignaling(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody SignalingRequestDTO request) {

        request.setCallId(id);
        SignalingResponseDTO response = callService.saveSignaling(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Lấy tín hiệu WebRTC", description = "Lấy dữ liệu signaling cho WebRTC của một cuộc gọi")
    @GetMapping("/{id}/signaling")
    public ResponseEntity<List<SignalingResponseDTO>> getSignaling(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        List<SignalingResponseDTO> response = callService.getSignaling(id, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy lịch sử cuộc gọi", description = "Lấy danh sách lịch sử cuộc gọi của người dùng hiện tại")
    @GetMapping("/history")
    public ResponseEntity<List<CallResponseDTO>> getCallHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<CallResponseDTO> response = callService.getCallHistory(userDetails.getId());
        return ResponseEntity.ok(response);
    }
}
