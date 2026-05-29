package com.team12345.messenger.service;

import com.team12345.messenger.entity.Message;
import com.team12345.messenger.entity.MessageStatus;
import com.team12345.messenger.entity.MessageStatusEnum;
import com.team12345.messenger.entity.MessageStatusId;
import com.team12345.messenger.entity.Conversation;
import com.team12345.messenger.entity.ParticipantId;
import com.team12345.messenger.gateway.MqttGateway;
import com.team12345.messenger.repository.MessageStatusRepository;
import com.team12345.messenger.repository.ParticipantRepository;
import com.team12345.messenger.service.impl.MessageStatusServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageStatusServiceTest {

    @Mock
    private MessageStatusRepository messageStatusRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private MqttGateway mqttGateway;

    @InjectMocks
    private MessageStatusServiceImpl messageStatusService;

    @Test
    void testMarkConversationAsRead_Success() {
        when(participantRepository.existsById(new ParticipantId(1L, 1L))).thenReturn(true);
        when(messageStatusRepository.markAsReadByConversationId(1L, 1L, MessageStatusEnum.read)).thenReturn(2);

        messageStatusService.markConversationAsRead(1L, 1L);

        verify(messageStatusRepository).markAsReadByConversationId(1L, 1L, MessageStatusEnum.read);
        verify(mqttGateway).sendToMqtt(anyString(), eq("conversation/1/read"));
    }

    @Test
    void testMarkConversationAsRead_NotParticipant() {
        when(participantRepository.existsById(new ParticipantId(1L, 1L))).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> {
            messageStatusService.markConversationAsRead(1L, 1L);
        });

        verify(messageStatusRepository, never()).markAsReadByConversationId(anyLong(), anyLong(), any());
        verify(mqttGateway, never()).sendToMqtt(anyString(), anyString());
    }

    @Test
    void testUpdateMessageStatus_Success() {
        MessageStatus ms = new MessageStatus();
        Message m = new Message();
        Conversation c = new Conversation();
        c.setId(1L);
        m.setConversation(c);
        ms.setMessage(m);
        ms.setStatus(MessageStatusEnum.delivered);

        when(messageStatusRepository.findById(new MessageStatusId(1L, 1L))).thenReturn(Optional.of(ms));

        messageStatusService.updateMessageStatus(1L, 1L, "READ");

        verify(messageStatusRepository).save(ms);
        assertThat(ms.getStatus()).isEqualTo(MessageStatusEnum.read);
        verify(mqttGateway).sendToMqtt(anyString(), eq("conversation/1/message-status"));
    }

    @Test
    void testUpdateMessageStatus_InvalidStatus() {
        assertThrows(IllegalArgumentException.class, () -> {
            messageStatusService.updateMessageStatus(1L, 1L, "INVALID");
        });
    }

    @Test
    void testCountUnreadMessages() {
        when(messageStatusRepository.countUnreadInConversation(1L, 1L, MessageStatusEnum.read)).thenReturn(5L);
        
        long count = messageStatusService.countUnreadMessages(1L, 1L);
        
        assertThat(count).isEqualTo(5L);
        verify(messageStatusRepository).countUnreadInConversation(1L, 1L, MessageStatusEnum.read);
    }

    @Test
    void testCountTotalUnreadMessages() {
        when(messageStatusRepository.countById_ReceiverIdAndStatusNot(1L, MessageStatusEnum.read)).thenReturn(10L);
        
        long count = messageStatusService.countTotalUnreadMessages(1L);
        
        assertThat(count).isEqualTo(10L);
        verify(messageStatusRepository).countById_ReceiverIdAndStatusNot(1L, MessageStatusEnum.read);
    }
}

