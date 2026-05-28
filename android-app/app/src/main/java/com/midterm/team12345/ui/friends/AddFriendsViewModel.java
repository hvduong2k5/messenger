package com.midterm.team12345.ui.friends;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.dto.response.FriendshipStatus;
import com.midterm.team12345.data.remote.dto.response.UserSearchResponseDTO;
import com.midterm.team12345.domain.repository.FriendRepository;
import com.midterm.team12345.domain.repository.UserRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class AddFriendsViewModel extends BaseViewModel {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    private final MutableLiveData<Resource<List<UserSearchResponseDTO>>> _searchResults = new MutableLiveData<>();
    public final LiveData<Resource<List<UserSearchResponseDTO>>> searchResults = _searchResults;

    public AddFriendsViewModel(UserRepository userRepository, FriendRepository friendRepository) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
    }

    public void searchUsers(String keyword) {
        _searchResults.setValue(Resource.loading(null));
        userRepository.searchUsers(keyword, 0, 50).observeForever(resource -> {
            if (resource != null) {
                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    _searchResults.setValue(Resource.success(resource.data.getContent()));
                } else if (resource.status == Resource.Status.ERROR) {
                    _searchResults.setValue(Resource.error(resource.message, null));
                    setError(resource.message);
                }
            }
        });
    }

    public void loadSuggestions() {
        searchUsers(""); // Or you can call a specific suggestions API if available
    }

    public void sendFriendRequest(Long targetUserId) {
        friendRepository.sendFriendRequest(targetUserId).observeForever(resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                updateUserStatusLocally(targetUserId, FriendshipStatus.SENDER_PENDING);
            } else if (resource != null && resource.status == Resource.Status.ERROR) {
                setError(resource.message);
            }
        });
    }

    public void cancelFriendRequest(Long targetUserId) {
        friendRepository.cancelFriendRequest(targetUserId).observeForever(resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                updateUserStatusLocally(targetUserId, FriendshipStatus.STRANGER);
            } else if (resource != null && resource.status == Resource.Status.ERROR) {
                setError(resource.message);
            }
        });
    }

    public void unfriend(Long targetUserId) {
        friendRepository.unfriend(targetUserId).observeForever(resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                updateUserStatusLocally(targetUserId, FriendshipStatus.STRANGER);
            } else if (resource != null && resource.status == Resource.Status.ERROR) {
                setError(resource.message);
            }
        });
    }

    private void updateUserStatusLocally(Long userId, FriendshipStatus newStatus) {
        Resource<List<UserSearchResponseDTO>> currentResource = _searchResults.getValue();
        if (currentResource != null && currentResource.data != null) {
            List<UserSearchResponseDTO> list = new ArrayList<>();
            for (UserSearchResponseDTO u : currentResource.data) {
                UserSearchResponseDTO copy = new UserSearchResponseDTO();
                copy.setId(u.getId());
                copy.setUsername(u.getUsername());
                copy.setAvatarUrl(u.getAvatarUrl());
                copy.setIsOnline(u.getIsOnline());
                copy.setLastSeen(u.getLastSeen());
                if (u.getId().equals(userId)) {
                    copy.setFriendshipStatus(newStatus);
                } else {
                    copy.setFriendshipStatus(u.getFriendshipStatus());
                }
                list.add(copy);
            }
            _searchResults.setValue(Resource.success(list));
        }
    }
}
