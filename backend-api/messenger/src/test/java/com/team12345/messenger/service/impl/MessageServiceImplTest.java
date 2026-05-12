package com.team12345.messenger.service.impl;

import com.team12345.messenger.dto.request.MessageRequestDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.entity.*;
import com.team12345.messenger.exception.ResourceNotFoundException;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private MessageStatusRepository messageStatusRepository;
    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MessageServiceImpl messageService;

    private User sender;
    private User receiver;
    private Conversation conversation;
    private MessageRequestDTO textMessageRequest;

    @BeforeEach
    void setUp() {
        sender = User.builder().id(1L).username("sender").avatarUrl("avatar.jpg").build();
        receiver = User.builder().id(2L).username("receiver").build();
        conversation = Conversation.builder().id(100L).build();

        textMessageRequest = MessageRequestDTO.builder()
                .senderId(sender.getId())
                .conversationId(conversation.getId())
                .content("Hello, World!")
                .clientMessageId("client-123")
                .build();
    }

    @Test
    void saveMessage_withTextOnly_shouldSaveMessageAndCreateStatuses() {
        // Arrange
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message msg = invocation.getArgument(0);
            msg.setId(1L); // Simulate saving and getting an ID
            msg.setCreatedAt(java.time.LocalDateTime.now());
            return msg;
        });

        Participant senderParticipant = Participant.builder().user(sender).build();
        Participant receiverParticipant = Participant.builder().user(receiver).build();
        when(participantRepository.findById_ConversationId(conversation.getId()))
                .thenReturn(Arrays.asList(senderParticipant, receiverParticipant));

        // Act
        var result = messageService.saveMessage(textMessageRequest);
        MessageResponseDTO response = result.message();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Hello, World!");
        assertThat(response.getSenderId()).isEqualTo(sender.getId());
        assertThat(response.getAttachments()).isEmpty();

        verify(messageRepository, times(1)).save(any(Message.class));
        verify(messageStatusRepository, times(1)).saveAll(anyList());
        verify(mediaService, never()).uploadFile(any());
    }

    @Test
    void saveMessage_withAttachments_shouldUploadFilesAndSave() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "some-image".getBytes());
        textMessageRequest.setFiles(List.of(file));

        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "http://cloudinary.com/test.jpg");
        uploadResult.put("type", "IMAGE");
        uploadResult.put("size", 12345);
        when(mediaService.uploadFile(any(MockMultipartFile.class))).thenReturn(uploadResult);

        Participant senderParticipant = Participant.builder().user(sender).build();
        Participant receiverParticipant = Participant.builder().user(receiver).build();
        when(participantRepository.findById_ConversationId(conversation.getId()))
                .thenReturn(Arrays.asList(senderParticipant, receiverParticipant));

        // Act
        var result = messageService.saveMessage(textMessageRequest);
        MessageResponseDTO response = result.message();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAttachments()).hasSize(1);
        assertThat(response.getAttachments().get(0).getUrl()).isEqualTo("http://cloudinary.com/test.jpg");
        assertThat(response.getAttachments().get(0).getType()).isEqualTo("IMAGE");

        verify(mediaService, times(1)).uploadFile(any(MockMultipartFile.class));
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void saveMessage_whenConversationNotFound_shouldThrowException() {
        // Arrange
        when(conversationRepository.findById(anyLong())).thenReturn(Optional.empty());
        // Đảm bảo user là participant đúng conversationId để test đúng lỗi không tìm thấy hội thoại
        when(participantRepository.findById_ConversationId(eq(textMessageRequest.getConversationId())))
            .thenReturn(List.of(Participant.builder().user(sender).build()));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> messageService.saveMessage(textMessageRequest));
    }

    @Test
    void getMessagesByConversation_shouldReturnPagedMessages() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Message message = Message.builder().id(1L).sender(sender).conversation(conversation).content("Hi").build();
        Page<Message> messagePage = new PageImpl<>(List.of(message), pageable, 1);

        when(conversationRepository.existsById(conversation.getId())).thenReturn(true);
        when(participantRepository.existsById(new ParticipantId(conversation.getId(), sender.getId()))).thenReturn(true);
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(conversation.getId(), pageable))
                .thenReturn(messagePage);

        // Act
        Page<MessageResponseDTO> resultPage = messageService.getMessagesByConversation(conversation.getId(), sender.getId(), pageable);

        // Assert
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().get(0).getContent()).isEqualTo("Hi");
        assertThat(resultPage.getContent().get(0).getSenderUsername()).isEqualTo("sender");
    }
}