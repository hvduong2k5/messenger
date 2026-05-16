package com.midterm.team12345.ui.creategroup;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.team12345.data.remote.dto.response.UserDTO;
import com.midterm.team12345.databinding.ItemSelectedMemberBinding;

import java.util.ArrayList;
import java.util.List;

public class SelectedMemberAdapter extends RecyclerView.Adapter<SelectedMemberAdapter.ViewHolder> {

    private List<UserDTO> selectedUsers = new ArrayList<>();
    private final OnRemoveClickListener listener;

    public interface OnRemoveClickListener {
        void onRemoveClick(UserDTO user);
    }

    public SelectedMemberAdapter(OnRemoveClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<UserDTO> users) {
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
        UserDTO user = selectedUsers.get(position);
        String firstName = user.getFullName().split(" ")[0];
        holder.binding.tvFirstName.setText(firstName);
        // Glide would be used here for avatar:
        // Glide.with(holder.itemView).load(user.getAvatarUrl()).into(holder.binding.ivAvatar);
        
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
