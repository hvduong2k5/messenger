package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.request.UpdateProfileRequestDTO;
import com.team12345.messenger.dto.response.UserProfileResponseDTO;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.exception.InvalidCredentialsException;
import com.team12345.messenger.exception.ResourceNotFoundException;
import com.team12345.messenger.exception.UserAlreadyExistsException;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.repository.UserFriendRepository;
import com.team12345.messenger.repository.FriendRequestRepository;
import com.team12345.messenger.service.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserFriendRepository userFriendRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UpdateProfileRequestDTO updateProfileRequestDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .avatarUrl("old-avatar.jpg")
                .status("online")
                .build();

        updateProfileRequestDTO = UpdateProfileRequestDTO.builder()
                .email(null)
                .password(null)
                .oldPassword(null)
                .status(null)
                .build();
    }

    @Test
    void testUpdateProfile_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateProfile(1L, null, updateProfileRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 1");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateProfile_OnlyAvatar() {
        // Arrange
        MultipartFile avatarFile = mock(MultipartFile.class);
        when(avatarFile.isEmpty()).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "new-avatar-url.jpg");
        when(mediaService.uploadFile(avatarFile)).thenReturn(uploadResult);

        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserProfileResponseDTO response = userService.updateProfile(1L, avatarFile, updateProfileRequestDTO);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
        verify(mediaService).uploadFile(avatarFile);
        verify(userRepository).save(argThat(u -> u.getAvatarUrl().equals("new-avatar-url.jpg")));
    }

    @Test
    void testUpdateProfile_EmailChange_WithOldPasswordValidation_Success() {
        // Arrange
        UpdateProfileRequestDTO request = UpdateProfileRequestDTO.builder()
                .email("newemail@example.com")
                .password(null)
                .oldPassword("oldPassword")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserProfileResponseDTO response = userService.updateProfile(1L, null, request);

        // Assert
        assertThat(response).isNotNull();
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        verify(userRepository).existsByEmail("newemail@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateProfile_EmailChange_NoOldPassword() {
        // Arrange
        UpdateProfileRequestDTO request = UpdateProfileRequestDTO.builder()
                .email("newemail@example.com")
                .password(null)
                .oldPassword(null)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> userService.updateProfile(1L, null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mật khẩu cũ là bắt buộc");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateProfile_EmailChange_IncorrectOldPassword() {
        // Arrange
        UpdateProfileRequestDTO request = UpdateProfileRequestDTO.builder()
                .email("newemail@example.com")
                .password(null)
                .oldPassword("wrongPassword")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateProfile(1L, null, request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Mật khẩu cũ không chính xác");

        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateProfile_EmailChange_DuplicateEmail() {
        // Arrange
        UpdateProfileRequestDTO request = UpdateProfileRequestDTO.builder()
                .email("existing@example.com")
                .password(null)
                .oldPassword("oldPassword")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateProfile(1L, null, request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email is already registered: existing@example.com");

        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateProfile_PasswordChange_WithOldPasswordValidation_Success() {
        // Arrange
        UpdateProfileRequestDTO request = UpdateProfileRequestDTO.builder()
                .email(null)
                .password("newPassword123")
                .oldPassword("oldPassword")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserProfileResponseDTO response = userService.updateProfile(1L, null, request);

        // Assert
        assertThat(response).isNotNull();
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateProfile_PasswordChange_NoOldPassword() {
        // Arrange
        UpdateProfileRequestDTO request = UpdateProfileRequestDTO.builder()
                .email(null)
                .password("newPassword123")
                .oldPassword(null)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> userService.updateProfile(1L, null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mật khẩu cũ là bắt buộc");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateProfile_PasswordChange_IncorrectOldPassword() {
        // Arrange
        UpdateProfileRequestDTO request = UpdateProfileRequestDTO.builder()
                .email(null)
                .password("newPassword123")
                .oldPassword("wrongPassword")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateProfile(1L, null, request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Mật khẩu cũ không chính xác");

        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateProfile_AllFieldsChanged() {
        // Arrange
        MultipartFile avatarFile = mock(MultipartFile.class);
        when(avatarFile.isEmpty()).thenReturn(false);

        UpdateProfileRequestDTO request = UpdateProfileRequestDTO.builder()
                .email("newemail@example.com")
                .password("newPassword123")
                .oldPassword("oldPassword")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "new-avatar-url.jpg");
        when(mediaService.uploadFile(avatarFile)).thenReturn(uploadResult);

        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserProfileResponseDTO response = userService.updateProfile(1L, avatarFile, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        verify(userRepository).existsByEmail("newemail@example.com");
        verify(passwordEncoder).encode("newPassword123");
        verify(mediaService).uploadFile(avatarFile);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateProfile_EmptyRequest_NoChanges() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserProfileResponseDTO response = userService.updateProfile(1L, null, updateProfileRequestDTO);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verify(mediaService, never()).uploadFile(any());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testUpdateProfile_NullRequest_InvalidFile() {
        // Arrange
        MultipartFile avatarFile = mock(MultipartFile.class);
        when(avatarFile.isEmpty()).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserProfileResponseDTO response = userService.updateProfile(1L, avatarFile, null);

        // Assert
        assertThat(response).isNotNull();
        verify(userRepository).findById(1L);
        verify(mediaService, never()).uploadFile(any());
        verify(userRepository).save(any(User.class));
    }
}

