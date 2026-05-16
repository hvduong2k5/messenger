package com.midterm.team12345.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.api.FriendApiService;
import com.midterm.team12345.data.remote.dto.response.FriendRequestResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;
import com.midterm.team12345.domain.repository.FriendRepository;
import com.midterm.team12345.utils.Resource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendRepositoryImpl implements FriendRepository {
    private final FriendApiService friendApiService;

    public FriendRepositoryImpl(FriendApiService friendApiService) {
        this.friendApiService = friendApiService;
    }

    @Override
    public LiveData<Resource<FriendRequestResponseDTO>> sendFriendRequest(Long receiverId) {
        MutableLiveData<Resource<FriendRequestResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        friendApiService.sendFriendRequest(receiverId).enqueue(new Callback<FriendRequestResponseDTO>() {
            @Override
            public void onResponse(Call<FriendRequestResponseDTO> call, Response<FriendRequestResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Failed to send request", null));
            }
            @Override
            public void onFailure(Call<FriendRequestResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> acceptFriendRequest(Long senderId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        friendApiService.acceptFriendRequest(senderId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Failed to accept", null));
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> declineFriendRequest(Long senderId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        friendApiService.declineFriendRequest(senderId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Failed to decline", null));
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> unfriend(Long friendId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        friendApiService.unfriend(friendId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(null));
                else data.setValue(Resource.error("Failed to unfriend", null));
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<List<UserResponseDTO>>> getFriendsList() {
        MutableLiveData<Resource<List<UserResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        friendApiService.getFriendsList().enqueue(new Callback<List<UserResponseDTO>>() {
            @Override
            public void onResponse(Call<List<UserResponseDTO>> call, Response<List<UserResponseDTO>> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Failed to fetch friends", null));
            }
            @Override
            public void onFailure(Call<List<UserResponseDTO>> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<List<FriendRequestResponseDTO>>> getPendingFriendRequests() {
        MutableLiveData<Resource<List<FriendRequestResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        friendApiService.getPendingRequests().enqueue(new Callback<List<FriendRequestResponseDTO>>() {
            @Override
            public void onResponse(Call<List<FriendRequestResponseDTO>> call, Response<List<FriendRequestResponseDTO>> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Failed to fetch pending requests", null));
            }
            @Override
            public void onFailure(Call<List<FriendRequestResponseDTO>> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }
}
