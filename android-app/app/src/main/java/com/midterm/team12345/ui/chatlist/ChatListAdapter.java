package com.midterm.team12345.ui.chatlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.dto.response.ConversationResponse;
import com.midterm.team12345.databinding.ItemConversationBinding;

public class ChatListAdapter extends ListAdapter<ConversationResponse, ChatListAdapter.ViewHolder> {

    private final OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(ConversationResponse conversation);
    }

    public ChatListAdapter(OnConversationClickListener listener) {
        super(new DiffUtil.ItemCallback<ConversationResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull ConversationResponse oldItem, @NonNull ConversationResponse newItem) {
                return oldItem.getConversationId().equals(newItem.getConversationId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull ConversationResponse oldItem, @NonNull ConversationResponse newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemConversationBinding binding = ItemConversationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemConversationBinding binding;

        public ViewHolder(ItemConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ConversationResponse conversation, OnConversationClickListener listener) {
            binding.tvConversationName.setText(conversation.getConversationName());
            binding.tvLastMessage.setText(conversation.getLastMessage());
            binding.tvTimestamp.setText(conversation.getFormattedTimestamp());

            if (conversation.getUnreadCount() != null && conversation.getUnreadCount() > 0) {
                binding.badgeUnreadCount.setText(String.valueOf(conversation.getUnreadCount()));
                binding.badgeUnreadCount.setVisibility(View.VISIBLE);
            } else {
                binding.badgeUnreadCount.setVisibility(View.GONE);
            }

            // ivPresenceStatus is for Issue #142
            binding.ivPresenceStatus.setVisibility(View.GONE); 

            String avatarUrl = conversation.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.startsWith("http")) {
                avatarUrl = com.midterm.team12345.data.remote.RetrofitClient.getBaseUrl() + (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
            }

            Glide.with(binding.ivConversationAvatar.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(binding.ivConversationAvatar);

            binding.getRoot().setOnClickListener(v -> listener.onConversationClick(conversation));
        }
    }
}
