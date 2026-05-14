package com.midterm.team12345.data.repository;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.dto.ConversationResponse;
import com.midterm.team12345.data.dto.ConversationRequestDTO;
import com.midterm.team12345.data.dto.ConversationResponseDTO;
import com.midterm.team12345.data.dto.MessageRequestDTO;
import com.midterm.team12345.data.dto.MessageResponse;
import com.midterm.team12345.data.dto.MessageResponseDTO;
import com.midterm.team12345.data.dto.MqttMessageDTO;
import com.midterm.team12345.data.dto.UserDTO;
import com.midterm.team12345.data.dto.UserProfileResponseDTO;
import com.midterm.team12345.data.remote.ConversationApiService;
import com.midterm.team12345.data.remote.MessageApiService;
import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.data.remote.UserApiService;
import com.midterm.team12345.util.Resource;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepositoryImpl implements ChatRepository {

    private static ChatRepositoryImpl instance;
    private final ConversationApiService conversationApiService;
    private final UserApiService userApiService;
    private final MessageApiService messageApiService;
    private final MutableLiveData<MqttMessageDTO> realTimeMessages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> connectionStatus = new MutableLiveData<>(false);

    private ChatRepositoryImpl(ConversationApiService conversationApiService, 
                               UserApiService userApiService,
                               MessageApiService messageApiService) {
        this.conversationApiService = conversationApiService;
        this.userApiService = userApiService;
        this.messageApiService = messageApiService;
    }

    public static synchronized ChatRepositoryImpl getInstance(Application application) {
        if (instance == null) {
            instance = new ChatRepositoryImpl(
                RetrofitClient.getConversationApiService(application),
                RetrofitClient.getUserApiService(application),
                RetrofitClient.getMessageApiService(application)
            );
        }
        return instance;
    }

    @Override
    public LiveData<Resource<List<ConversationResponse>>> getConversations() {
        MutableLiveData<Resource<List<ConversationResponse>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        conversationApiService.getConversations().enqueue(new Callback<List<ConversationResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ConversationResponseDTO>> call, @NonNull Response<List<ConversationResponseDTO>> response) {
                if (response.isSuccessful()) {
                    List<ConversationResponseDTO> body = response.body();
                    List<ConversationResponse> domainList = new ArrayList<>();
                    if (body != null) {
                        domainList = body.stream()
                                .map(ChatRepositoryImpl.this::mapConversationToDomain)
                                .collect(Collectors.toList());
                    }
                    data.setValue(Resource.success(domainList));
                } else {
                    data.setValue(Resource.error("Failed to fetch conversations", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ConversationResponseDTO>> call, @NonNull Throwable t) {
                if (t.getMessage() != null && t.getMessage().contains("BEGIN_ARRAY but was BEGIN_OBJECT")) {
                    data.setValue(Resource.success(new ArrayList<>()));
                } else {
                    data.setValue(Resource.error("Network error: " + t.getMessage(), null));
                }
            }
        });

        return data;
    }

    private ConversationResponse mapConversationToDomain(ConversationResponseDTO dto) {
        long timestamp = 0;
        if (dto.getLastMessageCreatedAt() != null) {
            timestamp = dto.getLastMessageCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else if (dto.getUpdatedAt() != null) {
            timestamp = dto.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        return new ConversationResponse(
                dto.getId(),
                dto.getName(),
                dto.getLastMessageContent(),
                dto.getAvatarUrl(),
                timestamp,
                dto.getUnreadCount() != null ? dto.getUnreadCount().intValue() : 0,
                false,
                false,
                dto.getIsGroup()
        );
    }

    @Override
    public LiveData<Resource<List<UserDTO>>> getFriends() {
        MutableLiveData<Resource<List<UserDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        userApiService.getFriends().enqueue(new Callback<List<UserDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserDTO>> call, @NonNull Response<List<UserDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(Resource.success(response.body()));
                } else {
                    data.setValue(Resource.error("Failed to fetch friends", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserDTO>> call, @NonNull Throwable t) {
                data.setValue(Resource.error("Network error: " + t.getMessage(), null));
            }
        });

        return data;
    }

    @Override
    public LiveData<Resource<ConversationResponse>> createConversation(ConversationRequestDTO request) {
        MutableLiveData<Resource<ConversationResponse>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        conversationApiService.createConversation(request).enqueue(new Callback<ConversationResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ConversationResponseDTO> call, @NonNull Response<ConversationResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(Resource.success(mapConversationToDomain(response.body())));
                } else {
                    data.setValue(Resource.error("Failed to create conversation", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ConversationResponseDTO> call, @NonNull Throwable t) {
                data.setValue(Resource.error("Network error: " + t.getMessage(), null));
            }
        });

        return data;
    }

    @Override
    public LiveData<Resource<List<MessageResponse>>> getMessages(Long conversationId) {
        MutableLiveData<Resource<List<MessageResponse>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        messageApiService.getMessagesByConversation(conversationId).enqueue(new Callback<List<MessageResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<MessageResponseDTO>> call, @NonNull Response<List<MessageResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MessageResponse> domainList = response.body().stream()
                            .map(ChatRepositoryImpl.this::mapMessageToDomain)
                            .collect(Collectors.toList());
                    data.setValue(Resource.success(domainList));
                } else {
                    data.setValue(Resource.error("Failed to fetch messages", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MessageResponseDTO>> call, @NonNull Throwable t) {
                if (t.getMessage() != null && t.getMessage().contains("BEGIN_ARRAY but was BEGIN_OBJECT")) {
                    data.setValue(Resource.success(new ArrayList<>()));
                } else {
                    data.setValue(Resource.error("Network error: " + t.getMessage(), null));
                }
            }
        });

        return data;
    }

    @Override
    public LiveData<Resource<MessageResponse>> sendMessage(MessageRequestDTO request) {
        MutableLiveData<Resource<MessageResponse>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        messageApiService.sendMessage(request).enqueue(new Callback<MessageResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<MessageResponseDTO> call, @NonNull Response<MessageResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(Resource.success(mapMessageToDomain(response.body())));
                } else {
                    data.setValue(Resource.error("Failed to send message", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessageResponseDTO> call, @NonNull Throwable t) {
                data.setValue(Resource.error("Network error: " + t.getMessage(), null));
            }
        });

        return data;
    }

    @Override
    public LiveData<Resource<UserProfileResponseDTO>> getMyProfile() {
        MutableLiveData<Resource<UserProfileResponseDTO>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        userApiService.getMyProfile().enqueue(new Callback<UserProfileResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponseDTO> call, @NonNull Response<UserProfileResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Failed to fetch profile", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponseDTO> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage(), null));
            }
        });

        return result;
    }

    private MessageResponse mapMessageToDomain(MessageResponseDTO dto) {
        MessageResponse domain = new MessageResponse();
        domain.setMessageId(dto.getMessageId());
        domain.setConversationId(dto.getConversationId());
        domain.setSenderId(dto.getSenderId());
        domain.setSenderUsername(dto.getSenderUsername());
        domain.setSenderAvatarUrl(dto.getSenderAvatarUrl());
        domain.setContent(dto.getContent());
        domain.setType(dto.getType());
        domain.setStatus(dto.getStatus());
        if (dto.getCreatedAt() != null) {
            domain.setCreatedAt(dto.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        domain.setDeleted(dto.getIsDeleted());
        domain.setEdited(dto.getIsEdited());
        domain.setAttachments(dto.getAttachments());
        return domain;
    }

    @Override
    public LiveData<MqttMessageDTO> getRealTimeMessages() {
        return realTimeMessages;
    }

    @Override
    public LiveData<Boolean> getConnectionStatus() {
        return connectionStatus;
    }

    public void emitRealTimeMessage(MqttMessageDTO message) {
        realTimeMessages.postValue(message);
    }

    public void updateConnectionStatus(boolean connected) {
        connectionStatus.postValue(connected);
    }
}
