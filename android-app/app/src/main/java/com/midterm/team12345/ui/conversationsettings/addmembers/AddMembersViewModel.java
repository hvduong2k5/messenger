package com.midterm.team12345.ui.conversationsettings.addmembers;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.midterm.team12345.data.local.entity.UserEntity;
import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.data.remote.api.ConversationApiService;
import com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
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

        // Trigger remote sync of friends list
        friendRepository.getFriends().observeForever(resource -> {});

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

    public void init(Long conversationId) {
        if (conversationId == null) return;
        conversationApiService.getParticipants(conversationId, "", 0, 100).enqueue(new Callback<PageResponse<ParticipantResponseDTO>>() {
            @Override
            public void onResponse(Call<PageResponse<ParticipantResponseDTO>> call, Response<PageResponse<ParticipantResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Long> ids = response.body().getContent().stream()
                            .map(ParticipantResponseDTO::getUserId)
                            .collect(Collectors.toList());
                    setExistingParticipants(ids);

                    // Also save these participants to Room to keep local DB in sync!
                    executor.execute(() -> {
                        try {
                            List<ConversationParticipantEntity> entities = new ArrayList<>();
                            for (Long userId : ids) {
                                ConversationParticipantEntity p = new ConversationParticipantEntity();
                                p.setConversationId(conversationId);
                                p.setUserId(userId);
                                p.setRole("MEMBER");
                                entities.add(p);
                            }
                            DatabaseProvider.getInstance(application).getConversationParticipantDao().insertParticipants(entities);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<PageResponse<ParticipantResponseDTO>> call, Throwable t) {
                // Keep the passed list in case of network failure
            }
        });
    }

    public void onSearchQueryChanged(String query) {
        searchQuery.setValue(query);
    }

    public void addParticipants(Long conversationId, Set<Long> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) return;
        
        showLoading();
        
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger failureCount = new java.util.concurrent.atomic.AtomicInteger(0);
        int total = selectedIds.size();
        
        for (Long userId : selectedIds) {
            Map<String, Long> body = new HashMap<>();
            body.put("userId", userId);
            
            conversationApiService.addParticipant(conversationId, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                    checkCompletion(successCount.get(), failureCount.get(), total, conversationId, selectedIds);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    failureCount.incrementAndGet();
                    checkCompletion(successCount.get(), failureCount.get(), total, conversationId, selectedIds);
                }
            });
        }
    }

    private void checkCompletion(int successCount, int failureCount, int total, Long conversationId, Set<Long> selectedIds) {
        if (successCount + failureCount == total) {
            hideLoading();
            if (successCount > 0) {
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
            }
            if (failureCount > 0) {
                setError("Lỗi khi thêm " + failureCount + " thành viên vào nhóm");
            }
        }
    }
}
