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

    public final com.midterm.team12345.utils.SingleLiveEvent<Resource<Void>> leaveGroupState = new com.midterm.team12345.utils.SingleLiveEvent<>();
    public final com.midterm.team12345.utils.SingleLiveEvent<Resource<Void>> updateRoleState = new com.midterm.team12345.utils.SingleLiveEvent<>();

    private int currentPage = 0;
    private final int pageSize = 20;
    private boolean isLastPage = false;
    private String currentKeyword = "";
    private Long currentConversationId;

    public GroupInfoViewModel(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public void init(Long conversationId) {
        this.currentConversationId = conversationId;
    }

    public void fetchGroupDetails(Long conversationId) {
        init(conversationId);
        fetchGroupInfo(conversationId);
        fetchGroupMembers(conversationId, true);
    }

    private void fetchGroupInfo(Long conversationId) {
        _groupInfo.setValue(Resource.loading(null));
        conversationRepository.getConversationDetails(conversationId).observeForever(resource -> {
            if (resource.status != Resource.Status.LOADING) {
                _groupInfo.setValue(resource);
            }
        });
    }

    public void searchMembers(String keyword) {
        this.currentKeyword = keyword;
        fetchGroupMembers(currentConversationId, true);
    }

    public void fetchGroupMembers(Long conversationId, boolean isRefresh) {
        if (isRefresh) {
            currentPage = 0;
            isLastPage = false;
            _members.setValue(Resource.success(new ArrayList<>()));
        }
        if (isLastPage) return;

        List<ParticipantResponseDTO> currentList = _members.getValue() != null && _members.getValue().data != null ? _members.getValue().data : new ArrayList<>();
        _members.setValue(Resource.loading(currentList));

        conversationRepository.getParticipants(conversationId, currentKeyword, currentPage, pageSize).observeForever(resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                List<ParticipantResponseDTO> newItems = resource.data.getContent();
                if (newItems.size() < pageSize) {
                    isLastPage = true;
                }
                List<ParticipantResponseDTO> updatedList = new ArrayList<>(currentList);
                updatedList.addAll(newItems);
                _members.setValue(Resource.success(updatedList));
                currentPage++;
            } else if (resource.status == Resource.Status.ERROR) {
                _members.setValue(Resource.error(resource.message, currentList));
            }
        });
    }

    public void leaveGroup(Long conversationId) {
        leaveGroupState.setValue(Resource.loading(null));
        conversationRepository.leaveConversation(conversationId).observeForever(resource -> {
            if (resource.status != Resource.Status.LOADING) {
                leaveGroupState.setValue(resource);
            }
        });
    }

    public void updateParticipantRole(Long conversationId, Long participantId, String newRole) {
        updateRoleState.setValue(Resource.loading(null));
        conversationRepository.updateParticipantRole(conversationId, participantId, newRole).observeForever(resource -> {
            if (resource.status != Resource.Status.LOADING) {
                updateRoleState.setValue(resource);
                if (resource.status == Resource.Status.SUCCESS) {
                    List<ParticipantResponseDTO> current = _members.getValue() != null ? _members.getValue().data : null;
                    if (current != null) {
                        List<ParticipantResponseDTO> updated = new ArrayList<>(current);
                        for (int i = 0; i < updated.size(); i++) {
                            if (updated.get(i).getUserId().equals(participantId)) {
                                ParticipantResponseDTO old = updated.get(i);
                                ParticipantResponseDTO p = new ParticipantResponseDTO();
                                p.setUserId(old.getUserId());
                                p.setUsername(old.getUsername());
                                p.setAvatarUrl(old.getAvatarUrl());
                                p.setJoinedAt(old.getJoinedAt());
                                p.setRole(newRole);
                                updated.set(i, p);
                                break;
                            }
                        }
                        _members.setValue(Resource.success(updated));
                    }
                }
            }
        });
    }

    public LiveData<Resource<Void>> removeMember(Long conversationId, Long userId) {
        return conversationRepository.removeParticipant(conversationId, userId);
    }
}
