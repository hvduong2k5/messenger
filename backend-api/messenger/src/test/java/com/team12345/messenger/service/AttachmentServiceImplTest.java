package com.team12345.messenger.service;

import com.team12345.messenger.dto.response.AttachmentResponseDTO;
import com.team12345.messenger.entity.Attachment;
import com.team12345.messenger.entity.Message;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.exception.ResourceNotFoundException;
import com.team12345.messenger.repository.AttachmentRepository;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.service.impl.AttachmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttachmentServiceImplTest {

    @Mock
    private MediaService mediaService;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    private User testUser;
    private Attachment testAttachment;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("testuser").build();
        testAttachment = Attachment.builder()
                .id(1L)
                .fileUrl("http://example.com/file.jpg")
                .fileType("IMAGE")
                .fileSize(1024)
                .publicId("public_id_123")
                .uploader(testUser)
                .build();
    }

    @Test
    void uploadAttachment_WithValidImage_ShouldSucceed() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "http://example.com/test.jpg");
        uploadResult.put("type", "IMAGE");
        uploadResult.put("size", 100);
        uploadResult.put("publicId", "pid123");

        when(mediaService.uploadFile(any(MultipartFile.class))).thenReturn(uploadResult);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(testAttachment);

        // Act
        AttachmentResponseDTO result = attachmentService.uploadAttachment(file, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(mediaService).uploadFile(any());
        verify(attachmentRepository).save(any());
    }

    @Test
    void uploadAttachment_WithInvalidType_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.exe", "application/x-msdownload", "content".getBytes());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attachmentService.uploadAttachment(file, 1L);
        });
        verify(mediaService, never()).uploadFile(any());
    }

    @Test
    void uploadAttachment_WithLargeImage_ShouldThrowException() {
        // Arrange
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile file = new MockMultipartFile("file", "large.jpg", "image/jpeg", largeContent);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            attachmentService.uploadAttachment(file, 1L);
        });
    }

    @Test
    void deleteAttachment_AsOwner_ShouldSucceed() {
        // Arrange
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(testAttachment));

        // Act
        attachmentService.deleteAttachment(1L, 1L);

        // Assert
        verify(mediaService).deleteFile("public_id_123");
        verify(attachmentRepository).delete(testAttachment);
    }

    @Test
    void deleteAttachment_AsNonOwner_ShouldThrowException() {
        // Arrange
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(testAttachment));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            attachmentService.deleteAttachment(1L, 2L);
        });
        verify(mediaService, never()).deleteFile(anyString());
        verify(attachmentRepository, never()).delete(any());
    }

    @Test
    void getAttachmentById_WhenExists_ShouldReturnDTO() {
        // Arrange
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(testAttachment));

        // Act
        AttachmentResponseDTO result = attachmentService.getAttachmentById(1L);

        // Assert
        assertThat(result.getUrl()).isEqualTo(testAttachment.getFileUrl());
    }

    @Test
    void getAttachmentsByMessageId_ShouldReturnList() {
        // Arrange
        when(attachmentRepository.findByMessageId(10L)).thenReturn(Collections.singletonList(testAttachment));

        // Act
        List<AttachmentResponseDTO> results = attachmentService.getAttachmentsByMessageId(10L);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(testAttachment.getId());
    }
}
