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
import com.midterm.team12345.databinding.ItemFriendBinding;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;

import java.time.LocalDateTime;

public class FriendAdapter extends ListAdapter<UserResponseDTO, FriendAdapter.ViewHolder> {

    private final OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(UserResponseDTO user);
        void onCallClick(UserResponseDTO user);
        void onVideoCallClick(UserResponseDTO user);
        void onUnfriendClick(UserResponseDTO user);
    }

    public FriendAdapter(OnFriendClickListener listener) {
        super(new DiffUtil.ItemCallback<UserResponseDTO>() {
            @Override
            public boolean areItemsTheSame(@NonNull UserResponseDTO oldItem, @NonNull UserResponseDTO newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull UserResponseDTO oldItem, @NonNull UserResponseDTO newItem) {
                return oldItem.getUsername().equals(newItem.getUsername()) &&
                       (oldItem.getIsOnline() != null && oldItem.getIsOnline().equals(newItem.getIsOnline())) &&
                       (oldItem.getAvatarUrl() != null && oldItem.getAvatarUrl().equals(newItem.getAvatarUrl()));
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

        void bind(UserResponseDTO user) {
            binding.tvName.setText(user.getUsername());

            // Handle Presence and Last Seen status
            if (user.getIsOnline() != null && user.getIsOnline()) {
                binding.ivPresenceStatus.setVisibility(View.VISIBLE);
                binding.tvStatus.setText("Đang hoạt động");
            } else {
                binding.ivPresenceStatus.setVisibility(View.GONE);
                binding.tvStatus.setText(formatLastSeenString(user.getLastSeen()));
            }

            // Load Avatar using Glide
            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.startsWith("http")) {
                avatarUrl = com.midterm.team12345.data.remote.RetrofitClient.getBaseUrl() + 
                            (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
            }

            Glide.with(binding.ivAvatar.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(binding.ivAvatar);

            binding.getRoot().setOnClickListener(v -> listener.onFriendClick(user));
            binding.ivCall.setOnClickListener(v -> listener.onCallClick(user));
            binding.ivVideoCall.setOnClickListener(v -> listener.onVideoCallClick(user));
            binding.ivUnfriend.setOnClickListener(v -> listener.onUnfriendClick(user));
        }

        private String formatLastSeenString(String lastSeenStr) {
            if (lastSeenStr == null || lastSeenStr.trim().isEmpty()) return "Ngoại tuyến";
            try {
                LocalDateTime ldt = LocalDateTime.parse(lastSeenStr);
                return formatLastSeen(ldt);
            } catch (Exception e) {
                try {
                    java.time.ZonedDateTime zdt = java.time.ZonedDateTime.parse(lastSeenStr);
                    return formatLastSeen(zdt.toLocalDateTime());
                } catch (Exception e2) {
                    return "Hoạt động gần đây";
                }
            }
        }

        private String formatLastSeen(LocalDateTime lastSeen) {
            if (lastSeen == null) return "Ngoại tuyến";
            try {
                java.time.Duration duration = java.time.Duration.between(lastSeen, LocalDateTime.now());
                long minutes = duration.toMinutes();
                if (minutes < 1) {
                    return "Vừa mới hoạt động";
                } else if (minutes < 60) {
                    return "Hoạt động " + minutes + " phút trước";
                } else {
                    long hours = duration.toHours();
                    if (hours < 24) {
                        return "Hoạt động " + hours + " giờ trước";
                    } else {
                        long days = duration.toDays();
                        if (days < 7) {
                            return "Hoạt động " + days + " ngày trước";
                        } else {
                            return "Hoạt động ngày " + lastSeen.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        }
                    }
                }
            } catch (Exception e) {
                return "Ngoại tuyến";
            }
        }
    }
}
