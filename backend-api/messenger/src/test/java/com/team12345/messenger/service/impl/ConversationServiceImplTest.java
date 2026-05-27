package com.team12345.messenger.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12345.messenger.dto.response.ParticipantResponseDTO;
import com.team12345.messenger.entity.Participant;
import com.team12345.messenger.entity.ParticipantId;
import com.team12345.messenger.entity.ParticipantRole;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.gateway.MqttGateway;
import com.team12345.messenger.repository.ConversationRepository;
import com.team12345.messenger.repository.MessageRepository;
import com.team12345.messenger.repository.MessageStatusRepository;
import com.team12345.messenger.repository.ParticipantRepository;
import com.team12345.messenger.repository.UserRepository;
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
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

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

    @Mock
    private MqttGateway mqttGateway;

    private ObjectMapper objectMapper = new ObjectMapper();

    private ConversationServiceImpl conversationService;

    private User currentUser;
    private Participant currentParticipant;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        conversationService = new ConversationServiceImpl(
                conversationRepository,
                participantRepository,
                messageRepository,
                messageStatusRepository,
                userRepository,
                mqttGateway,
                objectMapper
        );

        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("currentUser");

        currentParticipant = Participant.builder()
                .id(new ParticipantId(10L, 1L))
                .user(currentUser)
                .role(ParticipantRole.member)
                .joinedAt(LocalDateTime.now())
                .build();

        pageable = PageRequest.of(0, 20);
    }

    @Test
    void testGetParticipants_SearchWithKeyword_CaseInsensitive() {
        // Arrange
        Long conversationId = 10L;
        Long currentUserId = 1L;
        String keyword = "jOhN";

        User searchUser = new User();
        searchUser.setId(2L);
        searchUser.setUsername("Johnny");

        Participant searchParticipant = Participant.builder()
                .id(new ParticipantId(conversationId, 2L))
                .user(searchUser)
                .role(ParticipantRole.member)
                .joinedAt(LocalDateTime.now())
                .build();

        when(participantRepository.existsById(new ParticipantId(conversationId, currentUserId))).thenReturn(true);
        when(participantRepository.findById_ConversationIdAndUser_UsernameContainingIgnoreCase(
                eq(conversationId), eq("jOhN"), eq(pageable)
        )).thenReturn(new PageImpl<>(List.of(searchParticipant)));

        // Act
        Page<ParticipantResponseDTO> result = conversationService.getParticipants(conversationId, currentUserId, keyword, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Johnny", result.getContent().get(0).getUsername());
    }

    @Test
    void testGetParticipants_SearchWithKeyword_PartialMatch() {
        // Arrange
        Long conversationId = 10L;
        Long currentUserId = 1L;
        String keyword = "hn";

        User searchUser = new User();
        searchUser.setId(2L);
        searchUser.setUsername("Johnny");

        Participant searchParticipant = Participant.builder()
                .id(new ParticipantId(conversationId, 2L))
                .user(searchUser)
                .role(ParticipantRole.member)
                .joinedAt(LocalDateTime.now())
                .build();

        when(participantRepository.existsById(new ParticipantId(conversationId, currentUserId))).thenReturn(true);
        when(participantRepository.findById_ConversationIdAndUser_UsernameContainingIgnoreCase(
                eq(conversationId), eq("hn"), eq(pageable)
        )).thenReturn(new PageImpl<>(List.of(searchParticipant)));

        // Act
        Page<ParticipantResponseDTO> result = conversationService.getParticipants(conversationId, currentUserId, keyword, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Johnny", result.getContent().get(0).getUsername());
    }

    @Test
    void testGetParticipants_NoKeyword_ReturnsAll() {
        // Arrange
        Long conversationId = 10L;
        Long currentUserId = 1L;
        String keyword = "";

        when(participantRepository.existsById(new ParticipantId(conversationId, currentUserId))).thenReturn(true);
        when(participantRepository.findById_ConversationId(eq(conversationId), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(currentParticipant)));

        // Act
        Page<ParticipantResponseDTO> result = conversationService.getParticipants(conversationId, currentUserId, keyword, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("currentUser", result.getContent().get(0).getUsername());
    }

    @Test
    void testGetParticipants_NotParticipant_ThrowsException() {
        // Arrange
        Long conversationId = 10L;
        Long currentUserId = 1L;

        when(participantRepository.existsById(new ParticipantId(conversationId, currentUserId))).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            conversationService.getParticipants(conversationId, currentUserId, "kw", pageable);
        });
    }
    @Test
    void testRemoveParticipant_TargetIsSelf_ThrowsException() {
        Long conversationId = 10L;
        Long currentUserId = 1L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            conversationService.removeParticipant(conversationId, currentUserId, currentUserId);
        });
        assertTrue(exception.getMessage().contains("không thể tự xóa chính mình"));
    }

    @Test
    void testRemoveParticipant_NotAdmin_ThrowsException() {
        Long conversationId = 10L;
        Long currentUserId = 1L;
        Long targetUserId = 2L;

        com.team12345.messenger.entity.Conversation conversation = new com.team12345.messenger.entity.Conversation();
        conversation.setIsGroup(true);
        currentParticipant.setConversation(conversation);
        currentParticipant.setRole(ParticipantRole.member);

        when(participantRepository.existsById(new ParticipantId(conversationId, targetUserId))).thenReturn(true);
        when(participantRepository.findById(new ParticipantId(conversationId, currentUserId)))
                .thenReturn(java.util.Optional.of(currentParticipant));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            conversationService.removeParticipant(conversationId, currentUserId, targetUserId);
        });
        assertTrue(exception.getMessage().contains("Only admins can remove other participants"));
    }

    @Test
    void testUpdateParticipantRole_TargetIsSelf_ThrowsException() {
        Long conversationId = 10L;
        Long currentUserId = 1L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            conversationService.updateParticipantRole(conversationId, currentUserId, currentUserId, "ADMIN");
        });
        assertTrue(exception.getMessage().contains("không được tự cập nhật vai trò của chính mình"));
    }

    @Test
    void testUpdateParticipantRole_NotAdmin_ThrowsException() {
        Long conversationId = 10L;
        Long currentUserId = 1L;
        Long targetUserId = 2L;

        currentParticipant.setRole(ParticipantRole.member);

        when(participantRepository.findById(new ParticipantId(conversationId, currentUserId)))
                .thenReturn(java.util.Optional.of(currentParticipant));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            conversationService.updateParticipantRole(conversationId, currentUserId, targetUserId, "ADMIN");
        });
        assertTrue(exception.getMessage().contains("Only admins can update roles"));
    }
}
