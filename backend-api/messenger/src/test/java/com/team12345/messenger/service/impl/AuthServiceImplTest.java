package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.request.LoginRequestDTO;
import com.team12345.messenger.dto.request.RegisterRequestDTO;
import com.team12345.messenger.dto.response.AuthResponseDTO;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.exception.InvalidCredentialsException;
import com.team12345.messenger.exception.UserAlreadyExistsException;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

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
        loginRequestDTO.setUsernameOrEmail("testuser");
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
                .hasMessage("Username is already taken: testuser");

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
    void testLogin_Success_WithUsername() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtils.generateTokenFromUsername("testuser")).thenReturn("jwtToken");

        // Act
        AuthResponseDTO response = authService.login(loginRequestDTO);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwtToken");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");

        verify(userRepository).findByUsername("testuser");
        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtils).generateTokenFromUsername("testuser");
    }

    @Test
    void testLogin_Success_WithEmail() {
        // Arrange
        loginRequestDTO.setUsernameOrEmail("test@example.com");
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtils.generateTokenFromUsername("testuser")).thenReturn("jwtToken");

        // Act
        AuthResponseDTO response = authService.login(loginRequestDTO);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwtToken");

        verify(userRepository).findByUsername("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    @Test
    void testLogin_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequestDTO))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username/email or password");

        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByEmail("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLogin_InvalidPassword() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequestDTO))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username/email or password");

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtils, never()).generateTokenFromUsername(anyString());
    }
}
