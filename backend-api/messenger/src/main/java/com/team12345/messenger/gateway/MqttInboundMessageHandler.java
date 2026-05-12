package com.team12345.messenger.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12345.messenger.dto.request.MessageRequestDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.entity.Conversation;
import com.team12345.messenger.entity.Participant;
import com.team12345.messenger.repository.ConversationRepository;
import com.team12345.messenger.repository.ParticipantRepository;
import com.team12345.messenger.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
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

        // Validate required fields
        if (requestDTO.getSenderId() == null || requestDTO.getConversationId() == null) {
            log.error("[Inbound] Malformed payload - missing senderId or conversationId");
            return;
        }
        if (requestDTO.getContent() == null || requestDTO.getContent().isBlank()) {
            log.error("[Inbound] Malformed payload - missing content from sender {}", requestDTO.getSenderId());
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
                // Send to user's topic
                String topic = "users/" + p.getUser().getUsername() + "/receive";
                try {
                    mqttGateway.sendToMqtt(objectToJson(responseDTO), topic);
                } catch (Exception ex) {
                    log.error("[Inbound] Failed to send message to {}: {}", topic, ex.getMessage());
                }
            }
        }

        // Send ACK to sender
        String ackTopic = "users/" + requestDTO.getSenderId() + "/ack";
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
