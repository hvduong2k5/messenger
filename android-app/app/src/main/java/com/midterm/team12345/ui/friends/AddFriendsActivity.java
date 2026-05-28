package com.midterm.team12345.ui.friends;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.midterm.team12345.data.remote.dto.response.UserSearchResponseDTO;
import com.midterm.team12345.data.repository.FriendRepositoryImpl;
import com.midterm.team12345.data.repository.UserRepositoryImpl;
import com.midterm.team12345.databinding.ActivityAddFriendsBinding;
import com.midterm.team12345.ui.base.BaseActivity;
import com.midterm.team12345.utils.Resource;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

public class AddFriendsActivity extends BaseActivity<ActivityAddFriendsBinding, AddFriendsViewModel> {

    private GlobalUserSearchAdapter adapter;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected ActivityAddFriendsBinding inflateBinding(LayoutInflater inflater) {
        return ActivityAddFriendsBinding.inflate(inflater);
    }

    @Override
    protected AddFriendsViewModel createViewModel() {
        return new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new AddFriendsViewModel(
                        UserRepositoryImpl.getInstance(getApplication()),
                        FriendRepositoryImpl.getInstance(getApplicationContext())
                );
            }
        }).get(AddFriendsViewModel.class);
    }

    @Override
    protected void setupViews() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        adapter = new GlobalUserSearchAdapter(new GlobalUserSearchAdapter.OnUserActionListener() {
            @Override
            public void onAddClick(UserSearchResponseDTO user) {
                viewModel.sendFriendRequest(user.getId());
                Toast.makeText(AddFriendsActivity.this, "Request sent!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelClick(UserSearchResponseDTO user) {
                viewModel.cancelFriendRequest(user.getId());
                Toast.makeText(AddFriendsActivity.this, "Request cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.rvSearchResults.setAdapter(adapter);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    debounceHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (query.isEmpty()) {
                        viewModel.loadSuggestions();
                    } else {
                        viewModel.searchUsers(query);
                    }
                };
                debounceHandler.postDelayed(searchRunnable, 300);
            }
        });

        // Load initial suggestions
        viewModel.loadSuggestions();
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel();

        viewModel.searchResults.observe(this, resource -> {
            if (resource != null) {
                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    adapter.submitList(resource.data);
                    if (resource.data.isEmpty()) {
                        binding.tvEmptyState.setVisibility(View.VISIBLE);
                        binding.rvSearchResults.setVisibility(View.GONE);
                    } else {
                        binding.tvEmptyState.setVisibility(View.GONE);
                        binding.rvSearchResults.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    protected void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
    }
}
