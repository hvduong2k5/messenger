package com.midterm.team12345.ui.creategroup;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;
import com.midterm.team12345.databinding.ItemSelectedMemberBinding;

import java.util.ArrayList;
import java.util.List;

public class SelectedMemberAdapter extends RecyclerView.Adapter<SelectedMemberAdapter.ViewHolder> {

    private List<UserResponseDTO> selectedUsers = new ArrayList<>();
    private final OnRemoveClickListener listener;

    public interface OnRemoveClickListener {
        void onRemoveClick(UserResponseDTO user);
    }

    public SelectedMemberAdapter(OnRemoveClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<UserResponseDTO> users) {
        this.selectedUsers = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSelectedMemberBinding binding = ItemSelectedMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserResponseDTO user = selectedUsers.get(position);
        
        // Dùng username thay cho fullName. Lấy phần đầu của username làm tên hiển thị rút gọn
        String displayName = user.getUsername();
        if (displayName != null && displayName.contains(" ")) {
            displayName = displayName.split(" ")[0];
        }
        holder.binding.tvFirstName.setText(displayName);
        
        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.startsWith("http")) {
            avatarUrl = com.midterm.team12345.data.remote.RetrofitClient.getBaseUrl() + (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
        }
        Glide.with(holder.itemView.getContext())
                .load(avatarUrl)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .fallback(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder)
                .into(holder.binding.ivAvatar);
        
        holder.binding.ivRemove.setOnClickListener(v -> listener.onRemoveClick(user));
    }

    @Override
    public int getItemCount() {
        return selectedUsers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemSelectedMemberBinding binding;

        ViewHolder(ItemSelectedMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
