package com.midterm.team12345.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.api.ConversationApiService;
import com.midterm.team12345.data.remote.dto.request.ConversationRequestDTO;
import com.midterm.team12345.data.remote.dto.request.ConversationUpdateDTO;
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.data.remote.dto.response.PageResponse;
import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.utils.Resource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationRepositoryImpl implements ConversationRepository {
    private final ConversationApiService apiService;

    public ConversationRepositoryImpl(ConversationApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public LiveData<Resource<PageResponse<ConversationResponseDTO>>> getConversations(int page, int size) {
        MutableLiveData<Resource<PageResponse<ConversationResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.getConversations(page, size).enqueue(new Callback<PageResponse<ConversationResponseDTO>>() {
            @Override
            public void onResponse(Call<PageResponse<ConversationResponseDTO>> call, Response<PageResponse<ConversationResponseDTO>> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Failed to fetch conversations", null));
            }
            @Override
            public void onFailure(Call<PageResponse<ConversationResponseDTO>> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
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
    public LiveData<Resource<Void>> createConversation(ConversationRequestDTO request) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.createConversation(request).enqueue(new Callback<ConversationResponseDTO>() {
            @Override
            public void onResponse(Call<ConversationResponseDTO> call, Response<ConversationResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
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
        // Implementation similar to above...
        return new MutableLiveData<>();
    }

    @Override
    public LiveData<Resource<Void>> removeParticipant(Long conversationId, Long userId) {
        // Implementation...
        return new MutableLiveData<>();
    }

    @Override
    public LiveData<Resource<Void>> updateConversation(Long id, ConversationUpdateDTO request) {
        // Implementation...
        return new MutableLiveData<>();
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
}
