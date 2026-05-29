package com.midterm.team12345.ui.chatdetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.data.remote.dto.response.AttachmentResponseDTO;
import com.midterm.team12345.databinding.ItemAttachmentBinding;

import java.util.ArrayList;
import java.util.List;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolder> {

    private List<AttachmentResponseDTO> attachments = new ArrayList<>();

    public void setAttachments(List<AttachmentResponseDTO> attachments) {
        this.attachments = attachments != null ? attachments : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAttachmentBinding binding = ItemAttachmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(attachments.get(position));
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAttachmentBinding binding;

        public ViewHolder(ItemAttachmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AttachmentResponseDTO attachment) {
            String url = com.midterm.team12345.utils.ImageUtils.optimizeMediaUrl(attachment.getUrl());

            if (url == null) return;

            boolean isVideo = "VIDEO".equalsIgnoreCase(attachment.getType());
            binding.ivPlayIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);

            RequestOptions options = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .centerCrop();

            if (isVideo) {
                options = options.frame(1000000);
            }

            Glide.with(binding.ivAttachment.getContext())
                    .load(url)
                    .apply(options)
                    .into(binding.ivAttachment);
        }
    }
}
