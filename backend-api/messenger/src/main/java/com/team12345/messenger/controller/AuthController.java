package com.team12345.messenger.controller;

import com.team12345.messenger.dto.request.ForgotPasswordRequestDTO;
import com.team12345.messenger.dto.request.LoginRequestDTO;
import com.team12345.messenger.dto.request.RegisterRequestDTO;
import com.team12345.messenger.dto.request.ResetPasswordRequestDTO;
import com.team12345.messenger.dto.response.AuthResponseDTO;
import com.team12345.messenger.dto.response.ForgotPasswordResponseDTO;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Endpoints for user authentication and password management")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng ký tài khoản mới", description = "Tạo tài khoản người dùng mới với username, email và password")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        AuthResponseDTO authResponseDTO = authService.register(registerRequestDTO);
        return ResponseEntity.ok(authResponseDTO);
    }

    @Operation(summary = "Đăng nhập", description = "Xác thực thông tin đăng nhập và trả về JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        AuthResponseDTO authResponseDTO = authService.login(loginRequestDTO);
        return ResponseEntity.ok(authResponseDTO);
    }

    @Operation(summary = "Quên mật khẩu", description = "Gửi mã OTP để đặt lại mật khẩu")
    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponseDTO> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
        authService.forgotPassword(forgotPasswordRequestDTO);
        return ResponseEntity.ok(ForgotPasswordResponseDTO.builder()
                .message("OTP has been sent to your email")
                .success(true)
                .build());
    }

    @Operation(summary = "Đặt lại mật khẩu", description = "Xác nhận OTP và đặt lại mật khẩu mới")
    @PostMapping("/reset-password")
    public ResponseEntity<ForgotPasswordResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO resetPasswordRequestDTO) {
        authService.resetPassword(resetPasswordRequestDTO);
        return ResponseEntity.ok(ForgotPasswordResponseDTO.builder()
                .message("Password has been reset successfully")
                .success(true)
                .build());
    }

    @Operation(summary = "Đăng xuất", description = "Đăng xuất người dùng hiện tại")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request) {
        String token = extractToken(request);
        authService.logout(userDetails.getId(), token);
        return ResponseEntity.ok().build();
    }

    private String extractToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
