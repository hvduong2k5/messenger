package com.team12345.messenger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12345.messenger.dto.response.AttachmentResponseDTO;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.security.JwtUtils;
import com.team12345.messenger.service.AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttachmentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttachmentService attachmentService;

    @MockBean
    private JwtUtils jwtUtils;

    private AttachmentResponseDTO mockResponse;

    @BeforeEach
    void setUp() {
        // Mock Security Context
        User user = User.builder().id(1L).username("testuser").build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockResponse = AttachmentResponseDTO.builder()
                .id(1L)
                .url("http://example.com/file.jpg")
                .type("IMAGE")
                .fileSize(1024)
                .publicId("pid123")
                .build();
    }

    @Test
    void uploadFile_ShouldReturnAttachmentInfo() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        when(attachmentService.uploadAttachment(any(), eq(1L))).thenReturn(mockResponse);

        mockMvc.perform(multipart("/attachments/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.url").value("http://example.com/file.jpg"));

        verify(attachmentService).uploadAttachment(any(), eq(1L));
    }

    @Test
    void getAttachment_ShouldReturnInfo() throws Exception {
        when(attachmentService.getAttachmentById(1L)).thenReturn(mockResponse);

        mockMvc.perform(get("/attachments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(attachmentService).getAttachmentById(1L);
    }

    @Test
    void getAttachmentsByMessage_ShouldReturnList() throws Exception {
        when(attachmentService.getAttachmentsByMessageId(10L)).thenReturn(Collections.singletonList(mockResponse));

        mockMvc.perform(get("/attachments/message/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(attachmentService).getAttachmentsByMessageId(10L);
    }

    @Test
    void deleteAttachment_ShouldReturnNoContent() throws Exception {
        doNothing().when(attachmentService).deleteAttachment(1L, 1L);

        mockMvc.perform(delete("/attachments/1"))
                .andExpect(status().isNoContent());

        verify(attachmentService).deleteAttachment(1L, 1L);
    }
}
