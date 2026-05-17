package com.team12345.messenger.controller;

import com.team12345.messenger.dto.response.FriendRequestResponseDTO;
import com.team12345.messenger.dto.response.UserResponseDTO;
import com.team12345.messenger.entity.User;
import com.team12345.messenger.repository.BlacklistedTokenRepository;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.security.CustomUserDetails;
import com.team12345.messenger.security.JwtUtils;
import com.team12345.messenger.service.FriendshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FriendController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for pure controller testing
class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FriendshipService friendshipService;
    
    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BlacklistedTokenRepository blacklistedTokenRepository;

    private Long currentUserId = 1L;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(currentUserId).username("testuser").build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void sendFriendRequest_ShouldReturn201AndDto() throws Exception {
        Long receiverId = 2L;
        FriendRequestResponseDTO responseDTO = FriendRequestResponseDTO.builder()
                .senderId(currentUserId)
                .receiverId(receiverId)
                .status("PENDING")
                .build();

        when(friendshipService.sendFriendRequest(currentUserId, receiverId)).thenReturn(responseDTO);

        mockMvc.perform(post("/friends/request/{receiverId}", receiverId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senderId").value(currentUserId))
                .andExpect(jsonPath("$.receiverId").value(receiverId))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(friendshipService, times(1)).sendFriendRequest(currentUserId, receiverId);
    }

    @Test
    void acceptFriendRequest_ShouldReturn200() throws Exception {
        Long senderId = 2L;

        mockMvc.perform(put("/friends/request/{senderId}/accept", senderId))
                .andExpect(status().isOk())
                .andExpect(content().string("Friend request accepted successfully"));

        verify(friendshipService, times(1)).acceptFriendRequest(currentUserId, senderId);
    }

    @Test
    void declineFriendRequest_ShouldReturn204() throws Exception {
        Long senderId = 2L;

        mockMvc.perform(put("/friends/request/{senderId}/decline", senderId))
                .andExpect(status().isNoContent());

        verify(friendshipService, times(1)).declineFriendRequest(currentUserId, senderId);
    }

    @Test
    void unfriend_ShouldReturn200() throws Exception {
        Long friendId = 2L;

        mockMvc.perform(delete("/friends/{friendId}", friendId))
                .andExpect(status().isOk())
                .andExpect(content().string("Unfriended successfully"));

        verify(friendshipService, times(1)).unfriend(currentUserId, friendId);
    }

    @Test
    void getFriendsList_ShouldReturn200AndList() throws Exception {
        List<UserResponseDTO> friends = Arrays.asList(
                UserResponseDTO.builder().id(2L).username("friend1").build(),
                UserResponseDTO.builder().id(3L).username("friend2").build()
        );

        when(friendshipService.getFriendsList(currentUserId)).thenReturn(friends);

        mockMvc.perform(get("/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[1].id").value(3L));

        verify(friendshipService, times(1)).getFriendsList(currentUserId);
    }

    @Test
    void getPendingRequests_ShouldReturn200AndList() throws Exception {
        List<FriendRequestResponseDTO> requests = Arrays.asList(
                FriendRequestResponseDTO.builder().senderId(2L).receiverId(currentUserId).build()
        );

        when(friendshipService.getPendingRequests(currentUserId)).thenReturn(requests);

        mockMvc.perform(get("/friends/requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderId").value(2L));

        verify(friendshipService, times(1)).getPendingRequests(currentUserId);
    }

    @Test
    void cancelFriendRequest_ShouldReturn200() throws Exception {
        Long receiverId = 2L;

        mockMvc.perform(delete("/friends/request/{receiverId}/cancel", receiverId))
                .andExpect(status().isOk())
                .andExpect(content().string("Friend request canceled successfully"));

        verify(friendshipService, times(1)).cancelFriendRequest(currentUserId, receiverId);
    }
}