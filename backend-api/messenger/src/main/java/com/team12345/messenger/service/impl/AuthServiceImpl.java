package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.request.ForgotPasswordRequestDTO;
import com.team12345.messenger.dto.request.LoginRequestDTO;
import com.team12345.messenger.dto.request.RegisterRequestDTO;
import com.team12345.messenger.dto.request.ResetPasswordRequestDTO;
import com.team12345.messenger.dto.response.AuthResponseDTO;
import com.team12345.messenger.dto.response.UserResponseDTO;
import com.team12345.messenger.entity.BlacklistedToken;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.exception.InvalidCredentialsException;
import com.team12345.messenger.exception.UserAlreadyExistsException;
import com.team12345.messenger.repository.BlacklistedTokenRepository;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.security.JwtUtils;
import com.team12345.messenger.service.AuthService;
import com.team12345.messenger.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequestDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username is already registered: " + registerRequestDTO.getUsername());
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
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(
                loginRequestDTO.getUsernameOrEmail(),
                loginRequestDTO.getUsernameOrEmail()
        );

        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userOptional.get();

        // Verify password
        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
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
            throw new InvalidCredentialsException("Email not found");
        }

        User user = userOptional.get();

        // Generate 6-digit OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(1000000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5); // OTP expires in 5 minutes

        // Save OTP to user (ghi đè nếu đã có)
        user.setOtp(otp);
        user.setOtpExpiresAt(expiresAt);
        userRepository.save(user);

        // Send email with OTP
        emailService.sendPasswordResetEmail(user.getEmail(), otp);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO) {
        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(resetPasswordRequestDTO.getEmail());
        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException("Email not found");
        }

        User user = userOptional.get();

        // Check if OTP exists and not expired
        if (user.getOtp() == null || user.getOtpExpiresAt() == null) {
            throw new InvalidCredentialsException("OTP has expired or does not exist");
        }

        if (user.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("OTP has expired or does not exist");
        }

        // Validate OTP
        if (!user.getOtp().equals(resetPasswordRequestDTO.getOtp())) {
            throw new InvalidCredentialsException("OTP is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(resetPasswordRequestDTO.getNewPassword()));

        // Clear OTP
        user.setOtp(null);
        user.setOtpExpiresAt(null);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void logout(Long userId, String token) {
        if (token != null) {
            // Calculate expiration time of the token
            Date expirationDate = jwtUtils.getExpirationDateFromJwtToken(token);
            LocalDateTime expiresAt = expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            // Add to blacklist
            BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                    .token(token)
                    .expiresAt(expiresAt)
                    .build();
            blacklistedTokenRepository.save(blacklistedToken);
        }
    }
}
