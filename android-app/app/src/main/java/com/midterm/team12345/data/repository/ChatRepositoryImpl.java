package com.midterm.team12345.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.dto.ConversationResponse;
import com.midterm.team12345.data.dto.ConversationRequestDTO;
import com.midterm.team12345.data.dto.MqttMessageDTO;
import com.midterm.team12345.data.dto.UserDTO;
import com.midterm.team12345.util.Resource;
import java.util.ArrayList;
import java.util.List;

public class ChatRepositoryImpl implements ChatRepository {

    private static ChatRepositoryImpl instance;
    private final MutableLiveData<MqttMessageDTO> realTimeMessages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> connectionStatus = new MutableLiveData<>(false);

    private ChatRepositoryImpl() {}

    public static synchronized ChatRepositoryImpl getInstance() {
        if (instance == null) {
            instance = new ChatRepositoryImpl();
        }
        return instance;
    }

    @Override
    public LiveData<Resource<List<ConversationResponse>>> getConversations() {
        MutableLiveData<Resource<List<ConversationResponse>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        List<ConversationResponse> mockList = new ArrayList<>();
        mockList.add(new ConversationResponse(1L, "Martin Randolph", "You: What's man!", null, System.currentTimeMillis(), 0, false, false, false));
        mockList.add(new ConversationResponse(2L, "Andrew Parker", "You: Ok, thanks!", null, System.currentTimeMillis() - 100000, 0, false, false, false));
        mockList.add(new ConversationResponse(3L, "Karen Castillo", "You: Ok, See you in To...", null, System.currentTimeMillis() - 200000, 0, false, false, false));
        mockList.add(new ConversationResponse(4L, "Maisy Humphrey", "Have a good day, Maisy!", null, System.currentTimeMillis() - 300000, 0, false, false, false));
        mockList.add(new ConversationResponse(5L, "Joshua Lawrence", "The business plan loo...", null, System.currentTimeMillis() - 400000, 2, false, false, false));

        data.setValue(Resource.success(mockList));
        return data;
    }

    @Override
    public LiveData<Resource<List<UserDTO>>> getFriends() {
        MutableLiveData<Resource<List<UserDTO>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        List<UserDTO> friends = new ArrayList<>();
        String[] names = {"Martha Craig", "Kieron Dotson", "Zack John", "Jamie Franco", "Tabitha Potter", "Albert Flores", "Jenny Wilson"};
        for (int i = 0; i < names.length; i++) {
            UserDTO user = new UserDTO();
            user.setId((long) (i + 10));
            user.setFullName(names[i]);
            user.setUsername(names[i].toLowerCase().replace(" ", ""));
            friends.add(user);
        }

        data.setValue(Resource.success(friends));
        return data;
    }

    @Override
    public LiveData<Resource<ConversationResponse>> createConversation(ConversationRequestDTO request) {
        MutableLiveData<Resource<ConversationResponse>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        new android.os.Handler().postDelayed(() -> {
            ConversationResponse response = new ConversationResponse(
                100L, request.getName(), "Group created", null, System.currentTimeMillis(), 0, false, false, request.getIsGroup()
            );
            data.setValue(Resource.success(response));
        }, 1500);

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

    public void emitRealTimeMessage(MqttMessageDTO message) {
        realTimeMessages.postValue(message);
    }

    public void updateConnectionStatus(boolean connected) {
        connectionStatus.postValue(connected);
    }
}
