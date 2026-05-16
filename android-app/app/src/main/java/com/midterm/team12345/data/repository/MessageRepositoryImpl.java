package com.midterm.team12345.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.api.MessageApiService;
import com.midterm.team12345.data.remote.api.MessageStatusApiService;
import com.midterm.team12345.data.remote.dto.request.MessageRequestDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.MessageStatusResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.domain.repository.MessageRepository;
import com.midterm.team12345.utils.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageRepositoryImpl implements MessageRepository {
    private final MessageApiService messageApiService;
    private final MessageStatusApiService messageStatusApiService;

    public MessageRepositoryImpl(MessageApiService messageApiService, MessageStatusApiService messageStatusApiService) {
        this.messageApiService = messageApiService;
        this.messageStatusApiService = messageStatusApiService;
    }

    @Override
    public LiveData<Resource<MessageResponseDTO>> sendMessage(MessageRequestDTO request) {
        MutableLiveData<Resource<MessageResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        messageApiService.sendMessage(request).enqueue(new Callback<MessageResponseDTO>() {
            @Override
            public void onResponse(Call<MessageResponseDTO> call, Response<MessageResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Gửi tin nhắn thất bại", null));
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
    public LiveData<Resource<MessageResponseDTO>> editMessage(Long messageId, MessageRequestDTO request) {
        MutableLiveData<Resource<MessageResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        messageApiService.editMessage(messageId, request).enqueue(new Callback<MessageResponseDTO>() {
            @Override
            public void onResponse(Call<MessageResponseDTO> call, Response<MessageResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Chỉnh sửa tin nhắn thất bại", null));
            }

            @Override
            public void onFailure(Call<MessageResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<PageResponse<MessageResponseDTO>>> searchMessages(String keyword, Long conversationId, int page, int size) {
        MutableLiveData<Resource<PageResponse<MessageResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        messageApiService.searchMessages(keyword, conversationId, page, size).enqueue(new Callback<PageResponse<MessageResponseDTO>>() {
            @Override
            public void onResponse(Call<PageResponse<MessageResponseDTO>> call, Response<PageResponse<MessageResponseDTO>> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Tìm kiếm tin nhắn thất bại", null));
            }

            @Override
            public void onFailure(Call<PageResponse<MessageResponseDTO>> call, Throwable t) {
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
}
