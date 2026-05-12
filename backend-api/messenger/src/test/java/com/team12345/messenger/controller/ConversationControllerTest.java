package com.team12345.messenger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12345.messenger.dto.request.ConversationRequestDTO;
import com.team12345.messenger.dto.request.ConversationUpdateDTO;
import com.team12345.messenger.dto.response.ConversationResponseDTO;
import com.team12345.messenger.entity.Conversation;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConversationController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple unit test
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConversationService conversationService;

    @MockBean
    private com.team12345.messenger.security.JwtUtils jwtUtils;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        com.team12345.messenger.entity.User user = com.team12345.messenger.entity.User.builder()
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
    void getConversations_ShouldReturnPage() throws Exception {
        ConversationResponseDTO dto = ConversationResponseDTO.builder()
                .id(1L)
                .name("Test Group")
                .isGroup(true)
                .build();
        Page<ConversationResponseDTO> page = new PageImpl<>(List.of(dto));

        when(conversationService.getUserConversations(eq(1L), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/conversations")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Test Group"));
    }

    @Test
    void getConversationDetails_WhenParticipant_ShouldReturnDTO() throws Exception {
        ConversationResponseDTO dto = ConversationResponseDTO.builder()
                .id(1L)
                .name("Test Group")
                .isGroup(true)
                .build();

        when(conversationService.getConversationDetails(1L, 1L)).thenReturn(dto);

        mockMvc.perform(get("/conversations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getConversationDetails_WhenNotParticipant_ShouldReturnForbidden() throws Exception {
        when(conversationService.getConversationDetails(1L, 1L))
                .thenThrow(new RuntimeException("User is not a participant of this conversation"));

        mockMvc.perform(get("/conversations/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createConversation_ShouldReturnCreated() throws Exception {
        ConversationRequestDTO requestDTO = ConversationRequestDTO.builder()
                .name("New Group")
                .isGroup(true)
                .participantIds(List.of(1L, 2L))
                .build();

        Conversation conversation = Conversation.builder()
                .id(1L)
                .name("New Group")
                .isGroup(true)
                .build();

        when(conversationService.createConversation(1L, "New Group", true, requestDTO.getParticipantIds()))
                .thenReturn(conversation);

        mockMvc.perform(post("/conversations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void addParticipant_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/conversations/1/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("userId", 2L))))
                .andExpect(status().isOk());

        verify(conversationService, times(1)).addParticipant(1L, 1L, 2L);
    }

    @Test
    void removeParticipant_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/conversations/1/participants/2"))
                .andExpect(status().isNoContent());

        verify(conversationService, times(1)).removeParticipant(1L, 1L, 2L);
    }

    @Test
    void updateConversation_ShouldReturnOk() throws Exception {
        ConversationUpdateDTO updateDTO = ConversationUpdateDTO.builder()
                .name("Updated Group")
                .avatarUrl("http://example.com/avatar.jpg")
                .build();

        Conversation conversation = Conversation.builder()
                .id(1L)
                .name("Updated Group")
                .build();

        when(conversationService.updateConversation(1L, 1L, "Updated Group", "http://example.com/avatar.jpg"))
                .thenReturn(conversation);

        mockMvc.perform(put("/conversations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Group"));
    }
}
