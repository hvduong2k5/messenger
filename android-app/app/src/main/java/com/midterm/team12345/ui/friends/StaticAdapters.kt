package com.midterm.team12345.ui.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm.team12345.databinding.*

// Adapter cho phần tiêu đề "Friends"
class TitleAdapter(private val onAddClick: () -> Unit) : RecyclerView.Adapter<TitleAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemFriendsTitleBinding) : RecyclerView.ViewHolder(binding.getRoot())
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemFriendsTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.ivAddFriend.setOnClickListener { onAddClick() }
    }
    override fun getItemCount() = 1
}

// Adapter cho thanh tìm kiếm
class SearchAdapter : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemFriendsSearchBinding) : RecyclerView.ViewHolder(binding.getRoot())
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemFriendsSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}
    override fun getItemCount() = 1
}

// Adapter cho mục "Lời mời kết bạn"
class RequestsEntryAdapter(private val onClick: () -> Unit) : RecyclerView.Adapter<RequestsEntryAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemFriendsRequestsEntryBinding) : RecyclerView.ViewHolder(binding.getRoot())
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemFriendsRequestsEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener { onClick() }
    }
    override fun getItemCount() = 1
}

// Adapter cho bộ lọc "Tất cả / Mới truy cập"
class FilterAdapter : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemFriendsFilterBinding) : RecyclerView.ViewHolder(binding.getRoot())
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemFriendsFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}
    override fun getItemCount() = 1
}
