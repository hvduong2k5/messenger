package com.midterm.team12345.ui.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.dto.response.FriendshipStatus;
import com.midterm.team12345.data.remote.dto.response.UserSearchResponseDTO;
import com.midterm.team12345.databinding.ItemFriendRequestBinding;
import com.midterm.team12345.utils.NavigationUtils;

public class UserSearchAdapter extends ListAdapter<UserSearchResponseDTO, UserSearchAdapter.ViewHolder> {

    private final OnUserSearchActionListener listener;

    public interface OnUserSearchActionListener {
        void onAddFriend(Long userId);
        void onMessageClick(UserSearchResponseDTO user);
        void onAcceptRequest(Long userId);
        void onRejectRequest(Long userId);
        void onCancelRequest(Long userId);
        void onUnfriend(Long userId);
    }

    public UserSearchAdapter(OnUserSearchActionListener listener) {
        super(new DiffUtil.ItemCallback<UserSearchResponseDTO>() {
            @Override
            public boolean areItemsTheSame(@NonNull UserSearchResponseDTO oldItem, @NonNull UserSearchResponseDTO newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull UserSearchResponseDTO oldItem, @NonNull UserSearchResponseDTO newItem) {
                return oldItem.getUsername().equals(newItem.getUsername()) &&
                       java.util.Objects.equals(oldItem.getFriendshipStatus(), newItem.getFriendshipStatus()) &&
                       (oldItem.getAvatarUrl() != null && oldItem.getAvatarUrl().equals(newItem.getAvatarUrl()));
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemFriendRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFriendRequestBinding binding;

        ViewHolder(ItemFriendRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(UserSearchResponseDTO user) {
            binding.tvName.setText(user.getUsername());
            
            // Complete Glide Avatar URL
            Glide.with(binding.ivAvatar.getContext())
                    .load(com.midterm.team12345.utils.ImageUtils.optimizeAvatarUrl(user.getAvatarUrl()))
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .fallback(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(binding.ivAvatar);

            // Navigate to profile on avatar or name click
            View.OnClickListener toProfile = v -> NavigationUtils.navigateToProfile(v.getContext(), user.getId());
            binding.ivAvatar.setOnClickListener(toProfile);
            binding.tvName.setOnClickListener(toProfile);

            FriendshipStatus status = user.getFriendshipStatus();
            if (status == null) {
                status = FriendshipStatus.STRANGER;
            }

            binding.btnConfirm.setEnabled(true);
            binding.btnConfirm.setVisibility(View.VISIBLE);
            binding.btnDelete.setVisibility(View.GONE);

            switch (status) {
                case STRANGER:
                    binding.btnConfirm.setText("Thêm bạn bè");
                    binding.btnConfirm.setOnClickListener(v -> listener.onAddFriend(user.getId()));
                    break;
                case FRIEND:
                    binding.btnConfirm.setText("Nhắn tin");
                    binding.btnConfirm.setOnClickListener(v -> listener.onMessageClick(user));
                    
                    binding.btnDelete.setVisibility(View.VISIBLE);
                    binding.btnDelete.setText("Hủy kết bạn");
                    binding.btnDelete.setOnClickListener(v -> listener.onUnfriend(user.getId()));
                    break;
                case SENDER_PENDING:
                    binding.btnConfirm.setText("Đã gửi lời mời");
                    binding.btnConfirm.setEnabled(false);
                    binding.btnDelete.setVisibility(View.VISIBLE);
                    binding.btnDelete.setText("Hủy");
                    binding.btnDelete.setOnClickListener(v -> listener.onCancelRequest(user.getId()));
                    break;
                case RECEIVER_PENDING:
                    binding.btnConfirm.setText("Chấp nhận");
                    binding.btnConfirm.setOnClickListener(v -> listener.onAcceptRequest(user.getId()));
                    
                    binding.btnDelete.setVisibility(View.VISIBLE);
                    binding.btnDelete.setText("Từ chối");
                    binding.btnDelete.setOnClickListener(v -> listener.onRejectRequest(user.getId()));
                    break;
                case SELF:
                    binding.btnConfirm.setVisibility(View.GONE);
                    break;
            }
        }
    }
}
