package com.midterm.team12345.data.repository;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.dto.response.ConversationResponse;
import com.midterm.team12345.data.remote.dto.request.ConversationRequestDTO;
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.data.remote.dto.request.MessageRequestDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponse;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.MqttMessageDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.data.remote.dto.response.UserDTO;
import com.midterm.team12345.data.remote.dto.response.UserProfileResponseDTO;
import com.midterm.team12345.data.remote.api.ConversationApiService;
import com.midterm.team12345.data.remote.api.MessageApiService;
import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.data.remote.api.UserApiService;
import com.midterm.team12345.domain.repository.ChatRepository;
import com.midterm.team12345.utils.Resource;

import java.time.LocalDateTime;
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

        conversationApiService.getConversations().enqueue(new Callback<PageResponse<ConversationResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<ConversationResponseDTO>> call, @NonNull Response<PageResponse<ConversationResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ConversationResponseDTO> bodyContent = response.body().getContent();
                    List<ConversationResponse> domainList = new ArrayList<>();
                    if (bodyContent != null) {
                        domainList = bodyContent.stream()
                                .map(ChatRepositoryImpl.this::mapConversationToDomain)
                                .collect(Collectors.toList());
                    }
                    data.setValue(Resource.success(domainList));
                } else {
                    data.setValue(Resource.error("Failed to fetch conversations", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<ConversationResponseDTO>> call, @NonNull Throwable t) {
                data.setValue(Resource.error("Network error: " + t.getMessage(), null));
            }
        });

        return data;
    }


    private ConversationResponse mapConversationToDomain(ConversationResponseDTO dto) {
        long timestamp = 0;

        String timeStr = (dto.getLastMessageCreatedAt() != null) ? dto.getLastMessageCreatedAt() : dto.getUpdatedAt();

        if (timeStr != null && !timeStr.isEmpty()) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    timestamp = LocalDateTime.parse(timeStr)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli();
                } else {
                    // Fallback cho Android cũ
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                    timestamp = sdf.parse(timeStr).getTime();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

        messageApiService.getMessagesByConversation(conversationId).enqueue(new Callback<PageResponse<MessageResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<MessageResponseDTO>> call, @NonNull Response<PageResponse<MessageResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MessageResponseDTO> bodyContent = response.body().getContent();
                    List<MessageResponse> domainList = new ArrayList<>();
                    if (bodyContent != null) {
                        domainList = bodyContent.stream()
                                .map(ChatRepositoryImpl.this::mapMessageToDomain)
                                .collect(Collectors.toList());
                    }
                    data.setValue(Resource.success(domainList));
                } else {
                    data.setValue(Resource.error("Failed to fetch messages", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<MessageResponseDTO>> call, @NonNull Throwable t) {
                data.setValue(Resource.error("Network error: " + t.getMessage(), null));
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
        
        if (dto.getCreatedAt() != null && !dto.getCreatedAt().isEmpty()) {
            try {
                long timestamp = 0;
                String timeStr = dto.getCreatedAt();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    timestamp = LocalDateTime.parse(timeStr)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli();
                } else {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                    timestamp = sdf.parse(timeStr).getTime();
                }
                domain.setCreatedAt(timestamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
