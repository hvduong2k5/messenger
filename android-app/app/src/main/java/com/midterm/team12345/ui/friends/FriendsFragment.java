package com.midterm.team12345.ui.friends;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.team12345.R;
import com.midterm.team12345.databinding.FragmentFriendsBinding;
import com.midterm.team12345.network.UserResponse;
import com.midterm.team12345.ui.base.BaseFragment;

import java.util.List;

public class FriendsFragment extends BaseFragment<FragmentFriendsBinding, FriendsViewModel> {

    private TitleAdapter titleAdapter;
    private SearchAdapter searchAdapter;
    private RequestsEntryAdapter requestsEntryAdapter;
    private FilterAdapter filterAdapter;
    private FriendsListAdapter friendsListAdapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected FragmentFriendsBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentFriendsBinding.inflate(inflater, container, false);
    }

    @Override
    protected FriendsViewModel createViewModel() {
        return new ViewModelProvider(this).get(FriendsViewModel.class);
    }

    @Override
    protected void setupViews() {
        layoutManager = new LinearLayoutManager(requireContext());
        setupRecyclerView();
        setupAlphabetIndex();
        
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.loadData();
        });
    }

    private void setupRecyclerView() {
        titleAdapter = new TitleAdapter();
        searchAdapter = new SearchAdapter();
        requestsEntryAdapter = new RequestsEntryAdapter();
        filterAdapter = new FilterAdapter();
        
        friendsListAdapter = new FriendsListAdapter(new FriendsListAdapter.OnFriendActionListener() {
            @Override
            public void onCall(UserResponse user) {
                Log.d("FriendsFragment", "Calling: " + user.getUsername());
            }

            @Override
            public void onVideoCall(UserResponse user) {
                Log.d("FriendsFragment", "Video Calling: " + user.getUsername());
            }

            @Override
            public void onProfileClick(UserResponse user) {
                Log.d("FriendsFragment", "Opening Profile: " + user.getUsername());
            }
        });

        ConcatAdapter concatAdapter = new ConcatAdapter(
                titleAdapter,
                searchAdapter,
                requestsEntryAdapter,
                filterAdapter,
                friendsListAdapter
        );

        binding.rvFriends.setLayoutManager(layoutManager);
        binding.rvFriends.setAdapter(concatAdapter);
    }

    private void setupAlphabetIndex() {
        // Lắng nghe sự kiện click trên thanh chữ cái bên phải
        for (int i = 0; i < binding.llAlphabetIndex.getChildCount(); i++) {
            View view = binding.llAlphabetIndex.getChildAt(i);
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                tv.setOnClickListener(v -> {
                    String letter = tv.getText().toString();
                    scrollToLetter(letter);
                });
            }
        }
    }

    private void scrollToLetter(String letter) {
        List<Object> currentList = friendsListAdapter.getCurrentList();
        for (int i = 0; i < currentList.size(); i++) {
            Object item = currentList.get(i);
            if (item instanceof String && ((String) item).equalsIgnoreCase(letter)) {
                // Tính toán vị trí trong ConcatAdapter (Cộng thêm các adapter phía trước)
                int position = i + titleAdapter.getItemCount() + searchAdapter.getItemCount() 
                               + requestsEntryAdapter.getItemCount() + filterAdapter.getItemCount();
                layoutManager.scrollToPositionWithOffset(position, 0);
                break;
            }
        }
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel();
        
        viewModel.friendsListGrouped.observe(getViewLifecycleOwner(), list -> {
            friendsListAdapter.submitList(list);
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    @Override
    protected void showLoading() {
        binding.swipeRefresh.setRefreshing(true);
    }

    @Override
    protected void hideLoading() {
        binding.swipeRefresh.setRefreshing(false);
    }
}
