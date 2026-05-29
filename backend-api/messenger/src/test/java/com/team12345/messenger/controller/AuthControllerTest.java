package com.team12345.messenger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12345.messenger.dto.request.ForgotPasswordRequestDTO;
import com.team12345.messenger.dto.request.ResetPasswordRequestDTO;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void forgotPassword_ShouldReturnOk() throws Exception {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail("test@example.com");

        doNothing().when(authService).forgotPassword(any(ForgotPasswordRequestDTO.class));

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_ShouldReturnOk() throws Exception {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newpassword123");

        doNothing().when(authService).resetPassword(any(ResetPasswordRequestDTO.class));

        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void logout_ShouldReturnOk() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        doNothing().when(authService).logout(1L, "");

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk());
    }

    @Test
    void logout_WithNullUserDetails_ShouldReturnOk() throws Exception {
        SecurityContextHolder.clearContext();

        doNothing().when(authService).logout(null, "");

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk());
    }
}
