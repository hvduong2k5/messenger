package com.midterm.team12345.ui.creategroup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.dto.request.ConversationRequestDTO;
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;
import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.domain.repository.FriendRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;

import com.midterm.team12345.data.local.entity.UserEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CreateGroupViewModel extends BaseViewModel {

    private final FriendRepository friendRepository;
    private final ConversationRepository conversationRepository;

    private final MutableLiveData<List<UserEntity>> _selectedUsers = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<UserEntity>> getSelectedUsers() {
        return _selectedUsers;
    }

    private final MutableLiveData<Resource<ConversationResponseDTO>> _createState = new MutableLiveData<>();
    public LiveData<Resource<ConversationResponseDTO>> getCreateState() {
        return _createState;
    }

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<UserEntity> allFriendsList = new ArrayList<>();
    
    private final MutableLiveData<List<UserEntity>> _friends = new MutableLiveData<>();
    public LiveData<List<UserEntity>> getFriends() {
        return _friends;
    }

    public CreateGroupViewModel(FriendRepository friendRepository, ConversationRepository conversationRepository) {
        this.friendRepository = friendRepository;
        this.conversationRepository = conversationRepository;
        
        LiveData<List<UserEntity>> allFriendsLiveData = friendRepository.searchFriendsLocally("");
        allFriendsLiveData.observeForever(users -> {
            if (users != null) {
                allFriendsList = users;
                filterFriends(searchQuery.getValue());
            }
        });
        
        // Trigger remote sync of friends list
        friendRepository.getFriends().observeForever(resource -> {});
        
        searchQuery.observeForever(this::filterFriends);
    }

    public void onSearchQueryChanged(String query) {
        searchQuery.setValue(query);
    }

    private void filterFriends(String query) {
        executor.execute(() -> {
            String q = query != null ? query.toLowerCase() : "";
            List<UserEntity> filtered = allFriendsList.stream()
                    .filter(user -> q.isEmpty() || user.getUsername().toLowerCase().contains(q))
                    .collect(Collectors.toList());
            _friends.postValue(filtered);
        });
    }

    /**
     * Chọn hoặc bỏ chọn một thành viên
     */
    public void toggleUserSelection(UserEntity user) {
        List<UserEntity> currentSelected = new ArrayList<>(_selectedUsers.getValue() != null ? _selectedUsers.getValue() : new ArrayList<>());
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
     /* Thực hiện tạo nhóm mới
     */
    public void createGroup(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            setError("Vui lòng nhập tên nhóm");
            return;
        }

        List<UserEntity> selected = _selectedUsers.getValue();
        if (selected == null || selected.size() < 2) {
            setError("Vui lòng chọn ít nhất 2 thành viên");
            return;
        }

        showLoading();
        List<Long> ids = new ArrayList<>();
        for (UserEntity u : selected) {
            ids.add(u.getId());
        }

        ConversationRequestDTO request = new ConversationRequestDTO(groupName, true, ids);
        // Note: ConversationRepository.createConversation returns Resource<Void> in the current interface,
        // but CreateGroupViewModel expects Resource<ConversationResponse>. 
        // We might need to update the interface or handle Resource<Void> and navigate back.
        // Let's check ConversationRepository.java again.
        
        conversationRepository.createConversation(request).observeForever(resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                _createState.setValue(Resource.success(resource.data));
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
