package com.midterm.team12345.ui.friends;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.data.remote.dto.response.FriendshipStatus;
import com.midterm.team12345.data.remote.dto.response.UserSearchResponseDTO;
import com.midterm.team12345.databinding.ItemGlobalUserSearchBinding;
import com.midterm.team12345.utils.NavigationUtils;

public class GlobalUserSearchAdapter extends ListAdapter<UserSearchResponseDTO, GlobalUserSearchAdapter.ViewHolder> {

    private final OnUserActionListener listener;

    public interface OnUserActionListener {
        void onAddClick(UserSearchResponseDTO user);
        void onCancelClick(UserSearchResponseDTO user);
        void onUnfriendClick(UserSearchResponseDTO user);
        void onAcceptClick(UserSearchResponseDTO user);
        void onRejectClick(UserSearchResponseDTO user);
    }

    public GlobalUserSearchAdapter(OnUserActionListener listener) {
        super(new DiffUtil.ItemCallback<UserSearchResponseDTO>() {
            @Override
            public boolean areItemsTheSame(@NonNull UserSearchResponseDTO oldItem, @NonNull UserSearchResponseDTO newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull UserSearchResponseDTO oldItem, @NonNull UserSearchResponseDTO newItem) {
                return oldItem.getFriendshipStatus() == newItem.getFriendshipStatus() &&
                       oldItem.getUsername().equals(newItem.getUsername());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemGlobalUserSearchBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserSearchResponseDTO user = getItem(position);
        holder.binding.tvName.setText(user.getUsername()); // Or full name if available
        holder.binding.tvUsername.setText("@" + user.getUsername());

        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.startsWith("http")) {
            avatarUrl = RetrofitClient.getBaseUrl() + (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
        }
        Glide.with(holder.itemView.getContext())
                .load(avatarUrl)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder)
                .into(holder.binding.ivAvatar);

        android.view.View.OnClickListener toProfile = v -> {
            NavigationUtils.navigateToProfile(holder.itemView.getContext(), user.getId());
        };
        holder.binding.ivAvatar.setOnClickListener(toProfile);
        holder.binding.tvName.setOnClickListener(toProfile);
        holder.binding.tvUsername.setOnClickListener(toProfile);

        if (user.getFriendshipStatus() == FriendshipStatus.RECEIVER_PENDING) {
            holder.binding.btnAction.setVisibility(android.view.View.GONE);
            holder.binding.layoutRequestActions.setVisibility(android.view.View.VISIBLE);
            holder.binding.btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAcceptClick(user);
            });
            holder.binding.btnDecline.setOnClickListener(v -> {
                if (listener != null) listener.onRejectClick(user);
            });
        } else {
            holder.binding.btnAction.setVisibility(android.view.View.VISIBLE);
            holder.binding.layoutRequestActions.setVisibility(android.view.View.GONE);

            if (user.getFriendshipStatus() == FriendshipStatus.SENDER_PENDING) {
                holder.binding.btnAction.setText("Cancel");
                holder.binding.btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
                holder.binding.btnAction.setTextColor(Color.parseColor("#757575"));
                holder.binding.btnAction.setOnClickListener(v -> {
                    if (listener != null) listener.onCancelClick(user);
                });
            } else if (user.getFriendshipStatus() == FriendshipStatus.STRANGER) {
                holder.binding.btnAction.setText("Add");
                holder.binding.btnAction.setBackgroundTintList(ColorStateList.valueOf(holder.itemView.getContext().getResources().getColor(R.color.messenger_blue)));
                holder.binding.btnAction.setTextColor(Color.WHITE);
                holder.binding.btnAction.setOnClickListener(v -> {
                    if (listener != null) listener.onAddClick(user);
                });
            } else if (user.getFriendshipStatus() == FriendshipStatus.FRIEND) {
                holder.binding.btnAction.setText("Unfriend");
                holder.binding.btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFD2D2")));
                holder.binding.btnAction.setTextColor(Color.parseColor("#D32F2F"));
                holder.binding.btnAction.setOnClickListener(v -> {
                    if (listener != null) listener.onUnfriendClick(user);
                });
            } else {
                // Self
                holder.binding.btnAction.setVisibility(android.view.View.GONE);
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemGlobalUserSearchBinding binding;

        ViewHolder(ItemGlobalUserSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
