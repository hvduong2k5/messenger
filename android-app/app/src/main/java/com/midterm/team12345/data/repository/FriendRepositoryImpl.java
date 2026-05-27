package com.midterm.team12345.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.local.database.MessengerDatabase;
import com.midterm.team12345.data.local.entity.UserEntity;
import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.data.remote.api.FriendApiService;
import com.midterm.team12345.data.remote.dto.response.FriendRequestResponseDTO;
import com.midterm.team12345.data.remote.dto.response.FriendshipStatusResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;
import com.midterm.team12345.domain.repository.FriendRepository;
import com.midterm.team12345.utils.Resource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendRepositoryImpl implements FriendRepository {
    private static FriendRepositoryImpl instance;
    private final FriendApiService friendApiService;
    private final MessengerDatabase database;

    private FriendRepositoryImpl(Context context, FriendApiService friendApiService) {
        this.friendApiService = friendApiService;
        this.database = MessengerDatabase.getInstance(context);
    }

    public static synchronized FriendRepositoryImpl getInstance(Context context) {
        if (instance == null) {
            instance = new FriendRepositoryImpl(context, RetrofitClient.getFriendApiService(context));
        }
        return instance;
    }

    @Override
    public LiveData<Resource<Void>> sendFriendRequest(Long receiverId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        friendApiService.sendFriendRequest(receiverId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    data.setValue(Resource.success(null));
                } else {
                    data.setValue(Resource.error("Gửi lời mời kết bạn thất bại", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> acceptFriendRequest(Long requestId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        friendApiService.acceptFriendRequest(requestId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    data.setValue(Resource.success(null));
                } else {
                    data.setValue(Resource.error("Chấp nhận kết bạn thất bại", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> rejectFriendRequest(Long requestId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        friendApiService.rejectFriendRequest(requestId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    data.setValue(Resource.success(null));
                } else {
                    data.setValue(Resource.error("Từ chối kết bạn thất bại", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> removeFriend(Long friendId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        friendApiService.removeFriend(friendId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    data.setValue(Resource.success(null));
                } else {
                    data.setValue(Resource.error("Hủy kết bạn thất bại", null));
                }
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
        return removeFriend(friendId);
    }

    @Override
    public LiveData<Resource<List<UserResponseDTO>>> getFriends() {
        MutableLiveData<Resource<List<UserResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        friendApiService.getFriends().enqueue(new Callback<List<UserResponseDTO>>() {
            @Override
            public void onResponse(Call<List<UserResponseDTO>> call, Response<List<UserResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveFriendsToLocal(response.body());
                    data.setValue(Resource.success(response.body()));
                } else {
                    String errorMsg = "Không thể lấy danh sách bạn bè (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    data.setValue(Resource.error(errorMsg, null));
                }
            }

            @Override
            public void onFailure(Call<List<UserResponseDTO>> call, Throwable t) {
                data.setValue(Resource.error("Lỗi kết nối: " + t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<List<UserResponseDTO>>> getFriendsList() {
        return getFriends();
    }

    private void saveFriendsToLocal(List<UserResponseDTO> list) {
        new Thread(() -> {
            try {
                java.util.List<UserEntity> entities = new java.util.ArrayList<>();
                for (UserResponseDTO dto : list) {
                    UserEntity entity = new UserEntity();
                    entity.setId(dto.getId());
                    entity.setUsername(dto.getUsername());
                    entity.setEmail(dto.getEmail());
                    entity.setAvatarUrl(dto.getAvatarUrl());
                    entity.setBio(dto.getStatus());
                    entity.setIsOnline(dto.getIsOnline() != null && dto.getIsOnline());
                    entity.setIsFriend(true);
                    entities.add(entity);
                }
                database.userDao().insertUsers(entities);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public LiveData<Resource<List<FriendRequestResponseDTO>>> getPendingRequests() {
        MutableLiveData<Resource<List<FriendRequestResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        friendApiService.getPendingRequests().enqueue(new Callback<List<FriendRequestResponseDTO>>() {
            @Override
            public void onResponse(Call<List<FriendRequestResponseDTO>> call, Response<List<FriendRequestResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(Resource.success(response.body()));
                } else {
                    String errorMsg = "Không thể lấy danh sách lời mời (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    data.setValue(Resource.error(errorMsg, null));
                }
            }

            @Override
            public void onFailure(Call<List<FriendRequestResponseDTO>> call, Throwable t) {
                data.setValue(Resource.error("Lỗi kết nối: " + t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<Void>> cancelFriendRequest(Long receiverId) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        friendApiService.cancelFriendRequest(receiverId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    data.setValue(Resource.success(null));
                } else {
                    data.setValue(Resource.error("Hủy yêu cầu kết bạn thất bại", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<FriendshipStatusResponseDTO>> checkFriendshipStatus(Long userId) {
        MutableLiveData<Resource<FriendshipStatusResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        friendApiService.checkFriendshipStatus(userId).enqueue(new Callback<FriendshipStatusResponseDTO>() {
            @Override
            public void onResponse(Call<FriendshipStatusResponseDTO> call, Response<FriendshipStatusResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(Resource.success(response.body()));
                } else {
                    String errorMsg = "Không thể kiểm tra trạng thái bạn bè (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    data.setValue(Resource.error(errorMsg, null));
                }
            }

            @Override
            public void onFailure(Call<FriendshipStatusResponseDTO> call, Throwable t) {
                data.setValue(Resource.error("Lỗi kết nối: " + t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<List<UserEntity>> searchFriendsLocally(String query) {
        return database.userDao().searchFriendsLocally(query);
    }
}
