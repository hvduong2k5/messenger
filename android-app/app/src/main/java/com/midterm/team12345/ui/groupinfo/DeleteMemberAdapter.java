package com.midterm.team12345.ui.groupinfo;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO;
import com.midterm.team12345.databinding.ItemDeleteMemberBinding;

import java.util.HashSet;
import java.util.Set;

public class DeleteMemberAdapter extends ListAdapter<ParticipantResponseDTO, DeleteMemberAdapter.ViewHolder> {

    private final Set<Long> selectedUserIds = new HashSet<>();
    private final OnSelectionChangedListener selectionListener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    public DeleteMemberAdapter(OnSelectionChangedListener selectionListener) {
        super(new DiffUtil.ItemCallback<ParticipantResponseDTO>() {
            @Override
            public boolean areItemsTheSame(@NonNull ParticipantResponseDTO oldItem, @NonNull ParticipantResponseDTO newItem) {
                return oldItem.getUserId().equals(newItem.getUserId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull ParticipantResponseDTO oldItem, @NonNull ParticipantResponseDTO newItem) {
                return oldItem.getUsername().equals(newItem.getUsername()) &&
                        (oldItem.getAvatarUrl() == null ? "" : oldItem.getAvatarUrl())
                                .equals(newItem.getAvatarUrl() == null ? "" : newItem.getAvatarUrl());
            }
        });
        this.selectionListener = selectionListener;
    }

    public Set<Long> getSelectedUserIds() {
        return selectedUserIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemDeleteMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemDeleteMemberBinding binding;

        ViewHolder(ItemDeleteMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ParticipantResponseDTO member) {
            binding.tvName.setText(member.getUsername());
            Glide.with(binding.ivAvatar.getContext())
                    .load(member.getAvatarUrl())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .circleCrop()
                    .into(binding.ivAvatar);

            // Handle checkbox state
            binding.cbSelect.setOnCheckedChangeListener(null);
            binding.cbSelect.setChecked(selectedUserIds.contains(member.getUserId()));

            binding.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedUserIds.add(member.getUserId());
                } else {
                    selectedUserIds.remove(member.getUserId());
                }
                selectionListener.onSelectionChanged(selectedUserIds.size());
            });

            binding.getRoot().setOnClickListener(v -> binding.cbSelect.toggle());
        }
    }
}
