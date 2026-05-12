package com.team12345.messenger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12345.messenger.dto.request.InitiateCallRequestDTO;
import com.team12345.messenger.dto.request.SignalingRequestDTO;
import com.team12345.messenger.dto.response.CallResponseDTO;
import com.team12345.messenger.dto.response.SignalingResponseDTO;
import com.team12345.messenger.entity.CallStatus;
import com.team12345.messenger.entity.CallType;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.CallService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CallController.class)
@AutoConfigureMockMvc(addFilters = false)
class CallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CallService callService;

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
    void initiateCall_ShouldReturnCreated() throws Exception {
        InitiateCallRequestDTO request = new InitiateCallRequestDTO();
        request.setReceiverId(2L);
        request.setCallType(CallType.audio);

        CallResponseDTO response = CallResponseDTO.builder()
                .id(1L)
                .callerId(1L)
                .callerUsername("testuser")
                .receiverId(2L)
                .receiverUsername("receiver")
                .callType(CallType.audio)
                .status(CallStatus.ringing)
                .createdAt(LocalDateTime.now())
                .build();

        when(callService.initiateCall(eq(1L), any(InitiateCallRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/calls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.callerId").value(1))
                .andExpect(jsonPath("$.receiverId").value(2))
                .andExpect(jsonPath("$.callType").value("audio"))
                .andExpect(jsonPath("$.status").value("ringing"));
    }

    @Test
    void initiateCall_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        InitiateCallRequestDTO request = new InitiateCallRequestDTO();
        // Missing required fields

        mockMvc.perform(post("/calls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void answerCall_ShouldReturnOk() throws Exception {
        CallResponseDTO response = CallResponseDTO.builder()
                .id(1L)
                .callerId(1L)
                .receiverId(2L)
                .status(CallStatus.connected)
                .startedAt(LocalDateTime.now())
                .build();

        when(callService.answerCall(1L, 1L)).thenReturn(response);

        mockMvc.perform(put("/calls/1/answer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("connected"))
                .andExpect(jsonPath("$.startedAt").exists());
    }

    @Test
    void rejectCall_ShouldReturnOk() throws Exception {
        CallResponseDTO response = CallResponseDTO.builder()
                .id(1L)
                .callerId(1L)
                .receiverId(2L)
                .status(CallStatus.rejected)
                .endedAt(LocalDateTime.now())
                .build();

        when(callService.rejectCall(1L, 1L)).thenReturn(response);

        mockMvc.perform(put("/calls/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("rejected"))
                .andExpect(jsonPath("$.endedAt").exists());
    }

    @Test
    void endCall_ShouldReturnOk() throws Exception {
        CallResponseDTO response = CallResponseDTO.builder()
                .id(1L)
                .callerId(1L)
                .receiverId(2L)
                .status(CallStatus.ended)
                .endedAt(LocalDateTime.now())
                .build();

        when(callService.endCall(1L, 1L)).thenReturn(response);

        mockMvc.perform(put("/calls/1/end"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ended"))
                .andExpect(jsonPath("$.endedAt").exists());
    }

    @Test
    void sendSignaling_ShouldReturnCreated() throws Exception {
        SignalingRequestDTO request = new SignalingRequestDTO();
        request.setSignalType("offer");
        request.setData("{\"sdp\":\"test-offer\"}");

        SignalingResponseDTO response = SignalingResponseDTO.builder()
                .id(1L)
                .senderId(1L)
                .senderUsername("testuser")
                .signalType("offer")
                .data("{\"sdp\":\"test-offer\"}")
                .createdAt(LocalDateTime.now())
                .build();

        when(callService.saveSignaling(eq(1L), any(SignalingRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/calls/1/signaling")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.senderId").value(1))
                .andExpect(jsonPath("$.signalType").value("offer"))
                .andExpect(jsonPath("$.data").value("{\"sdp\":\"test-offer\"}"));
    }

    @Test
    void sendSignaling_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        SignalingRequestDTO request = new SignalingRequestDTO();
        // Missing required fields

        mockMvc.perform(post("/calls/1/signaling")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSignaling_ShouldReturnList() throws Exception {
        SignalingResponseDTO signaling1 = SignalingResponseDTO.builder()
                .id(1L)
                .senderId(1L)
                .signalType("offer")
                .data("{\"sdp\":\"offer-data\"}")
                .createdAt(LocalDateTime.now())
                .build();

        SignalingResponseDTO signaling2 = SignalingResponseDTO.builder()
                .id(2L)
                .senderId(2L)
                .signalType("answer")
                .data("{\"sdp\":\"answer-data\"}")
                .createdAt(LocalDateTime.now())
                .build();

        List<SignalingResponseDTO> response = List.of(signaling1, signaling2);

        when(callService.getSignaling(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/calls/1/signaling"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].signalType").value("offer"))
                .andExpect(jsonPath("$[1].signalType").value("answer"));
    }

    @Test
    void getCallHistory_ShouldReturnList() throws Exception {
        CallResponseDTO call1 = CallResponseDTO.builder()
                .id(1L)
                .callerId(1L)
                .receiverId(2L)
                .callType(CallType.audio)
                .status(CallStatus.ended)
                .createdAt(LocalDateTime.now())
                .build();

        CallResponseDTO call2 = CallResponseDTO.builder()
                .id(2L)
                .callerId(2L)
                .receiverId(1L)
                .callType(CallType.video)
                .status(CallStatus.missed)
                .createdAt(LocalDateTime.now())
                .build();

        List<CallResponseDTO> response = List.of(call1, call2);

        when(callService.getCallHistory(1L)).thenReturn(response);

        mockMvc.perform(get("/calls/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].callType").value("audio"))
                .andExpect(jsonPath("$[0].status").value("ended"))
                .andExpect(jsonPath("$[1].callType").value("video"))
                .andExpect(jsonPath("$[1].status").value("missed"));
    }
}
