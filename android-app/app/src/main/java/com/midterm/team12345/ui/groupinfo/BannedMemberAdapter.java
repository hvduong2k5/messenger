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
import com.midterm.team12345.databinding.ItemBannedMemberBinding;

public class BannedMemberAdapter extends ListAdapter<ParticipantResponseDTO, BannedMemberAdapter.ViewHolder> {

    private final OnUnbanClickListener listener;

    public interface OnUnbanClickListener {
        void onUnbanClick(ParticipantResponseDTO member);
    }

    public BannedMemberAdapter(OnUnbanClickListener listener) {
        super(new DiffUtil.ItemCallback<ParticipantResponseDTO>() {
            @Override
            public boolean areItemsTheSame(@NonNull ParticipantResponseDTO oldItem, @NonNull ParticipantResponseDTO newItem) {
                return oldItem.getUserId().equals(newItem.getUserId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull ParticipantResponseDTO oldItem, @NonNull ParticipantResponseDTO newItem) {
                return oldItem.getUsername().equals(newItem.getUsername());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemBannedMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemBannedMemberBinding binding;

        ViewHolder(ItemBannedMemberBinding binding) {
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

            binding.btnRemove.setOnClickListener(v -> listener.onUnbanClick(member));
        }
    }
}
