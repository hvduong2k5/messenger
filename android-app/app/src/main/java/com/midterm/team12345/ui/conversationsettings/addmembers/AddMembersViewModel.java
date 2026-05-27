package com.midterm.team12345.ui.conversationsettings.addmembers;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.midterm.team12345.data.local.entity.UserEntity;
import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.data.remote.api.ConversationApiService;
import com.midterm.team12345.domain.repository.FriendRepository;
import com.midterm.team12345.data.local.entity.ConversationParticipantEntity;
import com.midterm.team12345.data.local.database.DatabaseProvider;
import com.midterm.team12345.data.repository.FriendRepositoryImpl;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMembersViewModel extends BaseViewModel {
    private final FriendRepository friendRepository;
    private final ConversationApiService conversationApiService;
    private final Application application;
    
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private List<Long> existingParticipantIds = new ArrayList<>();
    
    private final MutableLiveData<List<UserEntity>> _friends = new MutableLiveData<>();
    public final LiveData<List<UserEntity>> friends = _friends;
    public final SingleLiveEvent<Void> addSuccessEvent = new SingleLiveEvent<>();

    private List<UserEntity> allFriendsList = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AddMembersViewModel(@NonNull Application application) {
        super();
        this.application = application;
        this.friendRepository = FriendRepositoryImpl.getInstance(application);
        this.conversationApiService = RetrofitClient.getConversationApiService(application);

        LiveData<List<UserEntity>> allFriendsLiveData = friendRepository.searchFriendsLocally("");
        allFriendsLiveData.observeForever(users -> {
            if (users != null) {
                allFriendsList = users;
                filterFriends(searchQuery.getValue());
            }
        });

        searchQuery.observeForever(this::filterFriends);
    }

    private void filterFriends(String query) {
        executor.execute(() -> {
            String q = query != null ? query.toLowerCase() : "";
            List<UserEntity> filtered = allFriendsList.stream()
                    .filter(user -> !existingParticipantIds.contains(user.getId()))
                    .filter(user -> q.isEmpty() || user.getUsername().toLowerCase().contains(q))
                    .collect(Collectors.toList());
            _friends.postValue(filtered);
        });
    }

    public void setExistingParticipants(List<Long> ids) {
        this.existingParticipantIds = ids != null ? ids : new ArrayList<>();
        searchQuery.setValue(searchQuery.getValue());
    }

    public void onSearchQueryChanged(String query) {
        searchQuery.setValue(query);
    }

    public void addParticipants(Long conversationId, Set<Long> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) return;
        
        showLoading();
        
        Map<String, List<Long>> body = new HashMap<>();
        body.put("userIds", new ArrayList<>(selectedIds));
        
        conversationApiService.addParticipants(conversationId, body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                hideLoading();
                if (response.isSuccessful()) {
                    // Update Local Room DB with new members
                    executor.execute(() -> {
                        List<ConversationParticipantEntity> newParticipants = new ArrayList<>();
                        for (Long userId : selectedIds) {
                            ConversationParticipantEntity participant = new ConversationParticipantEntity();
                            participant.setConversationId(conversationId);
                            participant.setUserId(userId);
                            participant.setRole("MEMBER");
                            newParticipants.add(participant);
                        }
                        try {
                            DatabaseProvider.getInstance(application).getConversationParticipantDao().insertParticipants(newParticipants);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        addSuccessEvent.postValue(null);
                    });
                } else {
                    setError("Lỗi khi thêm thành viên");
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                hideLoading();
                setError("Lỗi kết nối mạng");
            }
        });
    }
}
