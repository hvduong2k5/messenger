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
import com.midterm.team12345.data.repository.FriendRepositoryImpl;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMembersViewModel extends BaseViewModel {
    private final FriendRepository friendRepository;
    private final ConversationApiService conversationApiService;
    
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private List<Long> existingParticipantIds = new ArrayList<>();
    
    public final LiveData<List<UserEntity>> friends;
    public final SingleLiveEvent<Void> addSuccessEvent = new SingleLiveEvent<>();

    public AddMembersViewModel(@NonNull Application application) {
        super();
        this.friendRepository = FriendRepositoryImpl.getInstance(application);
        this.conversationApiService = RetrofitClient.getConversationApiService(application);

        this.friends = Transformations.switchMap(searchQuery, query -> 
            Transformations.map(friendRepository.searchFriendsLocally(query), list -> 
                list.stream()
                    .filter(user -> !existingParticipantIds.contains(user.getId()))
                    .collect(Collectors.toList())
            )
        );
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
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger totalCount = new AtomicInteger(selectedIds.size());

        for (Long userId : selectedIds) {
            Map<String, Long> body = new HashMap<>();
            body.put("userId", userId);
            
            conversationApiService.addParticipant(conversationId, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        if (successCount.incrementAndGet() == totalCount.get()) {
                            hideLoading();
                            addSuccessEvent.call();
                        }
                    } else {
                        hideLoading();
                        setError("Lỗi khi thêm thành viên: " + userId);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    hideLoading();
                    setError("Lỗi kết nối mạng");
                }
            });
        }
    }
}
