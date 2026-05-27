package com.midterm.team12345.ui.conversationsettings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.dto.request.ConversationUpdateDTO;
import com.midterm.team12345.domain.repository.ConversationRepository;
import com.midterm.team12345.domain.repository.FriendRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;

public class ConversationSettingsViewModel extends BaseViewModel {

    private final ConversationRepository conversationRepository;
    private final FriendRepository friendRepository;
    
    private final MutableLiveData<Resource<Void>> _actionState = new MutableLiveData<>();
    public final LiveData<Resource<Void>> actionState = _actionState;

    public ConversationSettingsViewModel(ConversationRepository conversationRepository, FriendRepository friendRepository) {
        this.conversationRepository = conversationRepository;
        this.friendRepository = friendRepository;
    }

    public LiveData<Resource<com.midterm.team12345.data.remote.dto.response.PageResponse<com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO>>> getParticipants(Long conversationId) {
        return conversationRepository.getParticipants(conversationId, null, 0, 100);
    }


    /**
     * Cập nhật thông tin hội thoại (Tên, Ảnh)
     */
    public void updateConversation(Long id, String name, String avatarUrl) {
        showLoading();
        ConversationUpdateDTO request = new ConversationUpdateDTO(name, avatarUrl);
        conversationRepository.updateConversation(id, request).observeForever(resource -> {
            if (resource.status != Resource.Status.LOADING) {
                hideLoading();
                // Chuyển đổi Resource<ConversationResponseDTO> sang Resource<Void> để báo trạng thái thành công
                if (resource.status == Resource.Status.SUCCESS) _actionState.setValue(Resource.success(null));
                else _actionState.setValue(Resource.error(resource.message, null));
            }
        });
    }

    /**
     * Rời khỏi hội thoại (Nhóm)
     */
    public void leaveGroup(Long conversationId) {
        showLoading();
        conversationRepository.leaveConversation(conversationId).observeForever(resource -> {
            if (resource.status != Resource.Status.LOADING) {
                hideLoading();
                _actionState.setValue(resource);
            }
        });
    }

    /**
     * Hủy kết bạn (Dành cho chat 1-1)
     */
    public void unfriend(Long friendId) {
        showLoading();
        friendRepository.unfriend(friendId).observeForever(resource -> {
            if (resource.status != Resource.Status.LOADING) {
                hideLoading();
                _actionState.setValue(resource);
            }
        });
    }
}
