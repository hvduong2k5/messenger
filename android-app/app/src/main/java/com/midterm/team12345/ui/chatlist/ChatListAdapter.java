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
import com.midterm.team12345.data.dto.ConversationResponse;
import com.midterm.team12345.databinding.ItemConversationBinding;

public class ChatListAdapter extends ListAdapter<ConversationResponse, ChatListAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ConversationResponse conversation);
    }

    private OnItemClickListener listener;

    protected ChatListAdapter(OnItemClickListener listener) {
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
        ConversationResponse item = getItem(position);
        holder.bind(item, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemConversationBinding binding;

        public ViewHolder(ItemConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ConversationResponse conversation, OnItemClickListener listener) {
            binding.tvConversationName.setText(conversation.getConversationName());
            binding.tvLastMessage.setText(conversation.getLastMessage());
            binding.tvTimestamp.setText(conversation.getFormattedTimestamp());

            if (conversation.getUnreadCount() != null && conversation.getUnreadCount() > 0) {
                binding.badgeUnreadCount.setVisibility(View.VISIBLE);
                binding.badgeUnreadCount.setText(String.valueOf(conversation.getUnreadCount()));
            } else {
                binding.badgeUnreadCount.setVisibility(View.GONE);
            }

            Glide.with(binding.ivConversationAvatar.getContext())
                    .load(conversation.getAvatarUrl())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(binding.ivConversationAvatar);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(conversation);
                }
            });
        }
    }
}
