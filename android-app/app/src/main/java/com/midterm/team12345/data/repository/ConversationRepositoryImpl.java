package com.midterm.team12345.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.data.remote.api.ConversationApiService;
import com.midterm.team12345.data.remote.dto.MqttMessageDTO;
import com.midterm.team12345.data.remote.dto.request.ConversationRequestDTO;
import com.midterm.team12345.data.remote.dto.request.ConversationUpdateDTO;
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.utils.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationRepositoryImpl implements ConversationRepository {
    private static ConversationRepositoryImpl instance;
    private final ConversationApiService apiService;
    private final com.midterm.team12345.data.local.dao.ConversationDao conversationDao;
    private final MutableLiveData<MqttMessageDTO> realTimeMessages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> connectionStatus = new MutableLiveData<>(false);

    private ConversationRepositoryImpl(ConversationApiService apiService, com.midterm.team12345.data.local.dao.ConversationDao conversationDao) {
        this.apiService = apiService;
        this.conversationDao = conversationDao;
    }

    public static synchronized ConversationRepositoryImpl getInstance(Context context) {
        if (instance == null) {
            instance = new ConversationRepositoryImpl(
                    RetrofitClient.getConversationApiService(context),
                    com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(context).conversationDao()
            );
        }
        return instance;
    }

    @Override
    public LiveData<Resource<PageResponse<ConversationResponseDTO>>> getConversations(int page, int size) {
        MutableLiveData<Resource<PageResponse<ConversationResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        // 1. Load from Local Room DB immediately in a background thread
        new java.lang.Thread(() -> {
            try {
                List<com.midterm.team12345.data.local.entity.ConversationEntity> localEntities = conversationDao.getConversationsSync();
                if (localEntities != null && !localEntities.isEmpty()) {
                    List<ConversationResponseDTO> dtoContent = localEntities.stream()
                            .map(com.midterm.team12345.data.mapper.ConversationMapper::toDto)
                            .collect(Collectors.collectingAndThen(Collectors.toList(), java.util.ArrayList::new));
                    
                    PageResponse<ConversationResponseDTO> pageResponse = new PageResponse<>();
                    pageResponse.setContent(dtoContent);
                    pageResponse.setTotalPages(1);
                    pageResponse.setTotalElements(dtoContent.size());
                    
                    data.postValue(Resource.success(pageResponse));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 2. Fetch from network in the background
            apiService.getConversations(page, size).enqueue(new Callback<PageResponse<ConversationResponseDTO>>() {
                @Override
                public void onResponse(Call<PageResponse<ConversationResponseDTO>> call, Response<PageResponse<ConversationResponseDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        new java.lang.Thread(() -> {
                            try {
                                List<ConversationResponseDTO> remoteDtos = response.body().getContent();
                                if (remoteDtos != null) {
                                    List<com.midterm.team12345.data.local.entity.ConversationEntity> entities = 
                                            com.midterm.team12345.data.mapper.ConversationMapper.toEntityList(remoteDtos);
                                    conversationDao.insertConversations(entities);
                                }
                                
                                // Fetch updated list from DB to ensure single source of truth
                                List<com.midterm.team12345.data.local.entity.ConversationEntity> updatedLocal = conversationDao.getConversationsSync();
                                List<ConversationResponseDTO> updatedDtos = updatedLocal.stream()
                                        .map(com.midterm.team12345.data.mapper.ConversationMapper::toDto)
                                        .collect(Collectors.collectingAndThen(Collectors.toList(), java.util.ArrayList::new));
                                
                                PageResponse<ConversationResponseDTO> pageResponse = new PageResponse<>();
                                pageResponse.setContent(updatedDtos);
                                pageResponse.setTotalPages(response.body().getTotalPages());
                                pageResponse.setTotalElements(response.body().getTotalElements());
                                
                                data.postValue(Resource.success(pageResponse));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                }

                @Override
                public void onFailure(Call<PageResponse<ConversationResponseDTO>> call, Throwable t) {
                    // Do nothing on failure to keep showing local cache
                }
            });
        }).start();

        return data;
    }

    @Override
    public LiveData<Resource<ConversationResponseDTO>> getConversationDetails(Long id) {
        MutableLiveData<Resource<ConversationResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.getConversationDetails(id).enqueue(new Callback<ConversationResponseDTO>() {
            @Override
            public void onResponse(Call<ConversationResponseDTO> call, Response<ConversationResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Failed to get details", null));
            }
            @Override
            public void onFailure(Call<ConversationResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<ConversationResponseDTO>> createConversation(ConversationRequestDTO request) {
        MutableLiveData<Resource<ConversationResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.createConversation(request).enqueue(new Callback<ConversationResponseDTO>() {
            @Override
            public void onResponse(Call<ConversationResponseDTO> call, Response<ConversationResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Failed to create conversation", null));
            }
            @Override
            public void onFailure(Call<ConversationResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> addParticipant(Long conversationId, Long userId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        Map<String, Long> body = new HashMap<>();
        body.put("userId", userId);
        apiService.addParticipant(conversationId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Failed to add participant", null));
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> removeParticipant(Long conversationId, Long userId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.removeParticipant(conversationId, userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Failed to remove participant", null));
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
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
            public void onResponse(Call<ConversationResponseDTO> call, Response<ConversationResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Failed to update conversation", null));
            }
            @Override
            public void onFailure(Call<ConversationResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> leaveConversation(Long conversationId) {
        // Leave is often implemented as removing self from participants on backend, 
        // or a specific endpoint. Assuming there's a dedicated endpoint or we just use removeParticipant for now if we have user ID.
        // If we don't have user ID here easily, let's assume the API might have a specific leave endpoint in a real scenario.
        // Since I don't see it in ConversationApiService, I'll add it there first.
        return new MutableLiveData<>(Resource.error("Method not fully implemented - Check API", null));
    }

    @Override
    public LiveData<Resource<PageResponse<MessageResponseDTO>>> getMessages(Long conversationId, int page, int size) {
        MutableLiveData<Resource<PageResponse<MessageResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.getMessages(conversationId, page, size).enqueue(new Callback<PageResponse<MessageResponseDTO>>() {
            @Override
            public void onResponse(Call<PageResponse<MessageResponseDTO>> call, Response<PageResponse<MessageResponseDTO>> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Failed to fetch messages", null));
            }
            @Override
            public void onFailure(Call<PageResponse<MessageResponseDTO>> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

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
