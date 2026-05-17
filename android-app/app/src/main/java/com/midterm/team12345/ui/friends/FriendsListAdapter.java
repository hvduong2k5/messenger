package com.midterm.team12345.ui.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.team12345.databinding.ItemAlphabetHeaderBinding;
import com.midterm.team12345.databinding.ItemFriendBinding;
import com.midterm.team12345.network.UserResponse;

public class FriendsListAdapter extends ListAdapter<Object, RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_FRIEND = 1;

    private final OnFriendActionListener listener;

    public interface OnFriendActionListener {
        void onCall(UserResponse user);
        void onVideoCall(UserResponse user);
        void onProfileClick(UserResponse user);
    }

    protected FriendsListAdapter(OnFriendActionListener listener) {
        super(new DiffUtil.ItemCallback<Object>() {
            @Override
            public boolean areItemsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
                if (oldItem instanceof String && newItem instanceof String) {
                    return oldItem.equals(newItem);
                }
                if (oldItem instanceof UserResponse && newItem instanceof UserResponse) {
                    return ((UserResponse) oldItem).getId().equals(((UserResponse) newItem).getId());
                }
                return false;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof String ? TYPE_HEADER : TYPE_FRIEND;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(ItemAlphabetHeaderBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new FriendViewHolder(ItemFriendBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) getItem(position));
        } else {
            ((FriendViewHolder) holder).bind((UserResponse) getItem(position));
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemAlphabetHeaderBinding binding;
        HeaderViewHolder(ItemAlphabetHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(String letter) {
            binding.tvAlphabet.setText(letter);
        }
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        private final ItemFriendBinding binding;
        FriendViewHolder(ItemFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(UserResponse user) {
            binding.tvName.setText(user.getUsername());
            // Glide.with(binding.ivAvatar).load(user.getAvatarUrl()).into(binding.ivAvatar);
            binding.ivCall.setOnClickListener(v -> listener.onCall(user));
            binding.ivVideoCall.setOnClickListener(v -> listener.onVideoCall(user));
            binding.getRoot().setOnClickListener(v -> listener.onProfileClick(user));
        }
    }
}
