package com.team12345.messenger.service;

import com.team12345.messenger.dto.response.ConversationResponseDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.entity.*;
import com.team12345.messenger.repository.*;
import com.team12345.messenger.service.impl.ConversationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private MessageStatusRepository messageStatusRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private User testUser;
    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("testuser").build();
        testConversation = Conversation.builder()
                .id(1L)
                .name("Test Group")
                .isGroup(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetUserConversations() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Conversation> conversationPage = new PageImpl<>(Arrays.asList(testConversation));
        
        when(conversationRepository.findConversationsByUserId(1L, pageable)).thenReturn(conversationPage);
        when(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());
        when(messageStatusRepository.countUnreadInConversation(1L, 1L, MessageStatusEnum.read)).thenReturn(5L);

        Page<ConversationResponseDTO> result = conversationService.getUserConversations(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUnreadCount()).isEqualTo(5L);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Group");
    }

    @Test
    void testCreateConversation() {
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Conversation result = conversationService.createConversation("New Group", true, Arrays.asList(1L));

        assertThat(result).isNotNull();
        verify(conversationRepository).save(any(Conversation.class));
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    void testAddParticipant() {
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(userRepository.findById(2L)).thenReturn(Optional.of(User.builder().id(2L).build()));
        when(participantRepository.existsById(any(ParticipantId.class))).thenReturn(false);

        conversationService.addParticipant(1L, 2L);

        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    void testAddParticipant_AlreadyExists() {
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(userRepository.findById(2L)).thenReturn(Optional.of(User.builder().id(2L).build()));
        when(participantRepository.existsById(any(ParticipantId.class))).thenReturn(true);

        assertThatThrownBy(() -> conversationService.addParticipant(1L, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User is already a participant");
    }

    @Test
    void testRemoveParticipant() {
        when(participantRepository.existsById(any(ParticipantId.class))).thenReturn(true);

        conversationService.removeParticipant(1L, 1L);

        verify(participantRepository).deleteById(any(ParticipantId.class));
    }

    @Test
    void testGetConversationDetails() {
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());
        when(messageStatusRepository.countUnreadInConversation(1L, 1L, MessageStatusEnum.read)).thenReturn(3L);

        ConversationResponseDTO result = conversationService.getConversationDetails(1L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUnreadCount()).isEqualTo(3L);
    }

    @Test
    void testGetConversationMessages() {
        Pageable pageable = PageRequest.of(0, 10);
        Message message = Message.builder()
                .id(1L)
                .content("Hello")
                .sender(testUser)
                .conversation(testConversation)
                .createdAt(LocalDateTime.now())
                .build();
        Page<Message> messagePage = new PageImpl<>(Arrays.asList(message));

        when(conversationRepository.existsById(1L)).thenReturn(true);
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(messagePage);

        Page<MessageResponseDTO> result = conversationService.getConversationMessages(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Hello");
        assertThat(result.getContent().get(0).getSenderUsername()).isEqualTo("testuser");
    }
}