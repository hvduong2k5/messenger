package com.team12345.messenger.service.impl;

import com.team12345.messenger.entity.User;
import com.team12345.messenger.entity.UserFriend;
import com.team12345.messenger.entity.UserFriendId;
import com.team12345.messenger.gateway.MqttGateway;
import com.team12345.messenger.repository.UserFriendRepository;
import com.team12345.messenger.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresenceServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserFriendRepository userFriendRepository;
    @Mock private MqttGateway mqttGateway;

    @InjectMocks
    private PresenceServiceImpl presenceService;

    private User user;
    private User friend;

    @BeforeEach
    void setUp() {
        user   = User.builder().id(1L).username("alice").isOnline(false).build();
        friend = User.builder().id(2L).username("bob").build();
    }

    @Test
    void updateUserPresence_online_shouldSetIsOnlineTrueAndNotSetLastSeen() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userFriendRepository.findById_UserId(1L)).thenReturn(List.of());

        presenceService.updateUserPresence("alice", true);

        assertThat(user.getIsOnline()).isTrue();
        assertThat(user.getLastSeen()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    void updateUserPresence_offline_shouldSetIsOnlineFalseAndUpdateLastSeen() {
        user.setIsOnline(true);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userFriendRepository.findById_UserId(1L)).thenReturn(List.of());

        presenceService.updateUserPresence("alice", false);

        assertThat(user.getIsOnline()).isFalse();
        assertThat(user.getLastSeen()).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void updateUserPresence_shouldBroadcastToAllFriends() {
        UserFriend uf = UserFriend.builder()
                .id(new UserFriendId(1L, 2L))
                .user(user)
                .friend(friend)
                .build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userFriendRepository.findById_UserId(1L)).thenReturn(List.of(uf));

        presenceService.updateUserPresence("alice", true);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(mqttGateway).sendToMqtt(payloadCaptor.capture(), topicCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("user/2/presence");
        assertThat(payloadCaptor.getValue()).contains("\"userId\":1");
        assertThat(payloadCaptor.getValue()).contains("\"status\":\"online\"");
    }

    @Test
    void updateUserPresence_offline_broadcastShouldContainOfflineStatus() {
        UserFriend uf = UserFriend.builder()
                .id(new UserFriendId(1L, 2L))
                .user(user)
                .friend(friend)
                .build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userFriendRepository.findById_UserId(1L)).thenReturn(List.of(uf));

        presenceService.updateUserPresence("alice", false);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(mqttGateway).sendToMqtt(payloadCaptor.capture(), anyString());
        assertThat(payloadCaptor.getValue()).contains("\"status\":\"offline\"");
    }

    @Test
    void updateUserPresence_unknownUser_shouldDoNothingGracefully() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        presenceService.updateUserPresence("ghost", true);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(mqttGateway);
    }

    @Test
    void updateUserPresence_mqttFailure_shouldNotPropagateException() {
        UserFriend uf = UserFriend.builder()
                .id(new UserFriendId(1L, 2L))
                .user(user)
                .friend(friend)
                .build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userFriendRepository.findById_UserId(1L)).thenReturn(List.of(uf));
        doThrow(new RuntimeException("broker down")).when(mqttGateway).sendToMqtt(anyString(), anyString());

        // Must not throw
        presenceService.updateUserPresence("alice", true);

        verify(userRepository).save(user);
    }
}
