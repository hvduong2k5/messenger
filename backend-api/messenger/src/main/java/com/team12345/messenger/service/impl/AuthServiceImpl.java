package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.request.ForgotPasswordRequestDTO;
import com.team12345.messenger.dto.request.LoginRequestDTO;
import com.team12345.messenger.dto.request.RegisterRequestDTO;
import com.team12345.messenger.dto.request.ResetPasswordRequestDTO;
import com.team12345.messenger.dto.response.AuthResponseDTO;
import com.team12345.messenger.dto.response.UserResponseDTO;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.exception.InvalidCredentialsException;
import com.team12345.messenger.exception.UserAlreadyExistsException;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.security.JwtUtils;
import com.team12345.messenger.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequestDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken: " + registerRequestDTO.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequestDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered: " + registerRequestDTO.getEmail());
        }

        // Create new user
        User user = User.builder()
                .username(registerRequestDTO.getUsername())
                .email(registerRequestDTO.getEmail())
                .password(passwordEncoder.encode(registerRequestDTO.getPassword()))
                .build();

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String jwt = jwtUtils.generateTokenFromUsername(savedUser.getUsername());

        // Create response
        UserResponseDTO userResponseDTO = new UserResponseDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getAvatarUrl(),
                savedUser.getStatus(),
                savedUser.getIsOnline(),
                savedUser.getLastSeen()
        );

        return new AuthResponseDTO(jwt, userResponseDTO);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginRequestDTO) {
        // Find user by username or email
        Optional<User> userOptional = userRepository.findByUsername(loginRequestDTO.getUsernameOrEmail());
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(loginRequestDTO.getUsernameOrEmail());
        }

        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        User user = userOptional.get();

        // Verify password
        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        // Generate JWT token
        String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());

        // Create response
        UserResponseDTO userResponseDTO = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.getIsOnline(),
                user.getLastSeen()
        );

        return new AuthResponseDTO(jwt, userResponseDTO);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(forgotPasswordRequestDTO.getEmail());
        if (userOptional.isEmpty()) {
            // For security reasons, don't reveal if email exists or not
            return;
        }

        User user = userOptional.get();

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24); // Token expires in 24 hours

        // Save token to user
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiresAt(expiresAt);
        userRepository.save(user);

        // TODO: Send email with reset link
        // For now, just log the token (in production, send email)
        System.out.println("Password reset token for " + user.getEmail() + ": " + resetToken);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO) {
        // Find user by reset token
        Optional<User> userOptional = userRepository.findByPasswordResetToken(resetPasswordRequestDTO.getToken());
        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException("Invalid or expired reset token");
        }

        User user = userOptional.get();

        // Check if token is expired
        if (user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Reset token has expired");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(resetPasswordRequestDTO.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        userRepository.save(user);
    }

    @Override
    public void logout(Long userId) {
        // Since JWT is stateless, logout is handled client-side by removing the token
        // In a production system with token blacklisting, we would add the token to a blacklist here
        // For now, just return (client will remove token from storage)
    }
}
