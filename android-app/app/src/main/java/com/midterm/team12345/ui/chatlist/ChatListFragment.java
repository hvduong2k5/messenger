package com.midterm.team12345.ui.chatlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.midterm.team12345.data.repository.ChatRepositoryImpl;
import com.midterm.team12345.databinding.FragmentChatListBinding;
import com.midterm.team12345.util.Resource;

public class ChatListFragment extends Fragment {

    private FragmentChatListBinding binding;
    private ChatListViewModel viewModel;
    private ChatListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupViewModel();
        setupObservers();
        setupListeners();

        viewModel.fetchConversations();
    }

    private void setupRecyclerView() {
        adapter = new ChatListAdapter();
        binding.rvChatList.setAdapter(adapter);
    }

    private void setupViewModel() {
        // In a real app with DI (Hilt/Koin), this would be simpler.
        // For now, manual injection.
        ChatListViewModelFactory factory = new ChatListViewModelFactory(new ChatRepositoryImpl());
        viewModel = new ViewModelProvider(this, factory).get(ChatListViewModel.class);
    }

    private void setupObservers() {
        viewModel.conversationState.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.tvEmptyState.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    if (resource.data != null && !resource.data.isEmpty()) {
                        adapter.submitList(resource.data);
                        binding.tvEmptyState.setVisibility(View.GONE);
                    } else {
                        binding.tvEmptyState.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.fetchConversations());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
