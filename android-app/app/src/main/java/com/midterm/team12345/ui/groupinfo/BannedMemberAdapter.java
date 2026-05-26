package com.midterm.team12345.ui.groupinfo;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO;
import com.midterm.team12345.databinding.ItemDeleteMemberBinding;

public class BannedMemberAdapter extends ListAdapter<ParticipantResponseDTO, BannedMemberAdapter.BannedMemberViewHolder> {

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
    public BannedMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDeleteMemberBinding binding = ItemDeleteMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()), 
                parent, 
                false // <--- CHÚ Ý: Bắt buộc là false
        );
        return new BannedMemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BannedMemberViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class BannedMemberViewHolder extends RecyclerView.ViewHolder {
        private final ItemDeleteMemberBinding binding;

        public BannedMemberViewHolder(ItemDeleteMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ParticipantResponseDTO member) {
            binding.tvName.setText(member.getUsername());
            Glide.with(binding.ivAvatar.getContext())
                    .load(member.getAvatarUrl())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .circleCrop()
                    .into(binding.ivAvatar);

            // Xử lý Logic Tái sử dụng Layout (UI Logic):
            // Thiết lập checkbox thành trạng thái checked và đổi tint màu xanh lá (badge_green) phù hợp với ngữ cảnh "Unban"
            binding.cbSelect.setChecked(true);
            binding.cbSelect.setButtonTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(binding.getRoot().getContext(), R.color.badge_green)
            ));

            // Khắc phục lỗi bắt sự kiện (Click Listener):
            // Sử dụng getBindingAdapterPosition() thay vì position để tránh IndexOutOfBoundsException
            binding.cbSelect.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onUnbanClick(getItem(pos));
                }
            });
        }
    }
}
