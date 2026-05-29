package com.midterm.team12345.ui.groupinfo;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO;
import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.utils.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GroupInfoViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ConversationRepository conversationRepository;

    private GroupInfoViewModel viewModel;

    @Before
    public void setup() {
        viewModel = new GroupInfoViewModel(conversationRepository);
    }

    @Test
    public void testFetchGroupDetails_success() {
        Long conversationId = 1L;
        MutableLiveData<Resource<ConversationResponseDTO>> liveData = new MutableLiveData<>();
        ConversationResponseDTO mockInfo = new ConversationResponseDTO();
        mockInfo.setId(conversationId);
        mockInfo.setName("Test Group");
        liveData.setValue(Resource.success(mockInfo));

        MutableLiveData<Resource<PageResponse<ParticipantResponseDTO>>> membersLiveData = new MutableLiveData<>();
        PageResponse<ParticipantResponseDTO> pageResponse = new PageResponse<>();
        pageResponse.setContent(new ArrayList<>());
        membersLiveData.setValue(Resource.success(pageResponse));

        when(conversationRepository.getConversationDetails(conversationId)).thenReturn(liveData);
        when(conversationRepository.getParticipants(anyLong(), anyString(), anyInt(), anyInt())).thenReturn(membersLiveData);

        viewModel.fetchGroupDetails(conversationId);

        Resource<ConversationResponseDTO> result = viewModel.groupInfo.getValue();
        assertNotNull(result);
        assertEquals(Resource.Status.SUCCESS, result.status);
        assertEquals("Test Group", result.data.getName());
    }

    @Test
    public void testFetchGroupMembers_pagination_success() {
        Long conversationId = 1L;
        viewModel.init(conversationId);

        MutableLiveData<Resource<PageResponse<ParticipantResponseDTO>>> membersLiveData = new MutableLiveData<>();
        PageResponse<ParticipantResponseDTO> pageResponse = new PageResponse<>();
        List<ParticipantResponseDTO> participants = new ArrayList<>();
        ParticipantResponseDTO p1 = new ParticipantResponseDTO();
        p1.setUserId(1L);
        participants.add(p1);
        pageResponse.setContent(participants);
        membersLiveData.setValue(Resource.success(pageResponse));

        when(conversationRepository.getParticipants(conversationId, "", 0, 20)).thenReturn(membersLiveData);

        viewModel.fetchGroupMembers(conversationId, true);

        Resource<List<ParticipantResponseDTO>> result = viewModel.members.getValue();
        assertNotNull(result);
        assertEquals(Resource.Status.SUCCESS, result.status);
        assertEquals(1, result.data.size());
    }

    @Test
    public void testLeaveGroup_success() {
        Long conversationId = 1L;
        MutableLiveData<Resource<Void>> leaveLiveData = new MutableLiveData<>();
        leaveLiveData.setValue(Resource.success(null));

        when(conversationRepository.leaveConversation(conversationId)).thenReturn(leaveLiveData);

        viewModel.leaveGroup(conversationId);

        Resource<Void> result = viewModel.leaveGroupState.getValue();
        assertNotNull(result);
        assertEquals(Resource.Status.SUCCESS, result.status);
    }

    @Test
    public void testUpdateParticipantRole_success() {
        Long conversationId = 1L;
        Long participantId = 2L;
        String newRole = "ADMIN";

        viewModel.init(conversationId);
        MutableLiveData<Resource<PageResponse<ParticipantResponseDTO>>> membersLiveData = new MutableLiveData<>();
        PageResponse<ParticipantResponseDTO> pageResponse = new PageResponse<>();
        List<ParticipantResponseDTO> participants = new ArrayList<>();
        ParticipantResponseDTO p1 = new ParticipantResponseDTO();
        p1.setUserId(participantId);
        p1.setRole("MEMBER");
        participants.add(p1);
        pageResponse.setContent(participants);
        membersLiveData.setValue(Resource.success(pageResponse));

        when(conversationRepository.getParticipants(conversationId, "", 0, 20)).thenReturn(membersLiveData);
        viewModel.fetchGroupMembers(conversationId, true);

        MutableLiveData<Resource<Void>> updateLiveData = new MutableLiveData<>();
        updateLiveData.setValue(Resource.success(null));

        when(conversationRepository.updateParticipantRole(conversationId, participantId, newRole)).thenReturn(updateLiveData);

        viewModel.updateParticipantRole(conversationId, participantId, newRole);

        Resource<Void> updateResult = viewModel.updateRoleState.getValue();
        assertNotNull(updateResult);
        assertEquals(Resource.Status.SUCCESS, updateResult.status);

        Resource<List<ParticipantResponseDTO>> listResult = viewModel.members.getValue();
        assertNotNull(listResult);
        assertEquals("ADMIN", listResult.data.get(0).getRole());
    }
}
