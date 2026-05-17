package com.midterm.team12345.ui.creategroup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.dto.request.ConversationRequestDTO;
import com.midterm.team12345.data.remote.dto.response.ConversationResponse;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;
import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.domain.repository.FriendRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupViewModel extends BaseViewModel {

    private final FriendRepository friendRepository;
    private final ConversationRepository conversationRepository;

    private final MutableLiveData<List<UserResponseDTO>> _selectedUsers = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<UserResponseDTO>> getSelectedUsers() {
        return _selectedUsers;
    }

    private final MutableLiveData<Resource<ConversationResponse>> _createState = new MutableLiveData<>();
    public LiveData<Resource<ConversationResponse>> getCreateState() {
        return _createState;
    }

    public CreateGroupViewModel(FriendRepository friendRepository, ConversationRepository conversationRepository) {
        this.friendRepository = friendRepository;
        this.conversationRepository = conversationRepository;
    }

    /**
     * Lấy danh sách bạn bè để mời vào nhóm
     */
    public LiveData<Resource<List<UserResponseDTO>>> getFriends() {
        return friendRepository.getFriendsList();
    }

    /**
     * Chọn hoặc bỏ chọn một thành viên
     */
    public void toggleUserSelection(UserResponseDTO user) {
        List<UserResponseDTO> currentSelected = new ArrayList<>(_selectedUsers.getValue() != null ? _selectedUsers.getValue() : new ArrayList<>());
        boolean exists = false;
        for (int i = 0; i < currentSelected.size(); i++) {
            if (currentSelected.get(i).getId().equals(user.getId())) {
                currentSelected.remove(i);
                exists = true;
                break;
            }
        }
        if (!exists) {
            currentSelected.add(user);
        }
        _selectedUsers.setValue(currentSelected);
    }

    /**
     * Thực hiện tạo nhóm mới
     */
    public void createGroup(String groupName) {
        if (groupName.trim().isEmpty()) {
            setError("Please enter group name");
            return;
        }

        List<UserResponseDTO> selected = _selectedUsers.getValue();
        if (selected == null || selected.size() < 2) {
            setError("Select at least 2 members");
            return;
        }

        showLoading();
        List<Long> ids = new ArrayList<>();
        for (UserResponseDTO u : selected) {
            ids.add(u.getId());
        }

        ConversationRequestDTO request = new ConversationRequestDTO(groupName, true, ids);
        // Note: ConversationRepository.createConversation returns Resource<Void> in the current interface,
        // but CreateGroupViewModel expects Resource<ConversationResponse>. 
        // We might need to update the interface or handle Resource<Void> and navigate back.
        // Let's check ConversationRepository.java again.
        
        conversationRepository.createConversation(request).observeForever(resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO dto = resource.data;
                ConversationResponse domain = new ConversationResponse(
                        dto.getId(),
                        dto.getName(),
                        dto.getLastMessageContent(),
                        dto.getAvatarUrl(),
                        System.currentTimeMillis(),
                        0,
                        false,
                        false,
                        dto.getIsGroup()
                );
                _createState.setValue(Resource.success(domain));
            } else if (resource.status == Resource.Status.ERROR) {
                _createState.setValue(Resource.error(resource.message, null));
            } else {
                _createState.setValue(Resource.loading(null));
            }

            if (resource.status != Resource.Status.LOADING) {
                hideLoading();
            }
            if (resource.status == Resource.Status.ERROR) {
                setError(resource.message);
            }
        });
    }
}
