package com.team12345.messenger.controller;

import com.team12345.messenger.dto.response.NotificationResponseDTO;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
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

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit testing
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.team12345.messenger.security.JwtUtils jwtUtils;

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
    void getNotifications_ShouldReturnSlice() throws Exception {
        NotificationResponseDTO dto = NotificationResponseDTO.builder()
                .id(1L)
                .type("message")
                .content("You have a new message")
                .isSeen(false)
                .build();
        Slice<NotificationResponseDTO> slice = new SliceImpl<>(List.of(dto));

        when(notificationService.getUserNotifications(eq(1L), any(PageRequest.class))).thenReturn(slice);

        mockMvc.perform(get("/notifications")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].content").value("You have a new message"));
    }

    @Test
    void markAsRead_ShouldReturnNoContent() throws Exception {
        doNothing().when(notificationService).markAsRead(1L, 1L);

        mockMvc.perform(patch("/notifications/1/read"))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).markAsRead(1L, 1L);
    }

    @Test
    void markAsRead_WhenNotAuthorized_ShouldReturnForbidden() throws Exception {
        doThrow(new RuntimeException("Not authorized to modify this notification"))
                .when(notificationService).markAsRead(1L, 1L);

        mockMvc.perform(patch("/notifications/1/read"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void markAllAsRead_ShouldReturnOk() throws Exception {
        when(notificationService.markAllAsSeen(1L)).thenReturn(5);

        mockMvc.perform(put("/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All notifications marked as read"));

        verify(notificationService, times(1)).markAllAsSeen(1L);
    }

    @Test
    void deleteNotification_ShouldReturnNoContent() throws Exception {
        doNothing().when(notificationService).deleteNotification(1L, 1L);

        mockMvc.perform(delete("/notifications/1"))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).deleteNotification(1L, 1L);
    }

    @Test
    void deleteNotification_WhenNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new RuntimeException("Notification not found"))
                .when(notificationService).deleteNotification(1L, 1L);

        mockMvc.perform(delete("/notifications/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}
