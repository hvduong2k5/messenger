package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.response.FriendRequestResponseDTO;
import com.team12345.messenger.entity.*;
import com.team12345.messenger.exception.ResourceNotFoundException;
import com.team12345.messenger.repository.FriendRequestRepository;
import com.team12345.messenger.repository.UserFriendRepository;
import com.team12345.messenger.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private UserFriendRepository userFriendRepository;

    @InjectMocks
    private FriendshipServiceImpl friendshipService;

    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {
        sender = User.builder().id(1L).username("senderUser").build();
        receiver = User.builder().id(2L).username("receiverUser").build();
    }

    @Test
    void sendFriendRequest_ShouldReturnDto_WhenValid() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(userFriendRepository.existsById_UserIdAndId_FriendId(1L, 2L)).thenReturn(false);
        when(friendRequestRepository.findById_SenderIdAndId_ReceiverId(1L, 2L)).thenReturn(Optional.empty());
        when(friendRequestRepository.findById_SenderIdAndId_ReceiverId(2L, 1L)).thenReturn(Optional.empty());

        FriendRequest savedRequest = FriendRequest.builder()
                .id(new FriendRequestId(1L, 2L))
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.pending)
                .build();
        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(savedRequest);

        // Act
        FriendRequestResponseDTO response = friendshipService.sendFriendRequest(1L, 2L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSenderId()).isEqualTo(1L);
        assertThat(response.getReceiverId()).isEqualTo(2L);
        assertThat(response.getStatus()).isEqualTo("pending");

        verify(friendRequestRepository, times(1)).save(any(FriendRequest.class));
    }

    @Test
    void sendFriendRequest_ShouldThrowException_WhenAlreadyFriends() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(userFriendRepository.existsById_UserIdAndId_FriendId(1L, 2L)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            friendshipService.sendFriendRequest(1L, 2L);
        });

        assertThat(exception.getMessage()).isEqualTo("Users are already friends");
        verify(friendRequestRepository, never()).save(any(FriendRequest.class));
    }

    @Test
    void sendFriendRequest_ShouldReuseExistingRequest_WhenRequestIsRejected() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(userFriendRepository.existsById_UserIdAndId_FriendId(1L, 2L)).thenReturn(false);

        FriendRequest rejectedRequest = FriendRequest.builder()
                .id(new FriendRequestId(1L, 2L))
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.rejected)
                .build();
        when(friendRequestRepository.findById_SenderIdAndId_ReceiverId(1L, 2L)).thenReturn(Optional.of(rejectedRequest));
        when(friendRequestRepository.save(any(FriendRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FriendRequestResponseDTO response = friendshipService.sendFriendRequest(1L, 2L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSenderId()).isEqualTo(1L);
        assertThat(response.getReceiverId()).isEqualTo(2L);
        assertThat(response.getStatus()).isEqualTo("pending");

        verify(friendRequestRepository, times(1)).save(rejectedRequest);
        assertThat(rejectedRequest.getStatus()).isEqualTo(FriendRequestStatus.pending);
    }

    @Test
    void acceptFriendRequest_ShouldSaveUserFriends_WhenValid() {
        // Arrange
        FriendRequest pendingRequest = FriendRequest.builder()
                .id(new FriendRequestId(1L, 2L))
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.pending)
                .build();

        when(friendRequestRepository.findById_SenderIdAndId_ReceiverId(1L, 2L)).thenReturn(Optional.of(pendingRequest));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));

        // Act
        friendshipService.acceptFriendRequest(2L, 1L);

        // Assert
        assertThat(pendingRequest.getStatus()).isEqualTo(FriendRequestStatus.accepted);
        verify(friendRequestRepository, times(1)).save(pendingRequest);
        verify(userFriendRepository, times(1)).saveAll(anyList());
    }

    @Test
    void unfriend_ShouldDeleteUserFriends_WhenValid() {
        // Arrange
        when(userFriendRepository.existsById_UserIdAndId_FriendId(1L, 2L)).thenReturn(true);

        // Act
        friendshipService.unfriend(1L, 2L);

        // Assert
        verify(userFriendRepository, times(2)).deleteById(any(UserFriendId.class));
    }

    @Test
    void cancelFriendRequest_ShouldDeleteRequest_WhenValidAndPending() {
        // Arrange
        FriendRequest pendingRequest = FriendRequest.builder()
                .id(new FriendRequestId(1L, 2L))
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.pending)
                .build();

        when(friendRequestRepository.findBySenderIdAndReceiverIdAndStatus(1L, 2L, FriendRequestStatus.pending))
                .thenReturn(Optional.of(pendingRequest));

        // Act
        friendshipService.cancelFriendRequest(1L, 2L);

        // Assert
        verify(friendRequestRepository, times(1)).delete(pendingRequest);
    }

    @Test
    void cancelFriendRequest_ShouldThrowException_WhenNotSenderOrNoRequestExists() {
        // Arrange
        when(friendRequestRepository.findBySenderIdAndReceiverIdAndStatus(2L, 1L, FriendRequestStatus.pending))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            friendshipService.cancelFriendRequest(2L, 1L);
        });

        verify(friendRequestRepository, never()).delete(any(FriendRequest.class));
    }

    @Test
    void cancelFriendRequest_ShouldThrowException_WhenRequestNotPending() {
        // Arrange
        when(friendRequestRepository.findBySenderIdAndReceiverIdAndStatus(1L, 2L, FriendRequestStatus.pending))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            friendshipService.cancelFriendRequest(1L, 2L);
        });

        verify(friendRequestRepository, never()).delete(any(FriendRequest.class));
    }
}