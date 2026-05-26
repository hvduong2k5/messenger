package com.midterm.team12345.ui.discover;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.midterm.team12345.data.remote.dto.response.FriendshipStatus;
import com.midterm.team12345.data.remote.dto.response.UserSearchResponseDTO;
import com.midterm.team12345.domain.repository.FriendRepository;
import com.midterm.team12345.domain.repository.UserRepository;
import com.midterm.team12345.data.repository.UserRepositoryImpl;
import com.midterm.team12345.data.repository.FriendRepositoryImpl;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class DiscoverViewModel extends BaseViewModel {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    private final MutableLiveData<String> _query = new MutableLiveData<>("");
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Sử dụng switchMap để tự động hủy bỏ các kết quả cũ khi query thay đổi
    public final LiveData<Resource<List<UserSearchResponseDTO>>> searchResult = Transformations.switchMap(_query, query -> {
        if (query == null || query.trim().isEmpty()) {
            MutableLiveData<Resource<List<UserSearchResponseDTO>>> empty = new MutableLiveData<>();
            empty.setValue(Resource.success(new ArrayList<>()));
            return empty;
        }
        return Transformations.map(userRepository.searchUsers(query, 0, 50), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                return Resource.success(resource.data.getContent());
            } else if (resource.status == Resource.Status.ERROR) {
                return Resource.error(resource.message, null);
            }
            return Resource.loading(null);
        });
    });

    private final MutableLiveData<List<UserSearchResponseDTO>> _userList = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<UserSearchResponseDTO>> userList = _userList;

    public DiscoverViewModel(@NonNull Application application) {
        super();
        this.userRepository = UserRepositoryImpl.getInstance(application);
        this.friendRepository = FriendRepositoryImpl.getInstance(application);
    }

    public void onSearchQueryChanged(String query) {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        
        searchRunnable = () -> _query.setValue(query);
        searchHandler.postDelayed(searchRunnable, 500); // Debounce 500ms
    }

    public void setUserList(List<UserSearchResponseDTO> list) {
        _userList.setValue(list);
    }

    public void sendFriendRequest(Long userId, int position) {
        friendRepository.sendFriendRequest(userId).observeForever(resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                updateUserStatusInList(position, FriendshipStatus.SENDER_PENDING);
            } else if (resource.status == Resource.Status.ERROR) {
                setError(resource.message);
            }
        });
    }

    private void updateUserStatusInList(int position, FriendshipStatus status) {
        List<UserSearchResponseDTO> currentList = _userList.getValue();
        if (currentList != null && position >= 0 && position < currentList.size()) {
            List<UserSearchResponseDTO> newList = new ArrayList<>(currentList);
            UserSearchResponseDTO oldUser = newList.get(position);
            
            // Clone object để DiffUtil nhận diện thay đổi nội dung
            UserSearchResponseDTO newUser = new UserSearchResponseDTO();
            newUser.setId(oldUser.getId());
            newUser.setUsername(oldUser.getUsername());
            newUser.setAvatarUrl(oldUser.getAvatarUrl());
            newUser.setFriendshipStatus(status);
            
            newList.set(position, newUser);
            _userList.setValue(newList);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        searchHandler.removeCallbacksAndMessages(null);
    }
}
