package com.midterm.team12345.ui.chatdetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.dto.response.AttachmentResponseDTO;
import com.midterm.team12345.data.remote.dto.response.MessageResponseDTO;
import com.midterm.team12345.databinding.ItemMessageReceivedBinding;
import com.midterm.team12345.databinding.ItemMessageSentBinding;
import com.midterm.team12345.utils.NavigationUtils;

import java.util.List;

public class MessageAdapter extends ListAdapter<MessageResponseDTO, RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    private Long currentUserId;
    private String recipientAvatarUrl;

    public MessageAdapter(Long currentUserId) {
        super(new DiffUtil.ItemCallback<MessageResponseDTO>() {
            @Override
            public boolean areItemsTheSame(@NonNull MessageResponseDTO oldItem, @NonNull MessageResponseDTO newItem) {
                if (oldItem.getMessageId() != null && newItem.getMessageId() != null) {
                    return oldItem.getMessageId().equals(newItem.getMessageId());
                }
                if (oldItem.getClientMessageId() != null && newItem.getClientMessageId() != null) {
                    return oldItem.getClientMessageId().equals(newItem.getClientMessageId());
                }
                return false;
            }
 
            @Override
            public boolean areContentsTheSame(@NonNull MessageResponseDTO oldItem, @NonNull MessageResponseDTO newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public Object getChangePayload(@NonNull MessageResponseDTO oldItem, @NonNull MessageResponseDTO newItem) {
                if (!java.util.Objects.equals(oldItem.getStatus(), newItem.getStatus())) {
                    return "STATUS_CHANGED";
                }
                return super.getChangePayload(oldItem, newItem);
            }
        });
        this.currentUserId = currentUserId;
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
        notifyDataSetChanged();
    }

    public void setRecipientAvatarUrl(String url) {
        this.recipientAvatarUrl = url;
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
        MessageResponseDTO message = getItem(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(message, recipientAvatarUrl);
        } else {
            ((ReceivedViewHolder) holder).bind(message);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            MessageResponseDTO message = getItem(position);
            if (holder instanceof SentViewHolder) {
                for (Object payload : payloads) {
                    if ("STATUS_CHANGED".equals(payload)) {
                        ((SentViewHolder) holder).bindStatus(message, recipientAvatarUrl);
                    }
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    public static String getFullUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        if (url.startsWith("http")) return url;
        return com.midterm.team12345.data.remote.RetrofitClient.getBaseUrl() + (url.startsWith("/") ? "" : "/") + url;
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageSentBinding binding;

        public SentViewHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MessageResponseDTO message, String recipientAvatar) {
            // 1. Handle Text content
            if (message.getDeleted() != null && message.getDeleted()) {
                binding.tvMessageContent.setText("Tin nhắn đã bị thu hồi");
                binding.tvMessageContent.setAlpha(0.6f);
                binding.tvMessageContent.setVisibility(View.VISIBLE);
                binding.rvAttachments.setVisibility(View.GONE);
            } else {
                binding.tvMessageContent.setAlpha(1.0f);
                String content = message.getContent();
                if (content == null || content.trim().isEmpty()) {
                    binding.tvMessageContent.setVisibility(View.GONE);
                } else {
                    binding.tvMessageContent.setVisibility(View.VISIBLE);
                    binding.tvMessageContent.setText(content);
                }

                // 2. Handle Attachments (Horizontal RecyclerView)
                List<AttachmentResponseDTO> attachments = message.getAttachments();
                if (attachments != null && !attachments.isEmpty()) {
                    binding.rvAttachments.setVisibility(View.VISIBLE);
                    AttachmentAdapter attachmentAdapter = new AttachmentAdapter();
                    binding.rvAttachments.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                    binding.rvAttachments.setAdapter(attachmentAdapter);
                    attachmentAdapter.setAttachments(attachments);
                } else {
                    binding.rvAttachments.setVisibility(View.GONE);
                }
            }

            // 3. Handle Status Indicator (Sending, Sent, Read, Delivered)
            bindStatus(message, recipientAvatar);
        }

        public void bindStatus(MessageResponseDTO message, String recipientAvatar) {
            String status = message.getStatus();
            binding.viewStatusSending.setVisibility(View.GONE);
            binding.ivStatusSent.setVisibility(View.GONE);
            binding.ivStatusRead.setVisibility(View.GONE);

            if ("SENDING".equalsIgnoreCase(status)) {
                binding.viewStatusSending.setVisibility(View.VISIBLE);
            } else if ("READ".equalsIgnoreCase(status)) {
                binding.ivStatusRead.setVisibility(View.VISIBLE);
                Glide.with(binding.ivStatusRead.getContext())
                        .load(getFullUrl(recipientAvatar))
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .fallback(R.drawable.ic_avatar_placeholder)
                        .error(R.drawable.ic_avatar_placeholder)
                        .into(binding.ivStatusRead);
            } else if ("DELIVERED".equalsIgnoreCase(status)) {
                binding.ivStatusSent.setVisibility(View.VISIBLE);
                binding.ivStatusSent.setImageResource(R.drawable.ic_tick_double);
                binding.ivStatusSent.setImageTintList(null);
            } else { // SENT or other
                binding.ivStatusSent.setVisibility(View.VISIBLE);
                binding.ivStatusSent.setImageResource(R.drawable.ic_tick_single);
                binding.ivStatusSent.setImageTintList(null);
            }
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageReceivedBinding binding;

        public ReceivedViewHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MessageResponseDTO message) {
            // Set sender name
            if (message.getSenderUsername() != null && !message.getSenderUsername().trim().isEmpty()) {
                binding.tvSenderName.setText(message.getSenderUsername());
                binding.tvSenderName.setVisibility(View.VISIBLE);
            } else {
                binding.tvSenderName.setVisibility(View.GONE);
            }

            if (message.getDeleted() != null && message.getDeleted()) {
                binding.tvMessageContent.setText("Tin nhắn đã bị thu hồi");
                binding.tvMessageContent.setAlpha(0.6f);
                binding.rvAttachments.setVisibility(View.GONE);
            } else {
                binding.tvMessageContent.setAlpha(1.0f);
                String content = message.getContent();
                if (content == null || content.trim().isEmpty()) {
                    binding.tvMessageContent.setVisibility(View.GONE);
                } else {
                    binding.tvMessageContent.setVisibility(View.VISIBLE);
                    binding.tvMessageContent.setText(content);
                }

                // Handle Attachments
                List<AttachmentResponseDTO> attachments = message.getAttachments();
                if (attachments != null && !attachments.isEmpty()) {
                    binding.rvAttachments.setVisibility(View.VISIBLE);
                    AttachmentAdapter attachmentAdapter = new AttachmentAdapter();
                    binding.rvAttachments.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                    binding.rvAttachments.setAdapter(attachmentAdapter);
                    attachmentAdapter.setAttachments(attachments);
                } else {
                    binding.rvAttachments.setVisibility(View.GONE);
                }
            }

            Glide.with(binding.ivAvatar.getContext())
                    .load(getFullUrl(message.getSenderAvatarUrl()))
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .fallback(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(binding.ivAvatar);

            // Navigate to profile on avatar click
            binding.ivAvatar.setOnClickListener(v -> {
                NavigationUtils.navigateToProfile(v.getContext(), message.getSenderId());
            });
        }
    }
}
