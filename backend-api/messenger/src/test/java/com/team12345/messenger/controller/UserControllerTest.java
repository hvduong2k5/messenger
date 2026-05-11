package com.team12345.messenger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12345.messenger.dto.request.UpdateProfileRequestDTO;
import com.team12345.messenger.dto.response.UserProfileResponseDTO;
import com.team12345.messenger.dto.response.UserResponseDTO;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.security.JwtUtils;
import com.team12345.messenger.service.MediaService;
import com.team12345.messenger.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for pure controller testing
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private MediaService mediaService;
    
    @MockBean
    private JwtUtils jwtUtils;

    private UserProfileResponseDTO mockProfileResponse;
    private UserResponseDTO mockUserResponse;

    @BeforeEach
    void setUp() {
        // Mock Security Context
        User user = User.builder().id(1L).username("testuser").build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockProfileResponse = UserProfileResponseDTO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .avatarUrl("http://example.com/avatar.jpg")
                .status("Active")
                .build();

        mockUserResponse = UserResponseDTO.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .build();
    }

    @Test
    void getMyProfile_ShouldReturnProfile() throws Exception {
        when(userService.getProfile(1L)).thenReturn(mockProfileResponse);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).getProfile(1L);
    }

    @Test
    void getUserProfile_ShouldReturnProfile() throws Exception {
        when(userService.getProfile(2L)).thenReturn(mockProfileResponse);

        mockMvc.perform(get("/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService, times(1)).getProfile(2L);
    }

    @Test
    void updateProfile_ShouldReturnUpdatedProfile() throws Exception {
        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO("Away");
        UserProfileResponseDTO updatedProfile = UserProfileResponseDTO.builder()
                .id(1L)
                .username("testuser")
                .status("Away")
                .build();

        when(userService.updateStatus(eq(1L), anyString())).thenReturn(updatedProfile);

        mockMvc.perform(put("/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Away"));

        verify(userService, times(1)).updateStatus(1L, "Away");
    }

    @Test
    void updateAvatar_ShouldReturnUpdatedProfile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "image-data".getBytes());
        
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "http://new-avatar-url.com");
        when(mediaService.uploadFile(any())).thenReturn(uploadResult);

        UserProfileResponseDTO updatedProfile = UserProfileResponseDTO.builder()
                .id(1L)
                .avatarUrl("http://new-avatar-url.com")
                .build();
        when(userService.updateAvatar(eq(1L), anyString())).thenReturn(updatedProfile);

        mockMvc.perform(multipart("/users/avatar")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PATCH"); // MockMvc multipart() defaults to POST
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value("http://new-avatar-url.com"));

        verify(mediaService, times(1)).uploadFile(any());
        verify(userService, times(1)).updateAvatar(1L, "http://new-avatar-url.com");
    }

    @Test
    void searchUsers_ShouldReturnUserList() throws Exception {
        List<UserResponseDTO> users = Arrays.asList(mockUserResponse);
        when(userService.searchUsers(anyString(), anyLong())).thenReturn(users);

        mockMvc.perform(get("/users/search")
                        .param("q", "other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].username").value("otheruser"));

        verify(userService, times(1)).searchUsers("other", 1L);
    }
}