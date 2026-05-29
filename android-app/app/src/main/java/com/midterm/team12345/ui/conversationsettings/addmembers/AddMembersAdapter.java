package com.midterm.team12345.ui.conversationsettings.addmembers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.local.entity.UserEntity;
import com.midterm.team12345.databinding.ItemMemberSelectBinding;

import java.util.HashSet;
import java.util.Set;

public class AddMembersAdapter extends ListAdapter<UserEntity, AddMembersAdapter.ViewHolder> {

    private final Set<Long> selectedUserIds = new HashSet<>();
    private final OnUserSelectedListener listener;

    public interface OnUserSelectedListener {
        void onSelectionChanged(int count, Set<Long> selectedIds);
    }

    public AddMembersAdapter(OnUserSelectedListener listener) {
        super(new DiffUtil.ItemCallback<UserEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull UserEntity oldItem, @NonNull UserEntity newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull UserEntity oldItem, @NonNull UserEntity newItem) {
                return oldItem.getUsername().equals(newItem.getUsername()) &&
                        (oldItem.getAvatarUrl() != null ? oldItem.getAvatarUrl().equals(newItem.getAvatarUrl()) : newItem.getAvatarUrl() == null);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberSelectBinding binding = ItemMemberSelectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMemberSelectBinding binding;

        public ViewHolder(ItemMemberSelectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(UserEntity user) {
            binding.tvFullName.setText(user.getUsername());
            binding.tvUsername.setVisibility(View.VISIBLE);
            binding.tvUsername.setText("@" + user.getUsername().toLowerCase());

            Glide.with(binding.ivAvatar.getContext())
                    .load(com.midterm.team12345.utils.ImageUtils.optimizeAvatarUrl(user.getAvatarUrl()))
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .fallback(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .circleCrop()
                    .into(binding.ivAvatar);

            // Cập nhật trạng thái CheckBox (Zero-latency)
            boolean isSelected = selectedUserIds.contains(user.getId());
            binding.cbSelect.setChecked(isSelected);

            itemView.setOnClickListener(v -> {
                if (selectedUserIds.contains(user.getId())) {
                    selectedUserIds.remove(user.getId());
                    binding.cbSelect.setChecked(false);
                } else {
                    selectedUserIds.add(user.getId());
                    binding.cbSelect.setChecked(true);
                }
                
                if (listener != null) {
                    listener.onSelectionChanged(selectedUserIds.size(), selectedUserIds);
                }
            });
        }
    }
}
