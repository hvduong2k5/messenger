package com.midterm.team12345.ui.chatdetail;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.dto.response.MessageResponse;
import com.midterm.team12345.databinding.ItemMessageReceivedBinding;
import com.midterm.team12345.databinding.ItemMessageSentBinding;

public class MessageAdapter extends ListAdapter<MessageResponse, RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    private Long currentUserId;

    public MessageAdapter(Long currentUserId) {
        super(new DiffUtil.ItemCallback<MessageResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull MessageResponse oldItem, @NonNull MessageResponse newItem) {
                return oldItem.getMessageId().equals(newItem.getMessageId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull MessageResponse oldItem, @NonNull MessageResponse newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.currentUserId = currentUserId;
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getSenderId().equals(currentUserId)) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            ItemMessageSentBinding binding = ItemMessageSentBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new SentViewHolder(binding);
        } else {
            ItemMessageReceivedBinding binding = ItemMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ReceivedViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageResponse message = getItem(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(message);
        } else {
            ((ReceivedViewHolder) holder).bind(message);
        }
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageSentBinding binding;

        public SentViewHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MessageResponse message) {
            if (message.getDeleted() != null && message.getDeleted()) {
                binding.tvMessageContent.setText("Tin nhắn đã bị thu hồi");
                binding.tvMessageContent.setAlpha(0.6f);
            } else {
                binding.tvMessageContent.setText(message.getContent());
                binding.tvMessageContent.setAlpha(1.0f);
            }
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageReceivedBinding binding;

        public ReceivedViewHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MessageResponse message) {
            if (message.getDeleted() != null && message.getDeleted()) {
                binding.tvMessageContent.setText("Tin nhắn đã bị thu hồi");
                binding.tvMessageContent.setAlpha(0.6f);
            } else {
                binding.tvMessageContent.setText(message.getContent());
                binding.tvMessageContent.setAlpha(1.0f);
            }

            Glide.with(binding.ivAvatar.getContext())
                    .load(message.getSenderAvatarUrl())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(binding.ivAvatar);
        }
    }
}
