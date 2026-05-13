package com.midterm.team12345.data.repository;

import androidx.lifecycle.LiveData;
import com.midterm.team12345.data.dto.ConversationResponse;
import com.midterm.team12345.util.Resource;
import java.util.List;

public interface ChatRepository {
    LiveData<Resource<List<ConversationResponse>>> getConversations();
}
