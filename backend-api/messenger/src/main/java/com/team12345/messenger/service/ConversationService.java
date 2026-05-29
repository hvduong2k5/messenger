package com.team12345.messenger.service;

import com.team12345.messenger.dto.response.ConversationResponseDTO;
import com.team12345.messenger.dto.response.MessageResponseDTO;
import com.team12345.messenger.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing conversations, participants, and retrieving chat history.
 */
public interface ConversationService {
    
    /**
     * Retrieves a paginated list of conversations for a specific user.
     * Includes details like last message content and unread count for each conversation.
     *
     * @param userId   the ID of the user whose conversations are being retrieved
     * @param pageable pagination and sorting information
     * @return a page of ConversationResponseDTO objects
     */
    Page<ConversationResponseDTO> getUserConversations(Long userId, Pageable pageable);

    /**
     * Creates a new conversation and adds initial participants.
     *
     * @param currentUserId  the ID of the user creating the conversation
     * @param name           the name of the conversation (optional for 1-to-1 chats)
     * @param isGroup        true if it's a group conversation, false for 1-to-1
     * @param participantIds list of user IDs to be added as participants
     * @return the created ConversationResponseDTO
     */
    ConversationResponseDTO createConversation(Long currentUserId, String name, boolean isGroup, List<Long> participantIds);

    /**
     * Adds a new participant to an existing conversation.
     *
     * @param conversationId the ID of the conversation
     * @param currentUserId  the ID of the user performing the action
     * @param userId         the ID of the user to be added
     * @throws RuntimeException if the conversation or user is not found, or if the user is already a participant
     */
    void addParticipant(Long conversationId, Long currentUserId, Long userId);

    /**
     * Removes a participant from a conversation.
     *
     * @param conversationId the ID of the conversation
     * @param currentUserId  the ID of the user performing the action
     * @param userId         the ID of the user to be removed
     * @throws RuntimeException if the participant record is not found
     */
    void removeParticipant(Long conversationId, Long currentUserId, Long userId);

    /**
     * Updates conversation info (e.g. name, avatarUrl).
     *
     * @param conversationId the ID of the conversation
     * @param currentUserId  the ID of the user performing the update
     * @param name           the new name
     * @param avatarUrl      the new avatar url
     * @return updated Conversation
     */
    Conversation updateConversation(Long conversationId, Long currentUserId, String name, String avatarUrl);

    /**
     * Retrieves detailed information about a single conversation for a specific user.
     *
     * @param conversationId the ID of the conversation
     * @param userId         the ID of the user (used to calculate unread count)
     * @return a ConversationResponseDTO containing details, last message, and unread count
     * @throws RuntimeException if the conversation is not found
     */
    ConversationResponseDTO getConversationDetails(Long conversationId, Long userId);

    /**
     * Retrieves a paginated list of messages for a specific conversation.
     * Messages are typically sorted by creation date in descending order.
     *
     * @param conversationId the ID of the conversation
     * @param userId         the ID of the current user
     * @param pageable       pagination and sorting information
     * @return a page of DetailMessageResponseDTO objects
     * @throws RuntimeException if the conversation is not found
     */
    Page<MessageResponseDTO> getConversationMessages(Long conversationId, Long userId, Pageable pageable);

    /**
     * Retrieves a paginated list of participants for a specific conversation with optional search.
     *
     * @param conversationId the ID of the conversation
     * @param currentUserId  the ID of the current user
     * @param keyword        optional search keyword
     * @param pageable       pagination information
     * @return a page of ParticipantResponseDTO objects
     */
    Page<com.team12345.messenger.dto.response.ParticipantResponseDTO> getParticipants(Long conversationId, Long currentUserId, String keyword, Pageable pageable);

    /**
     * Current user leaves the conversation.
     *
     * @param conversationId the ID of the conversation
     * @param currentUserId  the ID of the current user
     */
    void leaveConversation(Long conversationId, Long currentUserId);
    /**
     * Updates the role of a participant in a conversation.
     *
     * @param conversationId      the ID of the conversation
     * @param currentUserId       the ID of the current user
     * @param targetParticipantId the ID of the participant whose role will be updated
     * @param newRole             the new role (MEMBER, ADMIN)
     */
    void updateParticipantRole(Long conversationId, Long currentUserId, Long targetParticipantId, String newRole);
}