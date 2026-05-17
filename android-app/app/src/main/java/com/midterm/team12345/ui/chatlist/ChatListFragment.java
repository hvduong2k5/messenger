package com.midterm.team12345.ui.chatlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.repository.ConversationRepositoryImpl;
import com.midterm.team12345.data.repository.UserRepositoryImpl;
import com.midterm.team12345.databinding.FragmentChatListBinding;
import com.midterm.team12345.ui.base.BaseFragment;
import com.midterm.team12345.ui.chatdetail.ChatDetailActivity;
import com.midterm.team12345.ui.creategroup.CreateGroupActivity;
import com.midterm.team12345.utils.Resource;

public class ChatListFragment extends BaseFragment<FragmentChatListBinding, ChatListViewModel> {

    private ChatListAdapter adapter;

    @Override
    protected FragmentChatListBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentChatListBinding.inflate(inflater, container, false);
    }

    @Override
    protected ChatListViewModel createViewModel() {
        ChatListViewModelFactory factory = new ChatListViewModelFactory(
                ConversationRepositoryImpl.getInstance(requireContext()),
                UserRepositoryImpl.getInstance(requireContext())
        );
        return new ViewModelProvider(this, factory).get(ChatListViewModel.class);
    }

    @Override
    protected void setupViews() {
        setupRecyclerView();
        setupListeners();
        
        // Tải dữ liệu ban đầu
        viewModel.fetchConversations();
        viewModel.fetchMyProfile();
    }

    private void setupRecyclerView() {
        adapter = new ChatListAdapter(conversation -> {
            Intent intent = new Intent(getContext(), ChatDetailActivity.class);
            intent.putExtra("conversation", conversation); // Pass the whole object
            startActivity(intent);
        });
        binding.rvChatList.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.fetchConversations());

        binding.btnNewMessage.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CreateGroupActivity.class));
        });
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel(); // Tự động xử lý show/hide loading và error

        viewModel.conversationState.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            if (resource.status == Resource.Status.SUCCESS) {
                binding.swipeRefresh.setRefreshing(false);
                if (resource.data != null && !resource.data.isEmpty()) {
                    adapter.submitList(resource.data);
                    binding.emptyStateLayout.setVisibility(View.GONE);
                    binding.rvChatList.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyStateLayout.setVisibility(View.VISIBLE);
                    binding.rvChatList.setVisibility(View.GONE);
                }
            } else if (resource.status == Resource.Status.ERROR) {
                binding.swipeRefresh.setRefreshing(false);
                if (adapter.getItemCount() == 0) {
                    binding.emptyStateLayout.setVisibility(View.VISIBLE);
                    binding.rvChatList.setVisibility(View.GONE);
                }
            }
        });

        viewModel.profileState.observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                String avatarUrl = resource.data.getAvatarUrl();
                if (avatarUrl != null && !avatarUrl.startsWith("http")) {
                    avatarUrl = com.midterm.team12345.data.remote.RetrofitClient.getBaseUrl() + (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
                }
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .error(R.drawable.ic_avatar_placeholder)
                        .into(binding.ivMyProfile);
            }
        });
    }

    @Override
    protected void showLoading() {
        // Chỉ hiện progress bar nếu không phải đang dùng swipe refresh
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        binding.emptyStateLayout.setVisibility(View.GONE);
    }

    @Override
    protected void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
    }
}
