package com.midterm.team12345.ui.groupinfo;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.dto.response.AttachmentResponseDTO;
import com.midterm.team12345.databinding.ItemSharedMediaBinding;

public class MediaPreviewAdapter extends ListAdapter<AttachmentResponseDTO, MediaPreviewAdapter.ViewHolder> {

    protected MediaPreviewAdapter() {
        super(new DiffUtil.ItemCallback<AttachmentResponseDTO>() {
            @Override
            public boolean areItemsTheSame(@NonNull AttachmentResponseDTO oldItem, @NonNull AttachmentResponseDTO newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull AttachmentResponseDTO oldItem, @NonNull AttachmentResponseDTO newItem) {
                return oldItem.getUrl().equals(newItem.getUrl());
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSharedMediaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSharedMediaBinding binding;

        ViewHolder(ItemSharedMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AttachmentResponseDTO media) {
            Glide.with(binding.ivMediaThumbnail.getContext())
                    .load(media.getUrl())
                    .placeholder(R.color.btn_background_gray)
                    .centerCrop()
                    .into(binding.ivMediaThumbnail);
        }
    }
}
