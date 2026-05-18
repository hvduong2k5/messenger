package com.midterm.team12345.ui.friends;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.dto.response.FriendRequestResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserSearchResponseDTO;
import com.midterm.team12345.domain.repository.FriendRepository;
import com.midterm.team12345.domain.repository.UserRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FriendsViewModel extends BaseViewModel {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<Resource<List<UserResponseDTO>>> _friendsList = new MutableLiveData<>();
    public final LiveData<Resource<List<UserResponseDTO>>> friendsList = _friendsList;

    private final MutableLiveData<Resource<List<FriendRequestResponseDTO>>> _pendingRequests = new MutableLiveData<>();
    public final LiveData<Resource<List<FriendRequestResponseDTO>>> pendingRequests = _pendingRequests;

    private final MutableLiveData<Resource<List<UserSearchResponseDTO>>> _searchResults = new MutableLiveData<>();
    public final LiveData<Resource<List<UserSearchResponseDTO>>> searchResults = _searchResults;

    private final MutableLiveData<Resource<com.midterm.team12345.data.remote.dto.response.UserProfileResponseDTO>> _currentUserProfile = new MutableLiveData<>();
    public final LiveData<Resource<com.midterm.team12345.data.remote.dto.response.UserProfileResponseDTO>> currentUserProfile = _currentUserProfile;

    private final MutableLiveData<List<Object>> _friendsListGrouped = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<Object>> friendsListGrouped = _friendsListGrouped;

    private final MutableLiveData<Resource<com.midterm.team12345.data.remote.dto.response.FriendshipStatusResponseDTO>> _friendshipStatus = new MutableLiveData<>();
    public final LiveData<Resource<com.midterm.team12345.data.remote.dto.response.FriendshipStatusResponseDTO>> friendshipStatus = _friendshipStatus;

    private List<UserResponseDTO> allFriendsRaw = new ArrayList<>();
    private String lastSearchQuery = "";

    public FriendsViewModel(FriendRepository friendRepository, UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
    }

    public void loadData() {
        fetchFriends();
        fetchPendingRequests();
        fetchCurrentUserProfile();
    }

    public void fetchCurrentUserProfile() {
        _currentUserProfile.setValue(Resource.loading(null));
        userRepository.getMyProfile().observeForever(resource -> {
            if (resource != null) {
                _currentUserProfile.setValue(resource);
            }
        });
    }

    public void fetchFriends() {
        _friendsList.setValue(Resource.loading(null));
        friendRepository.getFriends().observeForever(resource -> {
            if (resource != null) {
                _friendsList.setValue(resource);
                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    allFriendsRaw = resource.data;
                    updateGroupedList(resource.data);
                } else if (resource.status == Resource.Status.ERROR) {
                    setError(resource.message);
                }
            }
        });
    }

    public void fetchPendingRequests() {
        _pendingRequests.setValue(Resource.loading(null));
        friendRepository.getPendingRequests().observeForever(resource -> {
            if (resource != null) {
                _pendingRequests.setValue(resource);
                if (resource.status == Resource.Status.ERROR) {
                    setError(resource.message);
                }
            }
        });
    }

    public void searchUser(String keyword) {
        lastSearchQuery = keyword;
        if (keyword == null || keyword.trim().isEmpty()) {
            _searchResults.setValue(Resource.success(new ArrayList<>()));
            return;
        }
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

    public void onAcceptRequest(Long requestId) {
        friendRepository.acceptFriendRequest(requestId).observeForever(resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                fetchFriends();
                fetchPendingRequests();
                // If there was an active search, refresh the search results as well to update FriendshipStatus to FRIEND
                if (lastSearchQuery != null && !lastSearchQuery.trim().isEmpty()) {
                    searchUser(lastSearchQuery);
                }
            } else if (resource != null && resource.status == Resource.Status.ERROR) {
                setError(resource.message);
            }
        });
    }

    public void onRejectRequest(Long requestId) {
        friendRepository.rejectFriendRequest(requestId).observeForever(resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                fetchPendingRequests();
                // Refresh active search results
                if (lastSearchQuery != null && !lastSearchQuery.trim().isEmpty()) {
                    searchUser(lastSearchQuery);
                }
            } else if (resource != null && resource.status == Resource.Status.ERROR) {
                setError(resource.message);
            }
        });
    }

    public void onAddFriend(Long userId) {
        friendRepository.sendFriendRequest(userId).observeForever(resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                updateLocalSearchFriendshipStatus(userId, com.midterm.team12345.data.remote.dto.response.FriendshipStatus.SENDER_PENDING);
                // Once invitation is sent, refresh the search results so that status changes to SENDER_PENDING
                if (lastSearchQuery != null && !lastSearchQuery.trim().isEmpty()) {
                    searchUser(lastSearchQuery);
                }
            } else if (resource != null && resource.status == Resource.Status.ERROR) {
                setError(resource.message);
            }
        });
    }

    public void onCancelFriendRequest(Long userId) {
        friendRepository.cancelFriendRequest(userId).observeForever(resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                updateLocalSearchFriendshipStatus(userId, com.midterm.team12345.data.remote.dto.response.FriendshipStatus.STRANGER);
                // Once cancelled, refresh the search results so status returns to STRANGER
                if (lastSearchQuery != null && !lastSearchQuery.trim().isEmpty()) {
                    searchUser(lastSearchQuery);
                }
            } else if (resource != null && resource.status == Resource.Status.ERROR) {
                setError(resource.message);
            }
        });
    }

    public void onUnfriend(Long userId) {
        friendRepository.unfriend(userId).observeForever(resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                updateLocalSearchFriendshipStatus(userId, com.midterm.team12345.data.remote.dto.response.FriendshipStatus.STRANGER);
                fetchFriends(); // Refresh friends list
                if (lastSearchQuery != null && !lastSearchQuery.trim().isEmpty()) {
                    searchUser(lastSearchQuery);
                }
            } else if (resource != null && resource.status == Resource.Status.ERROR) {
                setError(resource.message);
            }
        });
    }

    private void updateLocalSearchFriendshipStatus(Long userId, com.midterm.team12345.data.remote.dto.response.FriendshipStatus status) {
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
                    copy.setFriendshipStatus(status);
                } else {
                    copy.setFriendshipStatus(u.getFriendshipStatus());
                }
                list.add(copy);
            }
            _searchResults.setValue(Resource.success(list));
        }
    }

    public void checkFriendshipStatus(Long userId) {
        _friendshipStatus.setValue(Resource.loading(null));
        friendRepository.checkFriendshipStatus(userId).observeForever(resource -> {
            if (resource != null) {
                _friendshipStatus.setValue(resource);
                if (resource.status == Resource.Status.ERROR) {
                    setError(resource.message);
                }
            }
        });
    }

    public void onLocalSearch(String query) {
        if (query == null || query.isEmpty()) {
            updateGroupedList(allFriendsRaw);
        } else {
            List<UserResponseDTO> filtered = new ArrayList<>();
            for (UserResponseDTO u : allFriendsRaw) {
                if (u.getUsername() != null && u.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(u);
                }
            }
            updateGroupedList(filtered);
        }
    }

    private void updateGroupedList(List<UserResponseDTO> list) {
        if (list == null) {
            _friendsListGrouped.setValue(new ArrayList<>());
            return;
        }
        List<UserResponseDTO> sorted = new ArrayList<>(list);
        Collections.sort(sorted, (u1, u2) -> {
            String name1 = u1.getUsername() != null ? u1.getUsername() : "";
            String name2 = u2.getUsername() != null ? u2.getUsername() : "";
            return name1.compareToIgnoreCase(name2);
        });

        List<Object> grouped = new ArrayList<>();
        char lastChar = ' ';
        for (UserResponseDTO user : sorted) {
            String username = user.getUsername();
            if (username == null || username.isEmpty()) continue;
            char firstChar = username.toUpperCase().charAt(0);
            if (firstChar != lastChar) {
                grouped.add(String.valueOf(firstChar));
                lastChar = firstChar;
            }
            grouped.add(user);
        }
        _friendsListGrouped.setValue(grouped);
    }
}
