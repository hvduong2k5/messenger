package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.request.InitiateCallRequestDTO;
import com.team12345.messenger.dto.request.SignalingRequestDTO;
import com.team12345.messenger.dto.response.CallResponseDTO;
import com.team12345.messenger.dto.response.SignalingResponseDTO;
import com.team12345.messenger.entity.*;
import com.team12345.messenger.exception.CallNotFoundException;
import com.team12345.messenger.exception.InvalidCallStateException;
import com.team12345.messenger.exception.UnauthorizedCallAccessException;
import com.team12345.messenger.gateway.MqttGateway;
import com.team12345.messenger.repository.CallParticipantRepository;
import com.team12345.messenger.repository.CallRepository;
import com.team12345.messenger.repository.CallSignalingRepository;
import com.team12345.messenger.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CallServiceImplTest {

    @Autowired
    private CallServiceImpl callService;

    @MockBean
    private CallRepository callRepository;

    @MockBean
    private CallParticipantRepository callParticipantRepository;

    @MockBean
    private CallSignalingRepository callSignalingRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MqttGateway mqttGateway;

    @Test
    void testInitiateCall_Success() {
        // Given
        Long callerId = 1L;
        Long receiverId = 2L;
        User caller = User.builder().id(callerId).username("caller").build();
        User receiver = User.builder().id(receiverId).username("receiver").build();
        InitiateCallRequestDTO request = new InitiateCallRequestDTO();
        request.setReceiverId(receiverId);
        request.setCallType(CallType.audio);

        Call savedCall = Call.builder()
                .id(1L)
                .caller(caller)
                .receiver(receiver)
                .callType(CallType.audio)
                .status(CallStatus.ringing)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(callerId)).thenReturn(Optional.of(caller));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(callRepository.save(any(Call.class))).thenReturn(savedCall);
        when(callParticipantRepository.save(any(CallParticipant.class))).thenReturn(null);

        // When
        CallResponseDTO response = callService.initiateCall(callerId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCallerId()).isEqualTo(callerId);
        assertThat(response.getReceiverId()).isEqualTo(receiverId);
        assertThat(response.getCallType()).isEqualTo(CallType.audio);
        assertThat(response.getStatus()).isEqualTo(CallStatus.ringing);

        verify(callRepository, times(1)).save(any(Call.class));
        verify(callParticipantRepository, times(2)).save(any(CallParticipant.class));
        verify(mqttGateway, times(1)).sendToMqtt(anyString(), anyString());
    }

    @Test
    void testInitiateCall_CallerNotFound() {
        // Given
        Long callerId = 1L;
        InitiateCallRequestDTO request = new InitiateCallRequestDTO();
        request.setReceiverId(2L);
        request.setCallType(CallType.audio);

        when(userRepository.findById(callerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> callService.initiateCall(callerId, request))
                .isInstanceOf(CallNotFoundException.class)
                .hasMessage("Caller not found: 1");
    }

    @Test
    void testAnswerCall_Success() {
        // Given
        Long callId = 1L;
        Long userId = 2L;
        User caller = User.builder().id(1L).username("caller").build();
        User receiver = User.builder().id(userId).username("receiver").build();

        Call call = Call.builder()
                .id(callId)
                .caller(caller)
                .receiver(receiver)
                .status(CallStatus.ringing)
                .build();

        CallParticipant participant = CallParticipant.builder()
                .id(new CallParticipantId(callId, userId))
                .call(call)
                .user(receiver)
                .build();

        when(callRepository.findById(callId)).thenReturn(Optional.of(call));
        when(callRepository.save(any(Call.class))).thenReturn(call);
        when(callParticipantRepository.findById(any(CallParticipantId.class))).thenReturn(Optional.of(participant));
        when(callParticipantRepository.save(any(CallParticipant.class))).thenReturn(participant);

        // When
        CallResponseDTO response = callService.answerCall(callId, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(CallStatus.connected);
        assertThat(response.getStartedAt()).isNotNull();

        verify(callRepository, times(1)).save(call);
        verify(callParticipantRepository, times(1)).save(participant);
        verify(mqttGateway, times(1)).sendToMqtt(anyString(), anyString());
    }

    @Test
    void testAnswerCall_InvalidState() {
        // Given
        Long callId = 1L;
        Long userId = 2L;
        User caller = User.builder().id(1L).username("caller").build();
        User receiver = User.builder().id(userId).username("receiver").build();

        Call call = Call.builder()
                .id(callId)
                .caller(caller)
                .receiver(receiver)
                .status(CallStatus.connected) // Already connected
                .build();

        when(callRepository.findById(callId)).thenReturn(Optional.of(call));

        // When & Then
        assertThatThrownBy(() -> callService.answerCall(callId, userId))
                .isInstanceOf(InvalidCallStateException.class)
                .hasMessage("Call is not in ringing state: connected");
    }

    @Test
    void testEndCall_Success() {
        // Given
        Long callId = 1L;
        Long userId = 2L;
        User caller = User.builder().id(1L).username("caller").build();
        User receiver = User.builder().id(userId).username("receiver").build();

        Call call = Call.builder()
                .id(callId)
                .caller(caller)
                .receiver(receiver)
                .status(CallStatus.connected)
                .build();

        CallParticipant participant = CallParticipant.builder()
                .id(new CallParticipantId(callId, userId))
                .call(call)
                .user(receiver)
                .build();

        when(callRepository.findById(callId)).thenReturn(Optional.of(call));
        when(callRepository.save(any(Call.class))).thenReturn(call);
        when(callParticipantRepository.findById(any(CallParticipantId.class))).thenReturn(Optional.of(participant));
        when(callParticipantRepository.save(any(CallParticipant.class))).thenReturn(participant);

        // When
        CallResponseDTO response = callService.endCall(callId, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(CallStatus.ended);
        assertThat(response.getEndedAt()).isNotNull();

        verify(callRepository, times(1)).save(call);
        verify(callParticipantRepository, times(1)).save(participant);
        verify(mqttGateway, times(1)).sendToMqtt(anyString(), anyString());
    }

    @Test
    void testRejectCall_Success() {
        // Given
        Long callId = 1L;
        Long userId = 2L;
        User caller = User.builder().id(1L).username("caller").build();
        User receiver = User.builder().id(userId).username("receiver").build();

        Call call = Call.builder()
                .id(callId)
                .caller(caller)
                .receiver(receiver)
                .status(CallStatus.ringing)
                .build();

        when(callRepository.findById(callId)).thenReturn(Optional.of(call));
        when(callRepository.save(any(Call.class))).thenReturn(call);

        // When
        CallResponseDTO response = callService.rejectCall(callId, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(CallStatus.rejected);
        assertThat(response.getEndedAt()).isNotNull();

        verify(callRepository, times(1)).save(call);
        verify(mqttGateway, times(1)).sendToMqtt(anyString(), anyString());
    }

    @Test
    void testSaveSignaling_Success() {
        // Given
        Long userId = 1L;
        Long callId = 1L;
        User sender = User.builder().id(userId).username("sender").build();
        User caller = User.builder().id(1L).username("caller").build();
        User receiver = User.builder().id(2L).username("receiver").build();

        Call call = Call.builder()
                .id(callId)
                .caller(caller)
                .receiver(receiver)
                .status(CallStatus.connected)
                .build();

        SignalingRequestDTO request = new SignalingRequestDTO();
        request.setCallId(callId);
        request.setSignalType("offer");
        request.setData("{\"sdp\":\"test\"}");

        CallSignaling signaling = CallSignaling.builder()
                .id(1L)
                .sender(sender)
                .call(call)
                .signalType("offer")
                .data("{\"sdp\":\"test\"}")
                .createdAt(LocalDateTime.now())
                .build();

        when(callRepository.findById(callId)).thenReturn(Optional.of(call));
        when(userRepository.findById(userId)).thenReturn(Optional.of(sender));
        when(callSignalingRepository.save(any(CallSignaling.class))).thenReturn(signaling);

        // When
        SignalingResponseDTO response = callService.saveSignaling(userId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSenderId()).isEqualTo(userId);
        assertThat(response.getSignalType()).isEqualTo("offer");

        verify(callSignalingRepository, times(1)).save(any(CallSignaling.class));
        verify(mqttGateway, times(1)).sendToMqtt(anyString(), anyString());
    }

    @Test
    void testGetSignaling_Success() {
        // Given
        Long callId = 1L;
        Long userId = 1L;
        User caller = User.builder().id(1L).username("caller").build();
        User receiver = User.builder().id(2L).username("receiver").build();

        Call call = Call.builder()
                .id(callId)
                .caller(caller)
                .receiver(receiver)
                .status(CallStatus.connected)
                .build();

        CallSignaling signaling = CallSignaling.builder()
                .id(1L)
                .sender(caller)
                .call(call)
                .signalType("offer")
                .data("{\"sdp\":\"test\"}")
                .createdAt(LocalDateTime.now())
                .build();

        when(callRepository.findById(callId)).thenReturn(Optional.of(call));
        when(callSignalingRepository.findByCall_IdOrderByCreatedAtAsc(callId)).thenReturn(List.of(signaling));

        // When
        List<SignalingResponseDTO> responses = callService.getSignaling(callId, userId);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(0).getSignalType()).isEqualTo("offer");
    }

    @Test
    void testGetCallHistory_Success() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).username("testuser").build();
        User otherUser = User.builder().id(2L).username("otheruser").build();

        Call call1 = Call.builder()
                .id(1L)
                .caller(user)
                .receiver(otherUser)
                .callType(CallType.audio)
                .status(CallStatus.ended)
                .createdAt(LocalDateTime.now())
                .build();

        Call call2 = Call.builder()
                .id(2L)
                .caller(otherUser)
                .receiver(user)
                .callType(CallType.video)
                .status(CallStatus.missed)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(callRepository.findByCaller_IdOrReceiver_Id(userId, userId)).thenReturn(List.of(call1, call2));

        // When
        List<CallResponseDTO> responses = callService.getCallHistory(userId);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void testValidateCallParticipant_Unauthorized() {
        // Given
        Long callId = 1L;
        Long unauthorizedUserId = 3L;
        User caller = User.builder().id(1L).username("caller").build();
        User receiver = User.builder().id(2L).username("receiver").build();

        Call call = Call.builder()
                .id(callId)
                .caller(caller)
                .receiver(receiver)
                .build();

        // When & Then - This would be tested indirectly through service methods
        // For now, we'll test that the validation logic works by calling a private method via reflection or testing through public methods
        assertThatThrownBy(() -> {
            // Test through answerCall method
            when(callRepository.findById(callId)).thenReturn(Optional.of(call));
            callService.answerCall(callId, unauthorizedUserId);
        }).isInstanceOf(UnauthorizedCallAccessException.class)
          .hasMessage("User 3 is not a participant in call 1");
    }
}
