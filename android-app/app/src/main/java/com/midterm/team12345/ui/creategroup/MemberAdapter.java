package com.midterm.team12345.ui.creategroup;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;
import com.midterm.team12345.databinding.ItemMemberSelectBinding;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<UserResponseDTO> members = new ArrayList<>();
    private final List<Long> selectedIds = new ArrayList<>();
    private final OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onMemberClick(UserResponseDTO user);
    }

    public MemberAdapter(OnMemberClickListener listener) {
        this.listener = listener;
    }

    public void setMembers(List<UserResponseDTO> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    public void setSelectedIds(List<UserResponseDTO> selectedUsers) {
        selectedIds.clear();
        for (UserResponseDTO user : selectedUsers) {
            selectedIds.add(user.getId());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberSelectBinding binding = ItemMemberSelectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        UserResponseDTO user = members.get(position);
        // Sử dụng username vì UserResponseDTO không có fullName
        holder.binding.tvFullName.setText(user.getUsername());
        holder.binding.cbSelect.setChecked(selectedIds.contains(user.getId()));
        holder.itemView.setOnClickListener(v -> listener.onMemberClick(user));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        ItemMemberSelectBinding binding;

        MemberViewHolder(ItemMemberSelectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
