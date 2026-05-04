package com.team12345.messenger.service;

import com.team12345.messenger.dto.response.UserProfileResponseDTO;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.exception.ResourceNotFoundException;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedpassword")
                .avatarUrl("http://example.com/avatar.jpg")
                .status("Active")
                .build();
    }

    @Test
    void getProfile_WhenUserExists_ShouldReturnProfile() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserProfileResponseDTO response = userService.getProfile(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getAvatarUrl()).isEqualTo("http://example.com/avatar.jpg");
        assertThat(response.getStatus()).isEqualTo("Active");

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getProfile_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getProfile(1L);
        });

        assertThat(exception.getMessage()).isEqualTo("User not found with id: 1");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void updateStatus_WhenUserExists_ShouldUpdateStatus() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserProfileResponseDTO response = userService.updateStatus(1L, "Do not disturb");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("Do not disturb");

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateStatus_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateStatus(1L, "Do not disturb");
        });

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateAvatar_WhenUserExists_ShouldUpdateAvatar() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String newAvatarUrl = "http://example.com/new-avatar.png";

        // Act
        UserProfileResponseDTO response = userService.updateAvatar(1L, newAvatarUrl);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAvatarUrl()).isEqualTo(newAvatarUrl);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateAvatar_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateAvatar(1L, "http://example.com/new-avatar.png");
        });

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }
}