package com.midterm.team12345.ui.discover;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.dto.response.FriendshipStatus;
import com.midterm.team12345.data.remote.dto.response.UserSearchResponseDTO;
import com.midterm.team12345.databinding.ItemDiscoverUserBinding;

public class DiscoverAdapter extends ListAdapter<UserSearchResponseDTO, DiscoverAdapter.ViewHolder> {

    public interface OnUserActionListener {
        void onUserClick(Long userId);
        void onAddFriendClick(Long userId, int position);
    }

    private final OnUserActionListener listener;

    protected DiscoverAdapter(OnUserActionListener listener) {
        super(new DiffUtil.ItemCallback<UserSearchResponseDTO>() {
            @Override
            public boolean areItemsTheSame(@NonNull UserSearchResponseDTO oldItem, @NonNull UserSearchResponseDTO newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull UserSearchResponseDTO oldItem, @NonNull UserSearchResponseDTO newItem) {
                return oldItem.getFriendshipStatus() == newItem.getFriendshipStatus() &&
                        oldItem.getUsername().equals(newItem.getUsername()) &&
                        (oldItem.getAvatarUrl() != null ? oldItem.getAvatarUrl().equals(newItem.getAvatarUrl()) : newItem.getAvatarUrl() == null);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDiscoverUserBinding binding = ItemDiscoverUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemDiscoverUserBinding binding;

        public ViewHolder(ItemDiscoverUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(UserSearchResponseDTO user) {
            binding.tvFullName.setText(user.getUsername());
            binding.tvSubtitle.setText("@" + user.getUsername());

            Glide.with(binding.ivAvatar.getContext())
                    .load(com.midterm.team12345.utils.ImageUtils.optimizeAvatarUrl(user.getAvatarUrl()))
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .fallback(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(binding.ivAvatar);

            updateActionButton(user);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onUserClick(user.getId());
            });

            binding.btnAction.setOnClickListener(v -> {
                if (listener != null && user.getFriendshipStatus() == FriendshipStatus.STRANGER) {
                    listener.onAddFriendClick(user.getId(), getAdapterPosition());
                }
            });
        }

        private void updateActionButton(UserSearchResponseDTO user) {
            FriendshipStatus status = user.getFriendshipStatus();
            if (status == FriendshipStatus.SELF) {
                binding.btnAction.setVisibility(View.GONE);
                return;
            }

            binding.btnAction.setVisibility(View.VISIBLE);
            if (status == FriendshipStatus.FRIEND) {
                binding.btnAction.setText("Bạn bè");
                binding.btnAction.setEnabled(false);
                binding.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.btn_background_gray));
                binding.btnAction.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.btn_text_gray));
            } else if (status == FriendshipStatus.SENDER_PENDING) {
                binding.btnAction.setText("Đã gửi");
                binding.btnAction.setEnabled(false);
                binding.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.btn_background_gray));
                binding.btnAction.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.btn_text_gray));
            } else if (status == FriendshipStatus.RECEIVER_PENDING) {
                binding.btnAction.setText("Phản hồi");
                binding.btnAction.setEnabled(true);
                binding.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.messenger_blue));
                binding.btnAction.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            } else {
                binding.btnAction.setText("Thêm");
                binding.btnAction.setEnabled(true);
                binding.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.messenger_blue));
                binding.btnAction.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            }
        }
    }
}
