package com.midterm.team12345.ui.friends;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.midterm.team12345.network.UserResponse;
import com.midterm.team12345.ui.base.BaseViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FriendsViewModel extends BaseViewModel {

    private final MutableLiveData<List<UserResponse>> _friendRequests = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<UserResponse>> friendRequests = _friendRequests;

    private final MutableLiveData<List<Object>> _friendsListGrouped = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<Object>> friendsListGrouped = _friendsListGrouped;

    private final MutableLiveData<List<UserResponse>> _suggestions = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<UserResponse>> suggestions = _suggestions;

    private List<UserResponse> allFriendsRaw = new ArrayList<>();

    public FriendsViewModel() {
        loadData();
    }

    public void loadData() {
        showLoading();
        
        // 1. Mock Lời mời kết bạn
        List<UserResponse> requests = new ArrayList<>();
        requests.add(new UserResponse(101L, "Nguyễn Văn A", "pending"));
        requests.add(new UserResponse(102L, "Trần Thị B", "pending"));
        _friendRequests.setValue(requests);

        // 2. Mock Danh sách bạn bè
        allFriendsRaw = new ArrayList<>();
        allFriendsRaw.add(new UserResponse(1L, "Ái Vân", "online"));
        allFriendsRaw.add(new UserResponse(2L, "Ba Nam", "online"));
        allFriendsRaw.add(new UserResponse(3L, "Bảo Ngọc", "offline"));
        allFriendsRaw.add(new UserResponse(4L, "Bee", "online"));
        allFriendsRaw.add(new UserResponse(5L, "Boss", "offline"));
        allFriendsRaw.add(new UserResponse(6L, "Cường", "online"));
        allFriendsRaw.add(new UserResponse(7L, "Duy", "online"));
        
        updateGroupedList(allFriendsRaw);

        // 3. Mock Gợi ý kết bạn
        List<UserResponse> suggested = new ArrayList<>();
        suggested.add(new UserResponse(201L, "Người quen cũ", "offline"));
        _suggestions.setValue(suggested);

        hideLoading();
    }

    private void updateGroupedList(List<UserResponse> list) {
        Collections.sort(list, (u1, u2) -> u1.getUsername().compareToIgnoreCase(u2.getUsername()));
        List<Object> grouped = new ArrayList<>();
        char lastChar = ' ';
        for (UserResponse user : list) {
            char firstChar = user.getUsername().toUpperCase().charAt(0);
            if (firstChar != lastChar) {
                grouped.add(String.valueOf(firstChar));
                lastChar = firstChar;
            }
            grouped.add(user);
        }
        _friendsListGrouped.setValue(grouped);
    }

    public void onSearch(String query) {
        if (query.isEmpty()) {
            updateGroupedList(allFriendsRaw);
        } else {
            List<UserResponse> filtered = allFriendsRaw.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
            updateGroupedList(filtered);
        }
    }

    public void acceptRequest(Long id) { /* API call */ }
    public void deleteRequest(Long id) { /* API call */ }
}
