package com.team12345.messenger.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12345.messenger.dto.request.MessageRequestDTO;
import com.team12345.messenger.dto.response.SaveMessageResult;
import com.team12345.messenger.entity.Participant;
import com.team12345.messenger.repository.ParticipantRepository;
import com.team12345.messenger.service.MessageService;
import com.team12345.messenger.gateway.MqttGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttInboundMessageHandler implements MessageHandler {

    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final ParticipantRepository participantRepository;
    private final MqttGateway mqttGateway;

    @Override
    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleMessage(Message<?> message) throws MessagingException {
        long startTime = System.currentTimeMillis();
        String payload = (String) message.getPayload();
        
        try {
            // 1. Parsing
            MessageRequestDTO requestDTO = objectMapper.readValue(payload, MessageRequestDTO.class);
            
            // 2. Initial Validation (Security Check)
            if (requestDTO.getConversationId() == null || requestDTO.getSenderId() == null) {
                log.error("Invalid message payload: Missing conversationId or senderId");
                return;
            }

            // Check if sender is a participant in the conversation
            List<Participant> participants = participantRepository.findById_ConversationId(requestDTO.getConversationId());
            boolean isParticipant = participants.stream()
                    .anyMatch(p -> p.getUser().getId().equals(requestDTO.getSenderId()));

            if (!isParticipant) {
                log.warn("Unauthorized access: User {} is not a participant in conversation {}", 
                        requestDTO.getSenderId(), requestDTO.getConversationId());
                // Send error message back to sender
                sendErrorToSender(requestDTO.getSenderId(), "Unauthorized to send messages in this conversation");
                return;
            }

            // 3. Persistence (Save to Database)
            // The service handles validation of whether the conversation and user actually exist,
            // updating the conversation's updatedAt, and creating message status records.
            SaveMessageResult savedMessageResult = messageService.saveMessage(requestDTO);

            // 4. Forwarding & Real-time Broadcast
            for (Participant participant : participants) {
                Long receiverId = participant.getUser().getId();
                // Don't send the message back to the sender in the normal flow
                if (!receiverId.equals(requestDTO.getSenderId())) {
                    String topic = String.format("users/%d/receive", receiverId);
                    String messageJson = objectMapper.writeValueAsString(savedMessageResult.message());
                    mqttGateway.sendToMqtt(messageJson, topic, 1);
                }
            }

            // 5. ACK back to sender
            sendAckToSender(requestDTO.getSenderId(), savedMessageResult.message().getMessageId(), requestDTO.getClientMessageId());

            // 6. Logging & Monitor
            long duration = System.currentTimeMillis() - startTime;
            log.info("[Inbound] User {} -> Conversation {}: '{}'. Latency: {}ms", 
                    requestDTO.getSenderId(), requestDTO.getConversationId(), 
                    requestDTO.getContent() != null ? (requestDTO.getContent().length() > 20 ? requestDTO.getContent().substring(0, 20) + "..." : requestDTO.getContent()) : "Media", 
                    duration);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse incoming MQTT message: {}", payload, e);
        } catch (Exception e) {
            log.error("Error processing MQTT message", e);
        }
    }

    private void sendErrorToSender(Long senderId, String errorMessage) {
        try {
            String topic = String.format("users/%d/error", senderId);
            String errorJson = String.format("{\"error\": \"%s\"}", errorMessage);
            mqttGateway.sendToMqtt(errorJson, topic, 1);
        } catch (Exception e) {
            log.error("Failed to send error message back to sender {}", senderId, e);
        }
    }

    private void sendAckToSender(Long senderId, Long serverMessageId, String clientMessageId) {
        try {
            String topic = String.format("users/%d/ack", senderId);
            String ackJson = String.format("{\"serverMessageId\": %d, \"clientMessageId\": \"%s\", \"status\": \"SENT\"}", 
                    serverMessageId, clientMessageId != null ? clientMessageId : "");
            mqttGateway.sendToMqtt(ackJson, topic, 1);
        } catch (Exception e) {
            log.error("Failed to send ACK back to sender {}", senderId, e);
        }
    }
}