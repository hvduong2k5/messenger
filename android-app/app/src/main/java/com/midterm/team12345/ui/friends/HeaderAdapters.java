package com.midterm.team12345.ui.friends;

import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.RetrofitClient;
import com.midterm.team12345.databinding.*;

// Adapter cho phần tiêu đề "Friends"
class TitleAdapter extends RecyclerView.Adapter<TitleAdapter.ViewHolder> {
    private String avatarUrl;

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        notifyItemChanged(0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemFriendsTitleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String finalUrl = avatarUrl;
        if (finalUrl != null && !finalUrl.isEmpty()) {
            if (!finalUrl.startsWith("http")) {
                finalUrl = RetrofitClient.getBaseUrl() + (finalUrl.startsWith("/") ? "" : "/") + finalUrl;
            }
            Glide.with(holder.binding.ivUserAvatar.getContext())
                    .load(finalUrl)
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(holder.binding.ivUserAvatar);
        } else {
            holder.binding.ivUserAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }
    @Override
    public int getItemCount() { return 1; }
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemFriendsTitleBinding binding;
        ViewHolder(ItemFriendsTitleBinding binding) { 
            super(binding.getRoot()); 
            this.binding = binding;
        }
    }
}

// Adapter cho thanh tìm kiếm
class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private final TextWatcher textWatcher;
    private final View.OnClickListener addFriendClickListener;

    public SearchAdapter(TextWatcher textWatcher, View.OnClickListener addFriendClickListener) {
        this.textWatcher = textWatcher;
        this.addFriendClickListener = addFriendClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemFriendsSearchBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.etSearch.removeTextChangedListener(textWatcher);
        holder.binding.etSearch.addTextChangedListener(textWatcher);
        holder.binding.ivAddFriend.setOnClickListener(addFriendClickListener);
    }
    @Override
    public int getItemCount() { return 1; }
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemFriendsSearchBinding binding;
        ViewHolder(ItemFriendsSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

// Adapter cho mục "Lời mời kết bạn"
class RequestsEntryAdapter extends RecyclerView.Adapter<RequestsEntryAdapter.ViewHolder> {
    private final View.OnClickListener onClickListener;

    public RequestsEntryAdapter(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemFriendsRequestsEntryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(onClickListener);
    }
    @Override
    public int getItemCount() { return 1; }
    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(ItemFriendsRequestsEntryBinding binding) { super(binding.getRoot()); }
    }
}


