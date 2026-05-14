package com.midterm.team12345.ui.creategroup;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.team12345.data.dto.response.UserDTO;
import com.midterm.team12345.databinding.ItemMemberSelectBinding;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<UserDTO> members = new ArrayList<>();
    private final List<Long> selectedIds = new ArrayList<>();
    private final OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onMemberClick(UserDTO user);
    }

    public MemberAdapter(OnMemberClickListener listener) {
        this.listener = listener;
    }

    public void setMembers(List<UserDTO> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    public void setSelectedIds(List<UserDTO> selectedUsers) {
        selectedIds.clear();
        for (UserDTO user : selectedUsers) {
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
        UserDTO user = members.get(position);
        holder.binding.tvFullName.setText(user.getFullName());
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
