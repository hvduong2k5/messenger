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

    private ConversationRepositoryImpl(ConversationApiService apiService) {
        this.apiService = apiService;
    }

    public static synchronized ConversationRepositoryImpl getInstance(Application application) {
        if (instance == null) {
            instance = new ConversationRepositoryImpl(RetrofitClient.getConversationApiService(application));
        }
        return instance;
    }

    @Override
    public LiveData<Resource<PageResponse<ConversationResponseDTO>>> getConversations(int page, int size) {
        MutableLiveData<Resource<PageResponse<ConversationResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.getConversations(page, size).enqueue(new Callback<PageResponse<ConversationResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<ConversationResponseDTO>> call, @NonNull Response<PageResponse<ConversationResponseDTO>> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Failed to load conversations", null));
            }
            @Override
            public void onFailure(@NonNull Call<PageResponse<ConversationResponseDTO>> call, @NonNull Throwable t) {
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
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Failed to create", null));
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
        // Typically leave is removing oneself. Need current user ID.
        // For now, assuming it's handled or we need a specific API.
        // Usually, removing oneself from participants works if the backend allows it.
        return null; 
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
