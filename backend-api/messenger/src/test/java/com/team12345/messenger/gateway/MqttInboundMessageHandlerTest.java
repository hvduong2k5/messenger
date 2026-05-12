package com.team12345.messenger.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12345.messenger.dto.request.MessageRequestDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.dto.response.SaveMessageResult;
import com.team12345.messenger.entity.Participant;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MqttInboundMessageHandlerTest {
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MessageService messageService;
    @Mock
    private MqttGateway mqttGateway;

    @InjectMocks
    private MqttInboundMessageHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new MqttInboundMessageHandler(objectMapper, userRepository, messageService, mqttGateway);
    }

    @Test
    void handleInboundMessage_validPayload_shouldSendToParticipantsAndAck() throws Exception {
        String topic = "chat/server/incoming/testuser";
        String payload = "{\"conversationId\":1,\"content\":\"Hello\"}";
        MessageRequestDTO dto = new MessageRequestDTO();
        dto.setConversationId(1L);
        dto.setContent("Hello");
        User user = User.builder().id(10L).username("testuser").build();
        when(objectMapper.readValue(payload, MessageRequestDTO.class)).thenReturn(dto);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        MessageResponseDTO responseDTO = MessageResponseDTO.builder().messageId(100L).content("Hello").build();
        Participant p1 = Participant.builder().user(User.builder().id(10L).build()).build();
        Participant p2 = Participant.builder().user(User.builder().id(20L).build()).build();
        SaveMessageResult result = new SaveMessageResult(responseDTO, List.of(p1, p2));
        when(messageService.saveMessage(any())).thenReturn(result);
        when(objectMapper.writeValueAsString(any())).thenReturn("json");
        Message<byte[]> message = MessageBuilder.withPayload(payload.getBytes())
                .setHeader(MqttHeaders.RECEIVED_TOPIC, topic)
                .build();
        handler.handleInboundMessage(message);
        // Gửi cho participant khác
        verify(mqttGateway).sendToMqtt(eq("json"), eq("user/20/messages"));
        // Gửi ACK cho sender
        verify(mqttGateway).sendToMqtt(contains("\"ack\":true"), eq("user/10/ack"));
    }

    @Test
    void handleInboundMessage_invalidJson_shouldLogAndReturn() throws Exception {
        String topic = "chat/server/incoming/testuser";
        String payload = "invalid_json";
        when(objectMapper.readValue(anyString(), eq(MessageRequestDTO.class))).thenThrow(new RuntimeException("parse error"));
        Message<byte[]> message = MessageBuilder.withPayload(payload.getBytes())
                .setHeader(MqttHeaders.RECEIVED_TOPIC, topic)
                .build();
        handler.handleInboundMessage(message);
        verifyNoInteractions(userRepository, messageService, mqttGateway);
    }

    @Test
    void handleInboundMessage_missingTopic_shouldLogAndReturn() throws Exception {
        String payload = "{\"conversationId\":1,\"content\":\"Hello\"}";
        Message<byte[]> message = MessageBuilder.withPayload(payload.getBytes()).build();
        handler.handleInboundMessage(message);
        verifyNoInteractions(userRepository, messageService, mqttGateway);
    }

    @Test
    void handleInboundMessage_topicWrongFormat_shouldLogAndReturn() throws Exception {
        String topic = "chat/server/incoming";
        String payload = "{\"conversationId\":1,\"content\":\"Hello\"}";
        Message<byte[]> message = MessageBuilder.withPayload(payload.getBytes())
                .setHeader(MqttHeaders.RECEIVED_TOPIC, topic)
                .build();
        handler.handleInboundMessage(message);
        verifyNoInteractions(userRepository, messageService, mqttGateway);
    }

    @Test
    void handleInboundMessage_userNotFound_shouldLogAndReturn() throws Exception {
        String topic = "chat/server/incoming/testuser";
        String payload = "{\"conversationId\":1,\"content\":\"Hello\"}";
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(objectMapper.readValue(payload, MessageRequestDTO.class)).thenReturn(new MessageRequestDTO());
        Message<byte[]> message = MessageBuilder.withPayload(payload.getBytes())
                .setHeader(MqttHeaders.RECEIVED_TOPIC, topic)
                .build();
        handler.handleInboundMessage(message);
        verifyNoInteractions(messageService, mqttGateway);
    }

    @Test
    void handleInboundMessage_missingConversationId_shouldLogAndReturn() throws Exception {
        String topic = "chat/server/incoming/testuser";
        String payload = "{\"content\":\"Hello\"}";
        MessageRequestDTO dto = new MessageRequestDTO();
        dto.setContent("Hello");
        User user = User.builder().id(10L).username("testuser").build();
        when(objectMapper.readValue(payload, MessageRequestDTO.class)).thenReturn(dto);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Message<byte[]> message = MessageBuilder.withPayload(payload.getBytes())
                .setHeader(MqttHeaders.RECEIVED_TOPIC, topic)
                .build();
        handler.handleInboundMessage(message);
        verifyNoInteractions(messageService, mqttGateway);
    }

    @Test
    void handleInboundMessage_missingContent_shouldLogAndReturn() throws Exception {
        String topic = "chat/server/incoming/testuser";
        String payload = "{\"conversationId\":1}";
        MessageRequestDTO dto = new MessageRequestDTO();
        dto.setConversationId(1L);
        User user = User.builder().id(10L).username("testuser").build();
        when(objectMapper.readValue(payload, MessageRequestDTO.class)).thenReturn(dto);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Message<byte[]> message = MessageBuilder.withPayload(payload.getBytes())
                .setHeader(MqttHeaders.RECEIVED_TOPIC, topic)
                .build();
        handler.handleInboundMessage(message);
        verifyNoInteractions(messageService, mqttGateway);
    }

    @Test
    void handleInboundMessage_saveMessageThrows_shouldLogAndReturn() throws Exception {
        String topic = "chat/server/incoming/testuser";
        String payload = "{\"conversationId\":1,\"content\":\"Hello\"}";
        MessageRequestDTO dto = new MessageRequestDTO();
        dto.setConversationId(1L);
        dto.setContent("Hello");
        User user = User.builder().id(10L).username("testuser").build();
        when(objectMapper.readValue(payload, MessageRequestDTO.class)).thenReturn(dto);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(messageService.saveMessage(any())).thenThrow(new RuntimeException("db error"));
        Message<byte[]> message = MessageBuilder.withPayload(payload.getBytes())
                .setHeader(MqttHeaders.RECEIVED_TOPIC, topic)
                .build();
        handler.handleInboundMessage(message);
        verifyNoInteractions(mqttGateway);
    }
}

