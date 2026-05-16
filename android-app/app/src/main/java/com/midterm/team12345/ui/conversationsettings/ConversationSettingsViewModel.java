package com.midterm.team12345.ui.conversationsettings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.dto.request.ConversationUpdateDTO;
import com.midterm.team12345.domain.repository.ChatRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;

public class ConversationSettingsViewModel extends BaseViewModel {

    private final ChatRepository repository;
    
    // Quản lý trạng thái chung cho các hành động (Mute, Leave, Block...)
    private final MutableLiveData<Resource<Void>> _actionState = new MutableLiveData<>();
    public final LiveData<Resource<Void>> actionState = _actionState;

    public ConversationSettingsViewModel(ChatRepository repository) {
        this.repository = repository;
    }


}
