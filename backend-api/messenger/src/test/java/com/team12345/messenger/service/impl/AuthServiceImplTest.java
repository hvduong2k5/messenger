package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.request.ForgotPasswordRequestDTO;
import com.team12345.messenger.dto.request.LoginRequestDTO;
import com.team12345.messenger.dto.request.RegisterRequestDTO;
import com.team12345.messenger.dto.request.ResetPasswordRequestDTO;
import com.team12345.messenger.dto.response.AuthResponseDTO;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.exception.InvalidCredentialsException;
import com.team12345.messenger.exception.UserAlreadyExistsException;
import com.team12345.messenger.repository.BlacklistedTokenRepository;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.security.JwtUtils;
import com.team12345.messenger.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private EmailService emailService;

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDTO registerRequestDTO;
    private LoginRequestDTO loginRequestDTO;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequestDTO = new RegisterRequestDTO();
        registerRequestDTO.setUsername("testuser");
        registerRequestDTO.setEmail("test@example.com");
        registerRequestDTO.setPassword("password123");

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsernameOrEmail("test@example.com");
        loginRequestDTO.setPassword("password123");

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .avatarUrl("avatar.jpg")
                .status("online")
                .build();
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtils.generateTokenFromUsername(anyString())).thenReturn("jwtToken");

        // Act
        AuthResponseDTO response = authService.register(registerRequestDTO);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwtToken");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtils).generateTokenFromUsername("testuser");
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequestDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Username is already registered: testuser");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequestDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email is already registered: test@example.com");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("test@example.com", "test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtils.generateTokenFromUsername("testuser")).thenReturn("jwtToken");

        // Act
        AuthResponseDTO response = authService.login(loginRequestDTO);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwtToken");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");

        verify(userRepository).findByUsernameOrEmail("test@example.com", "test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtils).generateTokenFromUsername("testuser");
    }

    @Test
    void testLogin_SuccessWithUsername() {
        // Arrange
        loginRequestDTO.setUsernameOrEmail("testuser");
        when(userRepository.findByUsernameOrEmail("testuser", "testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtils.generateTokenFromUsername("testuser")).thenReturn("jwtToken");

        // Act
        AuthResponseDTO response = authService.login(loginRequestDTO);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwtToken");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");

        verify(userRepository).findByUsernameOrEmail("testuser", "testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtils).generateTokenFromUsername("testuser");
    }

    @Test
    void testLogin_UserNotFound() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("test@example.com", "test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequestDTO))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByUsernameOrEmail("test@example.com", "test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLogin_InvalidPassword() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("test@example.com", "test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequestDTO))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByUsernameOrEmail("test@example.com", "test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtils, never()).generateTokenFromUsername(anyString());
    }

    @Test
    void testForgotPassword_UserExists() {
        // Arrange
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        authService.forgotPassword(request);

        // Assert
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }

    @Test
    void testForgotPassword_UserNotExists() {
        // Arrange
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail("nonexistent@example.com");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Email not found");

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testResetPassword_Success() {
        // Arrange
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setEmail("test@example.com");
        request.setOtp("reset-token-123");
        request.setNewPassword("newpassword123");

        User userWithToken = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("oldEncodedPassword")
                .otp("reset-token-123")
                .otpExpiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userWithToken));
        when(passwordEncoder.encode("newpassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userWithToken);

        // Act
        authService.resetPassword(request);

        // Assert
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("newpassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testResetPassword_InvalidToken() {
        // Arrange
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setEmail("test@example.com");
        request.setOtp("invalid-token");
        request.setNewPassword("newpassword123");


        User userWithOtherOtp = User.builder()
                .email("test@example.com")
                .otp("000000")
                .otpExpiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userWithOtherOtp));

        // user exists and OTP is present but does not match -> should throw OTP is incorrect
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("OTP is incorrect");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testResetPassword_ExpiredToken() {
        // Arrange
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setEmail("test@example.com");
        request.setOtp("expired-token");
        request.setNewPassword("newpassword123");

        User userWithExpiredToken = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("oldEncodedPassword")
                .otp("expired-token")
                .otpExpiresAt(LocalDateTime.now().minusHours(1))
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userWithExpiredToken));

        // Act & Assert
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("OTP has expired or does not exist");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogout() {
        // Arrange
        when(jwtUtils.getExpirationDateFromJwtToken("test-token")).thenReturn(new java.util.Date());

        // Act
        authService.logout(1L, "test-token");

        // Assert
        verify(jwtUtils).getExpirationDateFromJwtToken("test-token");
        verify(blacklistedTokenRepository).save(any());
    }

    @Test
    void testLogout_NullToken() {
        // Act
        authService.logout(1L, null);

        // Assert - logout is a no-op for null token
        verify(jwtUtils, never()).getExpirationDateFromJwtToken(anyString());
        verify(blacklistedTokenRepository, never()).save(any());
    }
}
