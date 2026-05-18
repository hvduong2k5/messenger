package com.midterm.team12345.ui.friends;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.databinding.ItemFriendRequestBinding;
import com.midterm.team12345.data.remote.dto.response.FriendRequestResponseDTO;

public class FriendRequestAdapter extends ListAdapter<FriendRequestResponseDTO, FriendRequestAdapter.ViewHolder> {

    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onConfirm(Long requestId);
        void onDelete(Long requestId);
    }

    public FriendRequestAdapter(OnRequestActionListener listener) {
        super(new DiffUtil.ItemCallback<FriendRequestResponseDTO>() {
            @Override
            public boolean areItemsTheSame(@NonNull FriendRequestResponseDTO oldItem, @NonNull FriendRequestResponseDTO newItem) {
                if (oldItem.getSenderId() != null && newItem.getSenderId() != null) {
                    return oldItem.getSenderId().equals(newItem.getSenderId());
                }
                return false;
            }

            @Override
            public boolean areContentsTheSame(@NonNull FriendRequestResponseDTO oldItem, @NonNull FriendRequestResponseDTO newItem) {
                String oldName = oldItem.getSenderUsername() != null ? oldItem.getSenderUsername() : "";
                String newName = newItem.getSenderUsername() != null ? newItem.getSenderUsername() : "";
                String oldAvatar = oldItem.getSenderAvatarUrl() != null ? oldItem.getSenderAvatarUrl() : "";
                String newAvatar = newItem.getSenderAvatarUrl() != null ? newItem.getSenderAvatarUrl() : "";
                return oldName.equals(newName) && oldAvatar.equals(newAvatar);
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

        void bind(FriendRequestResponseDTO request) {
            binding.tvName.setText(request.getSenderUsername());

            // Load Avatar using Glide
            String avatarUrl = request.getSenderAvatarUrl();
            if (avatarUrl != null && !avatarUrl.startsWith("http")) {
                avatarUrl = com.midterm.team12345.data.remote.RetrofitClient.getBaseUrl() + 
                            (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
            }

            Glide.with(binding.ivAvatar.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(binding.ivAvatar);
            
            binding.btnConfirm.setOnClickListener(v -> listener.onConfirm(request.getSenderId()));
            binding.btnDelete.setOnClickListener(v -> listener.onDelete(request.getSenderId()));
        }
    }
}
