package com.team12345.messenger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12345.messenger.dto.request.MessageRequestDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.MessageService;
import com.team12345.messenger.dto.response.SaveMessageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.team12345.messenger.security.JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .build();
        userDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getMessages_ShouldReturnPage() throws Exception {
        MessageResponseDTO dto = MessageResponseDTO.builder()
                .messageId(1L)
                .content("Hello")
                .build();
        Page<MessageResponseDTO> page = new PageImpl<>(List.of(dto));

        when(messageService.getMessagesByConversation(eq(1L), eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/conversations/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("Hello"));
    }

    @Test
    void sendMessage_ShouldReturnCreated() throws Exception {
        MessageResponseDTO responseDTO = MessageResponseDTO.builder()
                .messageId(1L)
                .content("Test message")
                .build();
        SaveMessageResult saveResult = new SaveMessageResult(responseDTO, List.of());

        when(messageService.saveMessage(any(MessageRequestDTO.class))).thenReturn(saveResult);

        mockMvc.perform(post("/messages")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("conversationId", "1")
                .param("content", "Test message")
                .param("type", "TEXT"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Test message"));
    }

    @Test
    void revokeMessage_ShouldReturnNoContent() throws Exception {
        doNothing().when(messageService).revokeMessage(1L, 1L);

        mockMvc.perform(delete("/messages/1"))
                .andExpect(status().isNoContent());

        verify(messageService, times(1)).revokeMessage(1L, 1L);
    }

    @Test
    void editMessage_ShouldReturnUpdatedMessage() throws Exception {
        MessageRequestDTO requestDTO = new MessageRequestDTO();
        requestDTO.setContent("Updated content");
        requestDTO.setConversationId(1L); // Bổ sung để qua validation

        MessageResponseDTO responseDTO = MessageResponseDTO.builder()
                .messageId(1L)
                .content("Updated content")
                .isEdited(true)
                .build();

        when(messageService.editMessage(1L, 1L, "Updated content")).thenReturn(responseDTO);

        mockMvc.perform(put("/messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated content"))
                .andExpect(jsonPath("$.isEdited").value(true));
    }

    @Test
    void searchMessages_ShouldReturnPage() throws Exception {
        MessageResponseDTO dto = MessageResponseDTO.builder()
                .messageId(1L)
                .content("Keyword matched")
                .build();
        Page<MessageResponseDTO> page = new PageImpl<>(List.of(dto));

        when(messageService.searchMessages(eq("Keyword"), eq(1L), eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/messages/search")
                .param("keyword", "Keyword")
                .param("conversationId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("Keyword matched"));
    }
}
