package com.team12345.messenger.service;

import com.team12345.messenger.repository.MessageStatusRepository;
import com.team12345.messenger.service.impl.MessageStatusServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageStatusServiceTest {

    @Mock
    private MessageStatusRepository messageStatusRepository;

    @InjectMocks
    private MessageStatusServiceImpl messageStatusService;

    @Test
    void testMarkAsRead() {
        messageStatusService.markAsRead(1L, 1L);
        verify(messageStatusRepository).markAsRead(1L, 1L);
    }

    @Test
    void testCountUnreadMessages() {
        when(messageStatusRepository.countUnreadMessages(1L, 1L)).thenReturn(5L);
        
        long count = messageStatusService.countUnreadMessages(1L, 1L);
        
        assertThat(count).isEqualTo(5L);
        verify(messageStatusRepository).countUnreadMessages(1L, 1L);
    }
}
