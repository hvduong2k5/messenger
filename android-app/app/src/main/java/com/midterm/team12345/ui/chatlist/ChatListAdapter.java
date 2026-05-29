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
import com.midterm.team12345.databinding.ItemConversationBinding;
import com.midterm.team12345.domain.model.Conversation;

public class ChatListAdapter extends ListAdapter<Conversation, ChatListAdapter.ViewHolder> {

    private final OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ChatListAdapter(OnConversationClickListener listener) {
        super(new DiffUtil.ItemCallback<Conversation>() {
            @Override
            public boolean areItemsTheSame(@NonNull Conversation oldItem, @NonNull Conversation newItem) {
                return oldItem.getConversationId().equals(newItem.getConversationId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Conversation oldItem, @NonNull Conversation newItem) {
                return java.util.Objects.equals(oldItem.getLastMessageCreatedAt(), newItem.getLastMessageCreatedAt()) 
                       && java.util.Objects.equals(oldItem.getUnreadCount(), newItem.getUnreadCount());
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

        public void bind(Conversation conversation, OnConversationClickListener listener) {
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

            Glide.with(binding.ivConversationAvatar.getContext())
                    .load(com.midterm.team12345.utils.ImageUtils.optimizeAvatarUrl(conversation.getAvatarUrl()))
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .fallback(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(binding.ivConversationAvatar);

            binding.getRoot().setOnClickListener(v -> listener.onConversationClick(conversation));
        }
    }
}
