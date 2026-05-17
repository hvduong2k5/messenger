package com.midterm.team12345.ui.friends;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.midterm.team12345.databinding.*;

// Adapter cho phần tiêu đề "Friends"
class TitleAdapter extends RecyclerView.Adapter<TitleAdapter.ViewHolder> {
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemFriendsTitleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {}
    @Override
    public int getItemCount() { return 1; }
    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(ItemFriendsTitleBinding binding) { super(binding.getRoot()); }
    }
}

// Adapter cho thanh tìm kiếm
class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemFriendsSearchBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {}
    @Override
    public int getItemCount() { return 1; }
    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(ItemFriendsSearchBinding binding) { super(binding.getRoot()); }
    }
}

// Adapter cho mục "Lời mời kết bạn"
class RequestsEntryAdapter extends RecyclerView.Adapter<RequestsEntryAdapter.ViewHolder> {
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemFriendsRequestsEntryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {}
    @Override
    public int getItemCount() { return 1; }
    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(ItemFriendsRequestsEntryBinding binding) { super(binding.getRoot()); }
    }
}

// Adapter cho bộ lọc
class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemFriendsFilterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {}
    @Override
    public int getItemCount() { return 1; }
    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(ItemFriendsFilterBinding binding) { super(binding.getRoot()); }
    }
}
