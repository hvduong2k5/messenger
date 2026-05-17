package com.team12345.messenger.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12345.messenger.dto.request.MessageRequestDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.dto.response.SaveMessageResult;
import com.team12345.messenger.entity.*;
import com.team12345.messenger.exception.ResourceNotFoundException;
import com.team12345.messenger.gateway.MqttGateway;
import com.team12345.messenger.repository.*;
import com.team12345.messenger.service.MediaService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock private MessageRepository messageRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private UserRepository userRepository;
    @Mock private ParticipantRepository participantRepository;
    @Mock private MessageStatusRepository messageStatusRepository;
    @Mock private MediaService mediaService;
    @Mock private MqttGateway mqttGateway;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    private User sender;
    private User receiver;
    private Conversation conversation;
    private MessageRequestDTO request;

    @BeforeEach
    void setUp() throws Exception {
        sender       = User.builder().id(1L).username("sender").avatarUrl("avatar.jpg").build();
        receiver     = User.builder().id(2L).username("receiver").build();
        conversation = Conversation.builder().id(100L).build();
        request      = MessageRequestDTO.builder()
                .senderId(sender.getId())
                .conversationId(conversation.getId())
                .content("Hello, World!")
                .clientMessageId("client-123")
                .build();
        
        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static void setAuditFields(BaseEntity entity) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Field createdAt = BaseEntity.class.getDeclaredField("createdAt");
            createdAt.setAccessible(true);
            createdAt.set(entity, now);
            Field updatedAt = BaseEntity.class.getDeclaredField("updatedAt");
            updatedAt.setAccessible(true);
            updatedAt.set(entity, now);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Participant> twoParticipants() {
        return Arrays.asList(
                Participant.builder().user(sender).build(),
                Participant.builder().user(receiver).build()
        );
    }

    // ── saveMessage ───────────────────────────────────────────────────────────

    @Test
    void saveMessage_withTextOnly_shouldSaveAndCreateStatuses() {
        when(participantRepository.findById_ConversationId(conversation.getId())).thenReturn(twoParticipants());
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            m.setId(1L);
            setAuditFields(m);
            return m;
        });

        SaveMessageResult result = messageService.saveMessage(request);

        assertThat(result.message().getContent()).isEqualTo("Hello, World!");
        assertThat(result.message().getSenderId()).isEqualTo(sender.getId());
        assertThat(result.message().getAttachments()).isEmpty();
        verify(messageRepository).save(any(Message.class));
        verify(messageStatusRepository).saveAll(anyList());
        verify(mqttGateway, times(2)).sendToMqtt(anyString(), anyString());
        verify(mediaService, never()).uploadFile(any());
    }

    @Test
    void saveMessage_withAttachments_shouldUploadAndSave() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "data".getBytes());
        request.setFiles(List.of(file));

        when(participantRepository.findById_ConversationId(conversation.getId())).thenReturn(twoParticipants());
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mediaService.uploadFile(any())).thenReturn(Map.of(
                "url", "http://cdn.com/test.jpg", "type", "IMAGE", "size", 1234));

        SaveMessageResult result = messageService.saveMessage(request);

        assertThat(result.message().getAttachments()).hasSize(1);
        assertThat(result.message().getAttachments().get(0).getUrl()).isEqualTo("http://cdn.com/test.jpg");
        verify(mediaService).uploadFile(any());
        verify(mqttGateway, times(2)).sendToMqtt(anyString(), anyString());
    }

    @Test
    void saveMessage_whenSenderNotParticipant_shouldThrowBeforeHittingDB() {
        when(participantRepository.findById_ConversationId(conversation.getId()))
                .thenReturn(List.of(Participant.builder().user(receiver).build()));

        assertThrows(IllegalArgumentException.class, () -> messageService.saveMessage(request));
        verify(messageRepository, never()).save(any());
    }

    @Test
    void saveMessage_whenConversationNotFound_shouldThrow() {
        when(participantRepository.findById_ConversationId(conversation.getId()))
                .thenReturn(List.of(Participant.builder().user(sender).build()));
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> messageService.saveMessage(request));
    }

    @Test
    void saveMessage_shouldReturnParticipantsInResult() {
        when(participantRepository.findById_ConversationId(conversation.getId())).thenReturn(twoParticipants());
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(messageRepository.save(any())).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        SaveMessageResult result = messageService.saveMessage(request);

        assertThat(result.participants()).hasSize(2);
    }

    // ── revokeMessage ─────────────────────────────────────────────────────────

    @Test
    void revokeMessage_bySender_shouldMarkDeleted() {
        Message msg = Message.builder().id(1L).sender(sender).conversation(conversation)
                .content("Hi").isDeleted(false).build();
        when(messageRepository.findById(1L)).thenReturn(Optional.of(msg));
        when(messageRepository.save(any())).thenReturn(msg);
        when(participantRepository.findById_ConversationId(conversation.getId())).thenReturn(List.of());

        messageService.revokeMessage(1L, sender.getId());

        assertThat(msg.getIsDeleted()).isTrue();
        verify(messageRepository).save(msg);
    }

    @Test
    void revokeMessage_byNonSender_shouldThrowAccessDenied() {
        Message msg = Message.builder().id(1L).sender(sender).conversation(conversation).build();
        when(messageRepository.findById(1L)).thenReturn(Optional.of(msg));

        assertThrows(AccessDeniedException.class, () -> messageService.revokeMessage(1L, receiver.getId()));
        verify(messageRepository, never()).save(any());
    }

    @Test
    void revokeMessage_whenNotFound_shouldThrow() {
        when(messageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> messageService.revokeMessage(99L, sender.getId()));
    }

    // ── editMessage ───────────────────────────────────────────────────────────

    @Test
    void editMessage_bySender_shouldUpdateContent() {
        Message msg = Message.builder().id(1L).sender(sender).conversation(conversation)
                .content("old").isDeleted(false).isEdited(false).build();
        when(messageRepository.findById(1L)).thenReturn(Optional.of(msg));
        when(messageRepository.save(any())).thenReturn(msg);
        when(participantRepository.findById_ConversationId(conversation.getId())).thenReturn(List.of());

        MessageResponseDTO result = messageService.editMessage(1L, sender.getId(), "new content");

        assertThat(result.getContent()).isEqualTo("new content");
        assertThat(msg.getIsEdited()).isTrue();
    }

    @Test
    void editMessage_byNonSender_shouldThrowAccessDenied() {
        Message msg = Message.builder().id(1L).sender(sender).conversation(conversation).build();
        when(messageRepository.findById(1L)).thenReturn(Optional.of(msg));

        assertThrows(AccessDeniedException.class,
                () -> messageService.editMessage(1L, receiver.getId(), "new"));
    }

    @Test
    void editMessage_onDeletedMessage_shouldThrow() {
        Message msg = Message.builder().id(1L).sender(sender).conversation(conversation)
                .isDeleted(true).build();
        when(messageRepository.findById(1L)).thenReturn(Optional.of(msg));

        assertThrows(IllegalArgumentException.class,
                () -> messageService.editMessage(1L, sender.getId(), "new"));
    }

    // ── getMessagesByConversation ─────────────────────────────────────────────

    @Test
    void getMessagesByConversation_shouldReturnPagedMessages() {
        Pageable pageable = PageRequest.of(0, 20);
        Message msg = Message.builder().id(1L).sender(sender).conversation(conversation).content("Hi").build();
        Page<Message> page = new PageImpl<>(List.of(msg), pageable, 1);

        when(participantRepository.existsById(new ParticipantId(conversation.getId(), sender.getId()))).thenReturn(true);
        when(conversationRepository.existsById(conversation.getId())).thenReturn(true);
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(conversation.getId(), pageable)).thenReturn(page);

        Page<MessageResponseDTO> result = messageService.getMessagesByConversation(conversation.getId(), sender.getId(), pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Hi");
        assertThat(result.getContent().get(0).getSenderUsername()).isEqualTo("sender");
    }

    @Test
    void getMessagesByConversation_whenNotParticipant_shouldThrowAccessDenied() {
        when(participantRepository.existsById(new ParticipantId(conversation.getId(), sender.getId()))).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> messageService.getMessagesByConversation(conversation.getId(), sender.getId(), PageRequest.of(0, 20)));
    }

    // ── searchMessages ────────────────────────────────────────────────────────

    @Test
    void searchMessages_whenNotParticipant_shouldThrowAccessDenied() {
        when(participantRepository.existsById(new ParticipantId(conversation.getId(), sender.getId()))).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> messageService.searchMessages("hello", conversation.getId(), sender.getId(), PageRequest.of(0, 10)));
    }

    @Test
    void searchMessages_shouldReturnMatchingMessages() {
        Message msg = Message.builder().id(1L).sender(sender).conversation(conversation)
                .content("hello world").isDeleted(false).isEdited(false).build();
        Page<Message> page = new PageImpl<>(List.of(msg));

        when(participantRepository.existsById(new ParticipantId(conversation.getId(), sender.getId()))).thenReturn(true);
        when(messageRepository.findByConversationIdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(
                eq(conversation.getId()), eq("hello"), any())).thenReturn(page);

        Page<MessageResponseDTO> result = messageService.searchMessages(
                "hello", conversation.getId(), sender.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("hello world");
    }
}
