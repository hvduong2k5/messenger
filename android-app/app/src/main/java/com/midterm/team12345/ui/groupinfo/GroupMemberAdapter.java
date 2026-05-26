package com.midterm.team12345.ui.groupinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO;
import com.midterm.team12345.databinding.ItemGroupMemberBinding;

public class GroupMemberAdapter extends ListAdapter<ParticipantResponseDTO, GroupMemberAdapter.ViewHolder> {

    private final OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onMemberClick(ParticipantResponseDTO member);
    }

    public GroupMemberAdapter(OnMemberClickListener listener) {
        super(new DiffUtil.ItemCallback<ParticipantResponseDTO>() {
            @Override
            public boolean areItemsTheSame(@NonNull ParticipantResponseDTO oldItem, @NonNull ParticipantResponseDTO newItem) {
                return oldItem.getUserId().equals(newItem.getUserId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull ParticipantResponseDTO oldItem, @NonNull ParticipantResponseDTO newItem) {
                return oldItem.getUsername().equals(newItem.getUsername()) &&
                        oldItem.getRole().equals(newItem.getRole()) &&
                        (oldItem.getAvatarUrl() == null ? "" : oldItem.getAvatarUrl())
                                .equals(newItem.getAvatarUrl() == null ? "" : newItem.getAvatarUrl());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemGroupMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemGroupMemberBinding binding;

        ViewHolder(ItemGroupMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ParticipantResponseDTO member) {
            binding.tvName.setText(member.getUsername());
            
            // Set Role Label
            if ("ADMIN".equalsIgnoreCase(member.getRole()) || "OWNER".equalsIgnoreCase(member.getRole())) {
                binding.tvRole.setVisibility(View.VISIBLE);
                binding.tvRole.setText(member.getRole().equalsIgnoreCase("OWNER") ? "Owner" : "Admin");
            } else {
                binding.tvRole.setVisibility(View.GONE);
            }

            Glide.with(binding.ivAvatar.getContext())
                    .load(member.getAvatarUrl())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .circleCrop()
                    .into(binding.ivAvatar);

            binding.getRoot().setOnClickListener(v -> listener.onMemberClick(member));
        }
    }
}
