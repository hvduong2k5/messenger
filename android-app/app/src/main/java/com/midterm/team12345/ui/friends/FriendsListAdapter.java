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
import com.midterm.team12345.databinding.ItemAlphabetHeaderBinding;
import com.midterm.team12345.databinding.ItemFriendBinding;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;

import java.time.LocalDateTime;

public class FriendsListAdapter extends ListAdapter<Object, RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_FRIEND = 1;

    private final OnFriendActionListener listener;

    public interface OnFriendActionListener {
        void onCall(UserResponseDTO user);
        void onVideoCall(UserResponseDTO user);
        void onProfileClick(UserResponseDTO user);
        void onProfileLongClick(UserResponseDTO user);
    }

    protected FriendsListAdapter(OnFriendActionListener listener) {
        super(new DiffUtil.ItemCallback<Object>() {
            @Override
            public boolean areItemsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
                if (oldItem instanceof String && newItem instanceof String) {
                    return oldItem.equals(newItem);
                }
                if (oldItem instanceof UserResponseDTO && newItem instanceof UserResponseDTO) {
                    return ((UserResponseDTO) oldItem).getId().equals(((UserResponseDTO) newItem).getId());
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
            ((FriendViewHolder) holder).bind((UserResponseDTO) getItem(position));
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
        void bind(UserResponseDTO user) {
            binding.tvName.setText(user.getUsername());
            
            // Presence status and Last Seen
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

            binding.ivCall.setOnClickListener(v -> listener.onCall(user));
            binding.ivVideoCall.setOnClickListener(v -> listener.onVideoCall(user));
            binding.getRoot().setOnClickListener(v -> listener.onProfileClick(user));
            binding.getRoot().setOnLongClickListener(v -> {
                listener.onProfileLongClick(user);
                return true;
            });
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
