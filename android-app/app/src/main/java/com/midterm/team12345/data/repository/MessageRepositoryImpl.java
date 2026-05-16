package com.midterm.team12345.data.repository;

import android.content.Context;
import android.os.Build;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.data.remote.api.ConversationApiService;
import com.midterm.team12345.data.remote.api.MessageApiService;
import com.midterm.team12345.data.remote.api.MessageStatusApiService;
import com.midterm.team12345.data.remote.dto.request.MessageRequestDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponse;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.MessageStatusResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.domain.repository.MessageRepository;
import com.midterm.team12345.utils.Resource;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageRepositoryImpl implements MessageRepository {
    private static MessageRepositoryImpl instance;
    private final MessageApiService messageApiService;
    private final MessageStatusApiService messageStatusApiService;
    private final ConversationApiService conversationApiService;

    private MessageRepositoryImpl(MessageApiService messageApiService, 
                                 MessageStatusApiService messageStatusApiService,
                                 ConversationApiService conversationApiService) {
        this.messageApiService = messageApiService;
        this.messageStatusApiService = messageStatusApiService;
        this.conversationApiService = conversationApiService;
    }

    public static synchronized MessageRepositoryImpl getInstance(Context context) {
        if (instance == null) {
            instance = new MessageRepositoryImpl(
                RetrofitClient.getMessageApiService(context),
                RetrofitClient.getMessageStatusApiService(context),
                RetrofitClient.getConversationApiService(context)
            );
        }
        return instance;
    }

    @Override
    public LiveData<Resource<List<MessageResponse>>> getMessages(Long conversationId, int page, int size) {
        MutableLiveData<Resource<List<MessageResponse>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        conversationApiService.getMessages(conversationId, page, size).enqueue(new Callback<PageResponse<MessageResponseDTO>>() {
            @Override
            public void onResponse(Call<PageResponse<MessageResponseDTO>> call, Response<PageResponse<MessageResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MessageResponseDTO> content = response.body().getContent();
                    List<MessageResponse> domainList = new ArrayList<>();
                    if (content != null) {
                        domainList = content.stream()
                                .map(MessageRepositoryImpl.this::mapMessageToDomain)
                                .collect(Collectors.toList());
                    }
                    data.setValue(Resource.success(domainList));
                } else {
                    data.setValue(Resource.error("Lỗi lấy danh sách tin nhắn", null));
                }
            }

            @Override
            public void onFailure(Call<PageResponse<MessageResponseDTO>> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
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
            public void onResponse(Call<MessageResponseDTO> call, Response<MessageResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(Resource.success(mapMessageToDomain(response.body())));
                } else {
                    data.setValue(Resource.error("Gửi tin nhắn thất bại", null));
                }
            }

            @Override
            public void onFailure(Call<MessageResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> revokeMessage(Long messageId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        messageApiService.revokeMessage(messageId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Thu hồi tin nhắn thất bại", null));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<MessageResponse>> editMessage(Long messageId, MessageRequestDTO request) {
        MutableLiveData<Resource<MessageResponse>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        messageApiService.editMessage(messageId, request).enqueue(new Callback<MessageResponseDTO>() {
            @Override
            public void onResponse(Call<MessageResponseDTO> call, Response<MessageResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(Resource.success(mapMessageToDomain(response.body())));
                } else {
                    data.setValue(Resource.error("Chỉnh sửa tin nhắn thất bại", null));
                }
            }

            @Override
            public void onFailure(Call<MessageResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> markConversationAsRead(Long conversationId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        messageStatusApiService.markConversationAsRead(conversationId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Đánh dấu đã đọc thất bại", null));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> updateMessageStatus(Long messageId, String status) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        Map<String, String> body = new HashMap<>();
        body.put("status", status);
        messageStatusApiService.updateMessageStatus(messageId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Cập nhật trạng thái tin nhắn thất bại", null));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<List<MessageStatusResponseDTO>>> getMessageStatuses(Long messageId) {
        MutableLiveData<Resource<List<MessageStatusResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        messageStatusApiService.getMessageStatuses(messageId).enqueue(new Callback<List<MessageStatusResponseDTO>>() {
            @Override
            public void onResponse(Call<List<MessageStatusResponseDTO>> call, Response<List<MessageStatusResponseDTO>> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Lấy trạng thái tin nhắn thất bại", null));
            }

            @Override
            public void onFailure(Call<List<MessageStatusResponseDTO>> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
                try {
                    domain.setCreatedAt(Long.parseLong(dto.getCreatedAt()));
                } catch (NumberFormatException ignored) {}
            }
        }

        domain.setDeleted(dto.getIsDeleted() != null && dto.getIsDeleted());
        domain.setEdited(dto.getIsEdited() != null && dto.getIsEdited());
        domain.setAttachments(dto.getAttachments());
        return domain;
    }
}
