package com.team12345.messenger.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12345.messenger.dto.request.MessageRequestDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.entity.Conversation;
import com.team12345.messenger.entity.Participant;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.repository.ConversationRepository;
import com.team12345.messenger.repository.ParticipantRepository;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttInboundMessageHandler {
    private final ObjectMapper objectMapper;
    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final MqttGateway mqttGateway;

    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleInboundMessage(Message<byte[]> mqttMessage) {
        long start = System.currentTimeMillis();
        String payload = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8);
        MessageRequestDTO requestDTO;
        try {
            requestDTO = objectMapper.readValue(payload, MessageRequestDTO.class);
        } catch (Exception e) {
            log.error("[Inbound] JSON parse error: {}", e.getMessage());
            // Optionally send error to sender
            return;
        }

        // Authenticate sender from topic (chat/server/incoming/{username})
        // Never trust senderId from the payload — resolve identity from the broker-authenticated topic
        String receivedTopic = (String) mqttMessage.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        if (receivedTopic == null) {
            log.error("[Inbound] Missing MQTT topic header");
            return;
        }
        // Expected: chat/server/incoming/{username}
        String[] parts = receivedTopic.split("/");
        if (parts.length < 4) {
            log.error("[Inbound] Unexpected topic format: {}", receivedTopic);
            return;
        }
        String authenticatedUsername = parts[3];
        Optional<User> senderOpt = userRepository.findByUsername(authenticatedUsername);
        if (senderOpt.isEmpty()) {
            log.error("[Inbound] Authenticated user not found: {}", authenticatedUsername);
            return;
        }
        // Overwrite senderId from DB — payload value is ignored for authorization
        requestDTO.setSenderId(senderOpt.get().getId());

        // Validate required fields
        if (requestDTO.getConversationId() == null) {
            log.error("[Inbound] Malformed payload - missing conversationId from sender {}", authenticatedUsername);
            return;
        }
        if (requestDTO.getContent() == null || requestDTO.getContent().isBlank()) {
            log.error("[Inbound] Malformed payload - missing content from sender {}", authenticatedUsername);
            return;
        }

        // Validate conversation
        Optional<Conversation> conversationOpt = conversationRepository.findById(requestDTO.getConversationId());
        if (conversationOpt.isEmpty()) {
            log.error("[Inbound] Conversation {} not found", requestDTO.getConversationId());
            // Optionally send error to sender
            return;
        }

        // Validate participant
        List<Participant> participants = participantRepository.findById_ConversationId(requestDTO.getConversationId());
        boolean isParticipant = participants.stream().anyMatch(p -> p.getUser().getId().equals(requestDTO.getSenderId()));
        if (!isParticipant) {
            log.error("[Inbound] User {} is not a participant of conversation {}", requestDTO.getSenderId(), requestDTO.getConversationId());
            // Optionally send error to sender
            return;
        }

        // Save message (returns MessageResponseDTO)
        MessageResponseDTO responseDTO = messageService.saveMessage(requestDTO);

        // Forward to all participants except sender
        for (Participant p : participants) {
            if (!p.getUser().getId().equals(requestDTO.getSenderId())) {
                String topic = "user/" + p.getUser().getId() + "/messages";
                try {
                    mqttGateway.sendToMqtt(objectToJson(responseDTO), topic);
                } catch (Exception ex) {
                    log.error("[Inbound] Failed to send message to {}: {}", topic, ex.getMessage());
                }
            }
        }

        // Send ACK to sender
        String ackTopic = "user/" + requestDTO.getSenderId() + "/ack";
        try {
            mqttGateway.sendToMqtt("{\"ack\":true,\"messageId\":" + responseDTO.getMessageId() + "}", ackTopic);
        } catch (Exception ex) {
            log.error("[Inbound] Failed to send ACK to {}: {}", ackTopic, ex.getMessage());
        }

        // Logging
        log.info("[Inbound] User {} -> Conversation {}: \"{}\" (latency: {}ms)",
                requestDTO.getSenderId(), requestDTO.getConversationId(), requestDTO.getContent(),
                System.currentTimeMillis() - start);
    }

    private String objectToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
