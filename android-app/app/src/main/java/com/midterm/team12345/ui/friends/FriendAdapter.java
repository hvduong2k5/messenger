package com.midterm.team12345.ui.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.team12345.databinding.ItemFriendBinding;
import com.midterm.team12345.network.UserResponse;

public class FriendAdapter extends ListAdapter<UserResponse, FriendAdapter.ViewHolder> {

    private final OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(UserResponse user);
        void onCallClick(UserResponse user);
    }

    public FriendAdapter(OnFriendClickListener listener) {
        super(new DiffUtil.ItemCallback<UserResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull UserResponse oldItem, @NonNull UserResponse newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull UserResponse oldItem, @NonNull UserResponse newItem) {
                return oldItem.getUsername().equals(newItem.getUsername()) &&
                       oldItem.getStatus().equals(newItem.getStatus()) &&
                       oldItem.getAvatarUrl().equals(newItem.getAvatarUrl());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemFriendBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFriendBinding binding;

        ViewHolder(ItemFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(UserResponse user) {
            binding.tvName.setText(user.getUsername());
            binding.tvStatus.setText(user.getStatus());
            
            if ("online".equalsIgnoreCase(user.getStatus())) {
                binding.ivPresenceStatus.setVisibility(View.VISIBLE);
            } else {
                binding.ivPresenceStatus.setVisibility(View.GONE);
            }

            // Glide.with(binding.ivAvatar).load(user.getAvatarUrl()).into(binding.ivAvatar);
            
            binding.getRoot().setOnClickListener(v -> listener.onFriendClick(user));
            binding.ivCall.setOnClickListener(v -> listener.onCallClick(user));
        }
    }
}
