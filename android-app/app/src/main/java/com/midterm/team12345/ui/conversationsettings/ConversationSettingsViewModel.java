package com.midterm.team12345.ui.conversationsettings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.midterm.team12345.utils.Resource;

public class ConversationSettingsViewModel extends ViewModel {

    private final MutableLiveData<Resource<Void>> _actionState = new MutableLiveData<>();
    public LiveData<Resource<Void>> getActionState() {
        return _actionState;
    }

    public void muteNotifications(boolean isMuted) {
        _actionState.setValue(Resource.loading(null));
        // Mock API call
        new android.os.Handler().postDelayed(() -> {
            _actionState.setValue(Resource.success(null));
        }, 1000);
    }

    public void leaveConversation(Long conversationId) {
        _actionState.setValue(Resource.loading(null));
        // Mock API call
        new android.os.Handler().postDelayed(() -> {
            _actionState.setValue(Resource.success(null));
        }, 1000);
    }

    public void blockUser(Long userId) {
        _actionState.setValue(Resource.loading(null));
        // Mock API call
        new android.os.Handler().postDelayed(() -> {
            _actionState.setValue(Resource.success(null));
        }, 1000);
    }

    public void deleteHistory(Long conversationId) {
        _actionState.setValue(Resource.loading(null));
        // Mock API call
        new android.os.Handler().postDelayed(() -> {
            _actionState.setValue(Resource.success(null));
        }, 1000);
    }
}
