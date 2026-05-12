package com.team12345.messenger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12345.messenger.dto.response.MessageStatusResponseDTO;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.security.JwtUtils;
import com.team12345.messenger.service.MessageStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageStatusController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MessageStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageStatusService messageStatusService;

    @MockBean
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        // Mock Security Context
        User user = User.builder().id(1L).username("testuser").build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void markConversationAsRead_ShouldReturnNoContent() throws Exception {
        doNothing().when(messageStatusService).markConversationAsRead(1L, 1L);

        mockMvc.perform(put("/message-statuses/conversations/1/read"))
                .andExpect(status().isNoContent());

        verify(messageStatusService, times(1)).markConversationAsRead(1L, 1L);
    }

    @Test
    void updateMessageStatus_ShouldReturnNoContent() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "READ");

        doNothing().when(messageStatusService).updateMessageStatus(eq(1L), eq(1L), anyString());

        mockMvc.perform(patch("/message-statuses/messages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNoContent());

        verify(messageStatusService, times(1)).updateMessageStatus(1L, 1L, "READ");
    }

    @Test
    void updateMessageStatus_WhenStatusMissing_ShouldReturnBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();

        mockMvc.perform(patch("/message-statuses/messages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest()); // Handled by Spring or controller logic throwing IllegalArgumentException

        verify(messageStatusService, never()).updateMessageStatus(anyLong(), anyLong(), anyString());
    }

    @Test
    void getMessageStatuses_ShouldReturnList() throws Exception {
        MessageStatusResponseDTO responseDTO = MessageStatusResponseDTO.builder()
                .userId(2L)
                .username("otheruser")
                .status("READ")
                .updatedAt(LocalDateTime.now())
                .build();

        when(messageStatusService.getMessageStatuses(1L)).thenReturn(Collections.singletonList(responseDTO));

        mockMvc.perform(get("/message-statuses/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(2L))
                .andExpect(jsonPath("$[0].username").value("otheruser"))
                .andExpect(jsonPath("$[0].status").value("READ"));

        verify(messageStatusService, times(1)).getMessageStatuses(1L);
    }
}
