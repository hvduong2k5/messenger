package com.midterm.team12345.ui.groupinfo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO;
import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class GroupInfoViewModel extends BaseViewModel {

    private final ConversationRepository conversationRepository;

    private final MutableLiveData<Resource<ConversationResponseDTO>> _groupInfo = new MutableLiveData<>();
    public final LiveData<Resource<ConversationResponseDTO>> groupInfo = _groupInfo;

    private final MutableLiveData<Resource<List<ParticipantResponseDTO>>> _members = new MutableLiveData<>();
    public final LiveData<Resource<List<ParticipantResponseDTO>>> members = _members;

    private final MutableLiveData<Resource<List<ParticipantResponseDTO>>> _bannedMembers = new MutableLiveData<>();
    public final LiveData<Resource<List<ParticipantResponseDTO>>> bannedMembers = _bannedMembers;

    private final MutableLiveData<Boolean> _isAdmin = new MutableLiveData<>(false);
    public final LiveData<Boolean> isAdmin = _isAdmin;

    public GroupInfoViewModel(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public void fetchGroupDetails(Long conversationId) {
        fetchGroupInfo(conversationId);
        fetchGroupMembers(conversationId);
    }

    private void fetchGroupInfo(Long conversationId) {
        // Mocking group info - In production, use conversationRepository.getConversationDetails(conversationId)
        ConversationResponseDTO mockInfo = new ConversationResponseDTO();
        mockInfo.setId(conversationId);
        mockInfo.setName("Innovative Online Shopping");
        mockInfo.setIsGroup(true);
        _groupInfo.setValue(Resource.success(mockInfo));
    }

    public void fetchGroupMembers(Long conversationId) {
        _members.setValue(Resource.loading(null));
        // Mock data
        List<ParticipantResponseDTO> mockMembers = new ArrayList<>();
        mockMembers.add(createParticipant(1L, "Alex Mason", "OWNER"));
        mockMembers.add(createParticipant(2L, "Andrew Joseph", "ADMIN"));
        mockMembers.add(createParticipant(3L, "Avery Quinn", "MEMBER"));
        mockMembers.add(createParticipant(4L, "Brian Michael", "MEMBER"));
        mockMembers.add(createParticipant(5L, "Cameron Lee", "MEMBER"));
        _members.setValue(Resource.success(mockMembers));
    }

    public LiveData<Resource<Void>> removeMember(Long conversationId, Long userId) {
        return conversationRepository.removeParticipant(conversationId, userId);
    }

    public void fetchBannedMembers(Long conversationId) {
        _bannedMembers.setValue(Resource.loading(null));
        // Mock data
        List<ParticipantResponseDTO> mockBanned = new ArrayList<>();
        mockBanned.add(createParticipant(10L, "Linda Kay", "MEMBER"));
        mockBanned.add(createParticipant(11L, "Nancy Grace", "MEMBER"));
        _bannedMembers.setValue(Resource.success(mockBanned));
    }

    public void unbanMember(Long conversationId, Long userId) {
        // After success repo call, update list
        List<ParticipantResponseDTO> current = _bannedMembers.getValue() != null ? _bannedMembers.getValue().data : null;
        if (current != null) {
            List<ParticipantResponseDTO> updated = new ArrayList<>(current);
            updated.removeIf(m -> m.getUserId().equals(userId));
            _bannedMembers.setValue(Resource.success(updated));
        }
    }

    private ParticipantResponseDTO createParticipant(Long id, String name, String role) {
        ParticipantResponseDTO p = new ParticipantResponseDTO();
        p.setUserId(id);
        p.setUsername(name);
        p.setRole(role);
        return p;
    }
}
