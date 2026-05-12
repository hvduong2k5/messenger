package com.team12345.messenger.service;

import com.team12345.messenger.dto.request.InitiateCallRequestDTO;
import com.team12345.messenger.dto.request.SignalingRequestDTO;
import com.team12345.messenger.dto.response.CallResponseDTO;
import com.team12345.messenger.dto.response.SignalingResponseDTO;

import java.util.List;

public interface CallService {

    /**
     * Initiates a new call between caller and receiver
     * @param callerId ID of the user initiating the call
     * @param request Call initiation request containing receiver ID and call type
     * @return Call response with call details
     */
    CallResponseDTO initiateCall(Long callerId, InitiateCallRequestDTO request);

    /**
     * Answers an incoming call
     * @param callId ID of the call to answer
     * @param userId ID of the user answering the call
     * @return Updated call response
     */
    CallResponseDTO answerCall(Long callId, Long userId);

    /**
     * Ends an active call
     * @param callId ID of the call to end
     * @param userId ID of the user ending the call
     * @return Updated call response
     */
    CallResponseDTO endCall(Long callId, Long userId);

    /**
     * Rejects an incoming call
     * @param callId ID of the call to reject
     * @param userId ID of the user rejecting the call
     * @return Updated call response
     */
    CallResponseDTO rejectCall(Long callId, Long userId);

    /**
     * Saves WebRTC signaling data
     * @param userId ID of the user sending the signal
     * @param request Signaling request containing call ID, signal type, and data
     * @return Signaling response
     */
    SignalingResponseDTO saveSignaling(Long userId, SignalingRequestDTO request);

    /**
     * Retrieves signaling data for a call
     * @param callId ID of the call
     * @param userId ID of the user requesting signaling data
     * @return List of signaling responses
     */
    List<SignalingResponseDTO> getSignaling(Long callId, Long userId);

    /**
     * Gets call history for a user
     * @param userId ID of the user
     * @return List of call responses representing call history
     */
    List<CallResponseDTO> getCallHistory(Long userId);
}
