package com.team12345.messenger.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12345.messenger.dto.request.MessageRequestDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.dto.response.SaveMessageResult;
import com.team12345.messenger.entity.Participant;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MqttInboundMessageHandlerTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private UserRepository userRepository;
    @Mock private MessageService messageService;
    @Mock private MqttGateway mqttGateway;

    @InjectMocks
    private MqttInboundMessageHandler handler;

    // ── helpers ───────────────────────────────────────────────────────────────

    private Message<byte[]> msg(String payload, String topic) {
        var builder = MessageBuilder.withPayload(payload.getBytes());
        if (topic != null) builder.setHeader(MqttHeaders.RECEIVED_TOPIC, topic);
        return builder.build();
    }

    private MessageRequestDTO dtoWith(Long conversationId, String content) {
        MessageRequestDTO dto = new MessageRequestDTO();
        dto.setConversationId(conversationId);
        dto.setContent(content);
        return dto;
    }

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void validPayload_shouldSendToParticipantsAndAck() throws Exception {
        String payload = "{\"conversationId\":1,\"content\":\"Hello\"}";
        MessageRequestDTO dto = dtoWith(1L, "Hello");
        User user = User.builder().id(10L).username("testuser").build();

        when(objectMapper.readValue(payload, MessageRequestDTO.class)).thenReturn(dto);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        MessageResponseDTO responseDTO = MessageResponseDTO.builder().messageId(100L).content("Hello").build();
        Participant p1 = Participant.builder().user(User.builder().id(10L).build()).build();
        Participant p2 = Participant.builder().user(User.builder().id(20L).build()).build();
        when(messageService.saveMessage(any())).thenReturn(new SaveMessageResult(responseDTO, List.of(p1, p2)));
        when(objectMapper.writeValueAsString(any())).thenReturn("json");

        handler.handleInboundMessage(msg(payload, "chat/server/incoming/testuser"));

        verify(mqttGateway).sendToMqtt(eq("json"), eq("user/20/messages"));
        verify(mqttGateway).sendToMqtt(contains("\"ack\":true"), eq("user/10/ack"));
    }

    // ── early-exit cases ──────────────────────────────────────────────────────

    @Test
    void invalidJson_shouldReturnWithoutCallingDownstream() throws Exception {
        when(objectMapper.readValue(anyString(), eq(MessageRequestDTO.class)))
                .thenThrow(new RuntimeException("parse error"));

        handler.handleInboundMessage(msg("bad_json", "chat/server/incoming/testuser"));

        verifyNoInteractions(userRepository, messageService, mqttGateway);
    }

    @Test
    void missingTopic_shouldReturnAfterParse() throws Exception {
        String payload = "{\"conversationId\":1,\"content\":\"Hello\"}";
        when(objectMapper.readValue(payload, MessageRequestDTO.class)).thenReturn(dtoWith(1L, "Hello"));

        handler.handleInboundMessage(msg(payload, null));

        verifyNoInteractions(userRepository, messageService, mqttGateway);
    }

    @Test
    void topicTooShort_shouldReturnAfterParse() throws Exception {
        String payload = "{\"conversationId\":1,\"content\":\"Hello\"}";
        when(objectMapper.readValue(payload, MessageRequestDTO.class)).thenReturn(dtoWith(1L, "Hello"));

        handler.handleInboundMessage(msg(payload, "chat/server/incoming"));

        verifyNoInteractions(userRepository, messageService, mqttGateway);
    }

    @Test
    void userNotFound_shouldReturnWithoutSaving() throws Exception {
        String payload = "{\"conversationId\":1,\"content\":\"Hello\"}";
        when(objectMapper.readValue(payload, MessageRequestDTO.class)).thenReturn(dtoWith(1L, "Hello"));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        handler.handleInboundMessage(msg(payload, "chat/server/incoming/testuser"));

        verifyNoInteractions(messageService, mqttGateway);
    }

    @Test
    void missingConversationId_shouldReturnWithoutSaving() throws Exception {
        String payload = "{\"content\":\"Hello\"}";
        MessageRequestDTO dto = dtoWith(null, "Hello");
        User user = User.builder().id(10L).username("testuser").build();
        when(objectMapper.readValue(payload, MessageRequestDTO.class)).thenReturn(dto);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        handler.handleInboundMessage(msg(payload, "chat/server/incoming/testuser"));

        verifyNoInteractions(messageService, mqttGateway);
    }

    @Test
    void missingContent_shouldReturnWithoutSaving() throws Exception {
        String payload = "{\"conversationId\":1}";
        MessageRequestDTO dto = dtoWith(1L, null);
        User user = User.builder().id(10L).username("testuser").build();
        when(objectMapper.readValue(payload, MessageRequestDTO.class)).thenReturn(dto);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        handler.handleInboundMessage(msg(payload, "chat/server/incoming/testuser"));

        verifyNoInteractions(messageService, mqttGateway);
    }

    @Test
    void saveMessageThrows_shouldNotPublish() throws Exception {
        String payload = "{\"conversationId\":1,\"content\":\"Hello\"}";
        User user = User.builder().id(10L).username("testuser").build();
        when(objectMapper.readValue(payload, MessageRequestDTO.class)).thenReturn(dtoWith(1L, "Hello"));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(messageService.saveMessage(any())).thenThrow(new RuntimeException("db error"));

        handler.handleInboundMessage(msg(payload, "chat/server/incoming/testuser"));

        verifyNoInteractions(mqttGateway);
    }

    @Test
    void serializationFailure_shouldSkipParticipantPublishButStillAck() throws Exception {
        String payload = "{\"conversationId\":1,\"content\":\"Hello\"}";
        User user = User.builder().id(10L).username("testuser").build();
        when(objectMapper.readValue(payload, MessageRequestDTO.class)).thenReturn(dtoWith(1L, "Hello"));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        MessageResponseDTO responseDTO = MessageResponseDTO.builder().messageId(100L).build();
        Participant p1 = Participant.builder().user(User.builder().id(10L).build()).build();
        Participant p2 = Participant.builder().user(User.builder().id(20L).build()).build();
        when(messageService.saveMessage(any())).thenReturn(new SaveMessageResult(responseDTO, List.of(p1, p2)));
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("fail") {});

        handler.handleInboundMessage(msg(payload, "chat/server/incoming/testuser"));

        verify(mqttGateway, never()).sendToMqtt(anyString(), eq("user/20/messages"));
        verify(mqttGateway).sendToMqtt(contains("\"ack\":true"), eq("user/10/ack"));
    }
}
