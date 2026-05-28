package com.midterm.team12345.data.repository;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.data.remote.api.ConversationApiService;
import com.midterm.team12345.data.remote.dto.request.ConversationRequestDTO;
import com.midterm.team12345.data.remote.dto.request.ConversationUpdateDTO;
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO;
import com.midterm.team12345.data.remote.dto.MqttMessageDTO;
import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.utils.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationRepositoryImpl implements ConversationRepository {

    private static ConversationRepositoryImpl instance;
    private final ConversationApiService apiService;
    private final com.midterm.team12345.data.local.dao.ConversationDao conversationDao;
    private final com.midterm.team12345.data.local.database.MessengerDatabase database;

    private ConversationRepositoryImpl(
            ConversationApiService apiService,
            com.midterm.team12345.data.local.database.MessengerDatabase database) {
        this.apiService = apiService;
        this.database = database;
        this.conversationDao = database.conversationDao();
    }

    public static synchronized ConversationRepositoryImpl getInstance(Application application) {
        if (instance == null) {
            instance = new ConversationRepositoryImpl(
                RetrofitClient.getConversationApiService(application),
                com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(application)
            );
        }
        return instance;
    }

    @Override
    public LiveData<Resource<PageResponse<ConversationResponseDTO>>> getConversations(int page, int size) {
        androidx.lifecycle.MediatorLiveData<Resource<PageResponse<ConversationResponseDTO>>> mediator = new androidx.lifecycle.MediatorLiveData<>();
        mediator.setValue(Resource.loading(null));

        // 1. Get LiveData source from Room DB
        LiveData<List<com.midterm.team12345.data.local.entity.ConversationEntity>> dbSource = conversationDao.getConversations();

        // 2. Add dbSource to MediatorLiveData
        mediator.addSource(dbSource, localEntities -> {
            new java.lang.Thread(() -> {
                try {
                    List<ConversationResponseDTO> dtos = com.midterm.team12345.data.mapper.ConversationMapper.toDtoList(localEntities);
                    PageResponse<ConversationResponseDTO> pageResponse = new PageResponse<>();
                    pageResponse.setContent(dtos);
                    pageResponse.setTotalElements((long) dtos.size());
                    pageResponse.setTotalPages(1);
                    mediator.postValue(Resource.success(pageResponse));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

        // 3. Fetch from network in background to sync local DB
        apiService.getConversations(page, size).enqueue(new Callback<PageResponse<ConversationResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<ConversationResponseDTO>> call, @NonNull Response<PageResponse<ConversationResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new java.lang.Thread(() -> {
                        try {
                            List<ConversationResponseDTO> remoteDtos = response.body().getContent();
                            if (remoteDtos != null) {
                                for (ConversationResponseDTO dto : remoteDtos) {
                                    com.midterm.team12345.data.local.entity.ConversationEntity local = conversationDao.getConversationByIdSync(dto.getId());
                                    com.midterm.team12345.data.local.entity.ConversationEntity entity = 
                                            com.midterm.team12345.data.mapper.ConversationMapper.toEntity(dto);
                                    if (local != null) {
                                        // Fix race condition where local has reset unread count to 0, but remote is still lagging
                                        long localLastMsgTime = local.getLastMessageCreatedAt() != null ? local.getLastMessageCreatedAt() : 0L;
                                        long remoteLastMsgTime = entity.getLastMessageCreatedAt() != null ? entity.getLastMessageCreatedAt() : 0L;
                                        if (local.getUnreadCount() != null && local.getUnreadCount() == 0 && remoteLastMsgTime <= localLastMsgTime) {
                                            entity.setUnreadCount(0);
                                        }
                                    }
                                    conversationDao.insertConversation(entity);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
            @Override
            public void onFailure(@NonNull Call<PageResponse<ConversationResponseDTO>> call, @NonNull Throwable t) {
                // Ignore network sync failures in background
            }
        });

        return mediator;
    }

    @Override
    public LiveData<List<com.midterm.team12345.data.local.entity.ConversationEntity>> getLocalConversations() {
        return conversationDao.getConversations();
    }

    @Override
    public LiveData<List<com.midterm.team12345.data.local.entity.ConversationEntity>> searchLocalConversations(String query) {
        return conversationDao.searchConversations(query);
    }

    @Override
    public LiveData<Resource<ConversationResponseDTO>> getConversationDetails(Long id) {
        MutableLiveData<Resource<ConversationResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.getConversationDetails(id).enqueue(new Callback<ConversationResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ConversationResponseDTO> call, @NonNull Response<ConversationResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Failed to load details", null));
            }
            @Override
            public void onFailure(@NonNull Call<ConversationResponseDTO> call, @NonNull Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<PageResponse<ParticipantResponseDTO>>> getParticipants(Long id, String keyword, int page, int size) {
        MutableLiveData<Resource<PageResponse<ParticipantResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.getParticipants(id, keyword, page, size).enqueue(new Callback<PageResponse<ParticipantResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<ParticipantResponseDTO>> call, @NonNull Response<PageResponse<ParticipantResponseDTO>> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Failed to load members", null));
            }
            @Override
            public void onFailure(@NonNull Call<PageResponse<ParticipantResponseDTO>> call, @NonNull Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<ConversationResponseDTO>> createConversation(ConversationRequestDTO request) {
        MutableLiveData<Resource<ConversationResponseDTO>> data = new MutableLiveData<>();
        apiService.createConversation(request).enqueue(new Callback<ConversationResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ConversationResponseDTO> call, @NonNull Response<ConversationResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ConversationResponseDTO dto = response.body();
                    new java.lang.Thread(() -> {
                        try {
                            com.midterm.team12345.data.local.entity.ConversationEntity entity = 
                                    com.midterm.team12345.data.mapper.ConversationMapper.toEntity(dto);
                            conversationDao.insertConversation(entity);
                            
                            if (request.getParticipantIds() != null) {
                                for (Long pId : request.getParticipantIds()) {
                                    com.midterm.team12345.data.local.entity.ConversationParticipantEntity participant = 
                                            new com.midterm.team12345.data.local.entity.ConversationParticipantEntity(
                                                    dto.getId(),
                                                    pId,
                                                    "MEMBER",
                                                    null,
                                                    System.currentTimeMillis()
                                            );
                                    database.conversationParticipantDao().insertParticipant(participant);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                    data.setValue(Resource.success(dto));
                } else {
                    data.setValue(Resource.error("Failed to create", null));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ConversationResponseDTO> call, @NonNull Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> addParticipant(Long conversationId, Long userId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        Map<String, Long> body = Collections.singletonMap("userId", userId);
        apiService.addParticipant(conversationId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Failed to add participant", null));
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }});
        return data;
    }


    @Override
    public LiveData<Resource<Void>> addParticipants(Long conversationId, List<Long> userIds) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        if (userIds == null || userIds.isEmpty()) {
            data.setValue(Resource.success(null));
            return data;
        }

        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger failureCount = new java.util.concurrent.atomic.AtomicInteger(0);
        int total = userIds.size();

        for (Long userId : userIds) {
            Map<String, Long> body = Collections.singletonMap("userId", userId);
            apiService.addParticipant(conversationId, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                    if (successCount.get() + failureCount.get() == total) {
                        if (failureCount.get() == 0) {
                            data.setValue(Resource.success(null));
                        } else {
                            data.setValue(Resource.error("Failed to add " + failureCount.get() + " participants", null));
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    failureCount.incrementAndGet();
                    if (successCount.get() + failureCount.get() == total) {
                        data.setValue(Resource.error("Failed to add some participants: " + t.getMessage(), null));
                    }
                }
            });
        }
        return data;
    }


    @Override
    public LiveData<Resource<Void>> removeParticipant(Long conversationId, Long userId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.removeParticipant(conversationId, userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Failed to remove participant", null));
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> updateConversation(Long id, ConversationUpdateDTO request) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.updateConversation(id, request).enqueue(new Callback<ConversationResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ConversationResponseDTO> call, @NonNull Response<ConversationResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Failed to update conversation", null));
            }

            @Override
            public void onFailure(@NonNull Call<ConversationResponseDTO> call, @NonNull Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> leaveConversation(Long conversationId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.leaveConversation(conversationId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    new java.lang.Thread(() -> {
                        try {
                            database.runInTransaction(() -> {
                                database.conversationDao().deleteConversationById(conversationId);
                                database.conversationParticipantDao().deleteParticipantsByConversationId(conversationId);
                                database.messageDao().deleteMessagesByConversationId(conversationId);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                    data.setValue(Resource.success(null));
                } else {
                    data.setValue(Resource.error("Failed to leave conversation", null));
                }
            }
            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> updateParticipantRole(Long conversationId, Long participantId, String newRole) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.updateParticipantRole(conversationId, participantId, Collections.singletonMap("role", newRole)).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Failed to update role", null));
            }
            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<PageResponse<MessageResponseDTO>>> getMessages(Long conversationId, int page, int size) {
        MutableLiveData<Resource<PageResponse<MessageResponseDTO>>> data = new MutableLiveData<>();
        apiService.getMessages(conversationId, page, size).enqueue(new Callback<PageResponse<MessageResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<MessageResponseDTO>> call, @NonNull Response<PageResponse<MessageResponseDTO>> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
            }
            @Override
            public void onFailure(@NonNull Call<PageResponse<MessageResponseDTO>> call, @NonNull Throwable t) {}
        });
        return data;
    }

    private final MutableLiveData<MqttMessageDTO> realTimeMessages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> connectionStatus = new MutableLiveData<>();

    @Override
    public LiveData<MqttMessageDTO> getRealTimeMessages() {
        return realTimeMessages;
    }

    @Override
    public LiveData<Boolean> getConnectionStatus() {
        return connectionStatus;
    }

    @Override
    public void emitRealTimeMessage(MqttMessageDTO message) {
        realTimeMessages.postValue(message);
    }

    @Override
    public void updateConnectionStatus(boolean connected) {
        connectionStatus.postValue(connected);
    }
}
