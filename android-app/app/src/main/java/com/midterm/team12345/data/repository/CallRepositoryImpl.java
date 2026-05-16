package com.midterm.team12345.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.api.CallApiService;
import com.midterm.team12345.data.remote.dto.request.InitiateCallRequestDTO;
import com.midterm.team12345.data.remote.dto.request.SignalingRequestDTO;
import com.midterm.team12345.data.remote.dto.response.CallResponseDTO;
import com.midterm.team12345.data.remote.dto.response.SignalingResponseDTO;
import com.midterm.team12345.domain.repository.CallRepository;
import com.midterm.team12345.utils.Resource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallRepositoryImpl implements CallRepository {
    private final CallApiService apiService;

    public CallRepositoryImpl(CallApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public LiveData<Resource<CallResponseDTO>> initiateCall(InitiateCallRequestDTO request) {
        MutableLiveData<Resource<CallResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.initiateCall(request).enqueue(new Callback<CallResponseDTO>() {
            @Override
            public void onResponse(Call<CallResponseDTO> call, Response<CallResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Khởi tạo cuộc gọi thất bại", null));
            }
            @Override
            public void onFailure(Call<CallResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<CallResponseDTO>> answerCall(Long id) {
        MutableLiveData<Resource<CallResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.answerCall(id).enqueue(new Callback<CallResponseDTO>() {
            @Override
            public void onResponse(Call<CallResponseDTO> call, Response<CallResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Trả lời cuộc gọi thất bại", null));
            }
            @Override
            public void onFailure(Call<CallResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<CallResponseDTO>> rejectCall(Long id) {
        MutableLiveData<Resource<CallResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.rejectCall(id).enqueue(new Callback<CallResponseDTO>() {
            @Override
            public void onResponse(Call<CallResponseDTO> call, Response<CallResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Từ chối cuộc gọi thất bại", null));
            }
            @Override
            public void onFailure(Call<CallResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<CallResponseDTO>> endCall(Long id) {
        MutableLiveData<Resource<CallResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.endCall(id).enqueue(new Callback<CallResponseDTO>() {
            @Override
            public void onResponse(Call<CallResponseDTO> call, Response<CallResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Kết thúc cuộc gọi thất bại", null));
            }
            @Override
            public void onFailure(Call<CallResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<SignalingResponseDTO>> sendSignaling(Long id, SignalingRequestDTO request) {
        MutableLiveData<Resource<SignalingResponseDTO>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.sendSignaling(id, request).enqueue(new Callback<SignalingResponseDTO>() {
            @Override
            public void onResponse(Call<SignalingResponseDTO> call, Response<SignalingResponseDTO> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Gửi tín hiệu thất bại", null));
            }
            @Override
            public void onFailure(Call<SignalingResponseDTO> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<List<SignalingResponseDTO>>> getSignaling(Long id) {
        MutableLiveData<Resource<List<SignalingResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.getSignaling(id).enqueue(new Callback<List<SignalingResponseDTO>>() {
            @Override
            public void onResponse(Call<List<SignalingResponseDTO>> call, Response<List<SignalingResponseDTO>> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Lấy tín hiệu thất bại", null));
            }
            @Override
            public void onFailure(Call<List<SignalingResponseDTO>> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }

    @Override
    public LiveData<Resource<List<CallResponseDTO>>> getCallHistory() {
        MutableLiveData<Resource<List<CallResponseDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));
        apiService.getCallHistory().enqueue(new Callback<List<CallResponseDTO>>() {
            @Override
            public void onResponse(Call<List<CallResponseDTO>> call, Response<List<CallResponseDTO>> response) {
                if (response.isSuccessful()) data.setValue(Resource.success(response.body()));
                else data.setValue(Resource.error("Lấy lịch sử cuộc gọi thất bại", null));
            }
            @Override
            public void onFailure(Call<List<CallResponseDTO>> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return data;
    }
}
