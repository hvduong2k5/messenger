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
import com.team12345.messenger.service.CallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallServiceImpl implements CallService {

    private final CallRepository callRepository;
    private final CallParticipantRepository callParticipantRepository;
    private final CallSignalingRepository callSignalingRepository;
    private final UserRepository userRepository;
    private final MqttGateway mqttGateway;

    @Override
    @Transactional
    public CallResponseDTO initiateCall(Long callerId, InitiateCallRequestDTO request) {
        log.info("Initiating call from user {} to user {} with type {}", callerId, request.getReceiverId(), request.getCallType());

        // Validate caller and receiver exist
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new CallNotFoundException("Caller not found: " + callerId));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new CallNotFoundException("Receiver not found: " + request.getReceiverId()));

        // Create new call
        Call call = Call.builder()
                .caller(caller)
                .receiver(receiver)
                .callType(request.getCallType())
                .status(CallStatus.ringing)
                .build();

        call = callRepository.save(call);

        // Create call participants
        CallParticipant callerParticipant = CallParticipant.builder()
                .id(new CallParticipantId(call.getId(), caller.getId()))
                .call(call)
                .user(caller)
                .joinedAt(LocalDateTime.now())
                .build();

        CallParticipant receiverParticipant = CallParticipant.builder()
                .id(new CallParticipantId(call.getId(), receiver.getId()))
                .call(call)
                .user(receiver)
                .build();

        callParticipantRepository.save(callerParticipant);
        callParticipantRepository.save(receiverParticipant);

        // Send real-time notification to receiver
        String notificationMessage = String.format("{\"type\":\"incoming_call\",\"callId\":%d,\"callerId\":%d,\"callerUsername\":\"%s\",\"callType\":\"%s\"}",
                call.getId(), caller.getId(), caller.getUsername(), request.getCallType());
        mqttGateway.sendToMqtt(notificationMessage, "user/" + receiver.getId() + "/calls");

        log.info("Call initiated successfully with ID: {}", call.getId());
        return mapToCallResponseDTO(call);
    }

    @Override
    @Transactional
    public CallResponseDTO answerCall(Long callId, Long userId) {
        log.info("Answering call {} by user {}", callId, userId);

        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new CallNotFoundException("Call not found: " + callId));

        // Validate user is a participant
        validateCallParticipant(call, userId);

        // Check call status
        if (call.getStatus() != CallStatus.ringing) {
            throw new InvalidCallStateException("Call is not in ringing state: " + call.getStatus());
        }

        // Update call status and start time
        call.setStatus(CallStatus.connected);
        call.setStartedAt(LocalDateTime.now());
        call = callRepository.save(call);

        // Update participant join time
        CallParticipant participant = callParticipantRepository.findById(new CallParticipantId(callId, userId))
                .orElseThrow(() -> new CallNotFoundException("Call participant not found"));
        participant.setJoinedAt(LocalDateTime.now());
        callParticipantRepository.save(participant);

        // Send confirmation signal to caller
        String confirmationMessage = String.format("{\"type\":\"call_answered\",\"callId\":%d,\"answererId\":%d}", callId, userId);
        mqttGateway.sendToMqtt(confirmationMessage, "user/" + call.getCaller().getId() + "/calls");

        log.info("Call {} answered successfully", callId);
        return mapToCallResponseDTO(call);
    }

    @Override
    @Transactional
    public CallResponseDTO endCall(Long callId, Long userId) {
        log.info("Ending call {} by user {}", callId, userId);

        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new CallNotFoundException("Call not found: " + callId));

        // Validate user is a participant
        validateCallParticipant(call, userId);

        // Check call status
        if (call.getStatus() != CallStatus.connected && call.getStatus() != CallStatus.ringing) {
            throw new InvalidCallStateException("Call is not active: " + call.getStatus());
        }

        // Update call status and end time
        call.setStatus(CallStatus.ended);
        call.setEndedAt(LocalDateTime.now());
        call = callRepository.save(call);

        // Update participant leave time
        CallParticipant participant = callParticipantRepository.findById(new CallParticipantId(callId, userId))
                .orElseThrow(() -> new CallNotFoundException("Call participant not found"));
        participant.setLeftAt(LocalDateTime.now());
        callParticipantRepository.save(participant);

        // Send end signal to all participants
        String endMessage = String.format("{\"type\":\"call_ended\",\"callId\":%d,\"endedBy\":%d}", callId, userId);
        mqttGateway.sendToMqtt(endMessage, "call/" + callId + "/participants");

        log.info("Call {} ended successfully", callId);
        return mapToCallResponseDTO(call);
    }

    @Override
    @Transactional
    public CallResponseDTO rejectCall(Long callId, Long userId) {
        log.info("Rejecting call {} by user {}", callId, userId);

        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new CallNotFoundException("Call not found: " + callId));

        // Validate user is a participant
        validateCallParticipant(call, userId);

        // Check call status
        if (call.getStatus() != CallStatus.ringing) {
            throw new InvalidCallStateException("Call is not in ringing state: " + call.getStatus());
        }

        // Update call status
        call.setStatus(CallStatus.rejected);
        call.setEndedAt(LocalDateTime.now());
        call = callRepository.save(call);

        // Send rejection signal to caller
        String rejectionMessage = String.format("{\"type\":\"call_rejected\",\"callId\":%d,\"rejectedBy\":%d}", callId, userId);
        mqttGateway.sendToMqtt(rejectionMessage, "user/" + call.getCaller().getId() + "/calls");

        log.info("Call {} rejected successfully", callId);
        return mapToCallResponseDTO(call);
    }

    @Override
    @Transactional
    public SignalingResponseDTO saveSignaling(Long userId, SignalingRequestDTO request) {
        log.info("Saving signaling data for call {} from user {}", request.getCallId(), userId);

        Call call = callRepository.findById(request.getCallId())
                .orElseThrow(() -> new CallNotFoundException("Call not found: " + request.getCallId()));

        // Validate user is a participant
        validateCallParticipant(call, userId);

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new CallNotFoundException("Sender not found: " + userId));

        CallSignaling signaling = CallSignaling.builder()
                .sender(sender)
                .call(call)
                .signalType(request.getSignalType())
                .data(request.getData())
                .build();

        signaling = callSignalingRepository.save(signaling);

        // Broadcast signaling data to other participants
        String signalingMessage = String.format("{\"type\":\"signaling\",\"callId\":%d,\"senderId\":%d,\"signalType\":\"%s\",\"data\":%s}",
                request.getCallId(), userId, request.getSignalType(), request.getData());
        mqttGateway.sendToMqtt(signalingMessage, "call/" + request.getCallId() + "/signaling");

        log.info("Signaling data saved successfully with ID: {}", signaling.getId());
        return mapToSignalingResponseDTO(signaling);
    }

    @Override
    public List<SignalingResponseDTO> getSignaling(Long callId, Long userId) {
        log.info("Retrieving signaling data for call {} by user {}", callId, userId);

        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new CallNotFoundException("Call not found: " + callId));

        // Validate user is a participant
        validateCallParticipant(call, userId);

        List<CallSignaling> signalingList = callSignalingRepository.findByCall_IdOrderByCreatedAtAsc(callId);

        return signalingList.stream()
                .map(this::mapToSignalingResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CallResponseDTO> getCallHistory(Long userId) {
        log.info("Retrieving call history for user {}", userId);

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new CallNotFoundException("User not found: " + userId));

        // Get calls where user is caller or receiver
        List<Call> calls = callRepository.findByCaller_IdOrReceiver_Id(userId, userId);

        return calls.stream()
                .map(this::mapToCallResponseDTO)
                .collect(Collectors.toList());
    }

    private void validateCallParticipant(Call call, Long userId) {
        boolean isParticipant = call.getCaller().getId().equals(userId) ||
                               call.getReceiver().getId().equals(userId);

        if (!isParticipant) {
            throw new UnauthorizedCallAccessException("User " + userId + " is not a participant in call " + call.getId());
        }
    }

    private CallResponseDTO mapToCallResponseDTO(Call call) {
        return CallResponseDTO.builder()
                .id(call.getId())
                .callerId(call.getCaller().getId())
                .callerUsername(call.getCaller().getUsername())
                .receiverId(call.getReceiver().getId())
                .receiverUsername(call.getReceiver().getUsername())
                .callType(call.getCallType())
                .status(call.getStatus())
                .startedAt(call.getStartedAt())
                .endedAt(call.getEndedAt())
                .createdAt(call.getCreatedAt())
                .build();
    }

    private SignalingResponseDTO mapToSignalingResponseDTO(CallSignaling signaling) {
        return SignalingResponseDTO.builder()
                .id(signaling.getId())
                .senderId(signaling.getSender().getId())
                .senderUsername(signaling.getSender().getUsername())
                .signalType(signaling.getSignalType())
                .data(signaling.getData())
                .createdAt(signaling.getCreatedAt())
                .build();
    }
}

