package com.midterm.team12345.ui.creategroup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.midterm.team12345.data.remote.dto.request.ConversationRequestDTO;
import com.midterm.team12345.data.remote.dto.response.ConversationResponse;
import com.midterm.team12345.data.remote.dto.response.UserDTO;
import com.midterm.team12345.domain.repository.ChatRepository;
import com.midterm.team12345.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupViewModel extends ViewModel {

    private final ChatRepository chatRepository;

    private final MutableLiveData<List<UserDTO>> _friends = new MutableLiveData<>();
    public LiveData<Resource<List<UserDTO>>> getFriends() {
        return chatRepository.getFriends();
    }

    private final MutableLiveData<List<UserDTO>> _selectedUsers = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<UserDTO>> getSelectedUsers() {
        return _selectedUsers;
    }

    private final MutableLiveData<Resource<ConversationResponse>> _createState = new MutableLiveData<>();
    public LiveData<Resource<ConversationResponse>> getCreateState() {
        return _createState;
    }

    public CreateGroupViewModel(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public void toggleUserSelection(UserDTO user) {
        List<UserDTO> currentSelected = new ArrayList<>(_selectedUsers.getValue());
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

    public void createGroup(String groupName) {
        if (groupName.trim().isEmpty()) {
            _createState.setValue(Resource.error("Please enter group name", null));
            return;
        }

        List<UserDTO> selected = _selectedUsers.getValue();
        if (selected == null || selected.size() < 2) {
            _createState.setValue(Resource.error("Select at least 2 members", null));
            return;
        }

        List<Long> ids = new ArrayList<>();
        for (UserDTO u : selected) {
            ids.add(u.getId());
        }

        ConversationRequestDTO request = new ConversationRequestDTO(groupName, true, ids);
        chatRepository.createConversation(request).observeForever(resource -> {
            _createState.setValue(resource);
        });
    }
}
