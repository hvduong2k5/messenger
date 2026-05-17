package com.midterm.team12345.ui.friends;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.team12345.databinding.ItemFriendRequestBinding;
import com.midterm.team12345.network.UserResponse;

public class FriendRequestAdapter extends ListAdapter<UserResponse, FriendRequestAdapter.ViewHolder> {

    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onConfirm(Long userId);
        void onDelete(Long userId);
    }

    public FriendRequestAdapter(OnRequestActionListener listener) {
        super(new DiffUtil.ItemCallback<UserResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull UserResponse oldItem, @NonNull UserResponse newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull UserResponse oldItem, @NonNull UserResponse newItem) {
                return oldItem.getUsername().equals(newItem.getUsername()) &&
                       oldItem.getAvatarUrl().equals(newItem.getAvatarUrl());
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

        void bind(UserResponse user) {
            binding.tvName.setText(user.getUsername());
            // Glide.with(binding.ivAvatar).load(user.getAvatarUrl()).into(binding.ivAvatar);
            
            binding.btnConfirm.setOnClickListener(v -> listener.onConfirm(user.getId()));
            binding.btnDelete.setOnClickListener(v -> listener.onDelete(user.getId()));
        }
    }
}
