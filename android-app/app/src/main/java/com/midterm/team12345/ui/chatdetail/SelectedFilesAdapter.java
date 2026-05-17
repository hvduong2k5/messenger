package com.midterm.team12345.ui.chatdetail;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.midterm.team12345.databinding.ItemSelectedFilePreviewBinding;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelectedFilesAdapter extends RecyclerView.Adapter<SelectedFilesAdapter.ViewHolder> {

    private List<File> files = new ArrayList<>();
    private final OnFileRemoveListener listener;

    public interface OnFileRemoveListener {
        void onFileRemove(File file);
    }

    public SelectedFilesAdapter(OnFileRemoveListener listener) {
        this.listener = listener;
    }

    public void setFiles(List<File> files) {
        this.files = files != null ? new ArrayList<>(files) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSelectedFilePreviewBinding binding = ItemSelectedFilePreviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = files.get(position);
        holder.binding.tvFileName.setText(file.getName());
        holder.binding.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFileRemove(file);
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSelectedFilePreviewBinding binding;

        ViewHolder(ItemSelectedFilePreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
