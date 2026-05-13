package com.midterm.team12345.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.midterm.team12345.data.dto.ConversationResponse;
import com.midterm.team12345.util.Resource;
import java.util.ArrayList;
import java.util.List;

public class ChatRepositoryImpl implements ChatRepository {

    @Override
    public LiveData<Resource<List<ConversationResponse>>> getConversations() {
        MutableLiveData<Resource<List<ConversationResponse>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        // Mocking data for demonstration
        List<ConversationResponse> mockList = new ArrayList<>();
        mockList.add(new ConversationResponse(1L, "Martin Randolph", "You: What's man!", null, System.currentTimeMillis(), 0, false, false));
        mockList.add(new ConversationResponse(2L, "Andrew Parker", "You: Ok, thanks!", null, System.currentTimeMillis() - 100000, 0, false, false));
        mockList.add(new ConversationResponse(3L, "Karen Castillo", "You: Ok, See you in To...", null, System.currentTimeMillis() - 200000, 0, false, false));
        mockList.add(new ConversationResponse(4L, "Maisy Humphrey", "Have a good day, Maisy!", null, System.currentTimeMillis() - 300000, 0, false, false));
        mockList.add(new ConversationResponse(5L, "Joshua Lawrence", "The business plan loo...", null, System.currentTimeMillis() - 400000, 2, false, false));

        data.setValue(Resource.success(mockList));
        return data;
    }
}
