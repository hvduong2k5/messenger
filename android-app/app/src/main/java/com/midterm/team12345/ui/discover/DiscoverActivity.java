package com.midterm.team12345.ui.discover;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.team12345.databinding.ActivityDiscoverBinding;
import com.midterm.team12345.utils.Resource;

import java.util.ArrayList;

public class DiscoverActivity extends AppCompatActivity implements DiscoverAdapter.OnUserActionListener {

    private ActivityDiscoverBinding binding;
    private DiscoverViewModel viewModel;
    private DiscoverAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiscoverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication()))
                .get(DiscoverViewModel.class);

        setupUI();
        setupObservers();
        
        // Auto-show keyboard and focus
        binding.etSearch.requestFocus();
        showKeyboard(binding.etSearch);
    }

    private void setupUI() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new DiscoverAdapter(this);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);

        // Ẩn bàn phím khi cuộn danh sách
        binding.rvUsers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard();
                }
            }
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                binding.ivClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                viewModel.onSearchQueryChanged(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý nút Search trên bàn phím
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.etSearch.getText().toString().trim();
                viewModel.onSearchQueryChanged(query);
                hideKeyboard();
                return true;
            }
            return false;
        });

        binding.ivClearSearch.setOnClickListener(v -> {
            binding.etSearch.setText("");
            binding.ivClearSearch.setVisibility(View.GONE);
            viewModel.onSearchQueryChanged("");
        });
    }

    private void setupObservers() {
        viewModel.searchResult.observe(this, resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.tvEmpty.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    viewModel.setUserList(resource.data);
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.userList.observe(this, users -> {
            adapter.submitList(users);
            String query = binding.etSearch.getText().toString().trim();
            binding.tvEmpty.setVisibility(users.isEmpty() && !query.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.errorMessage.observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onUserClick(Long userId) {
        // Navigate to Profile
    }

    @Override
    public void onAddFriendClick(Long userId, int position) {
        viewModel.sendFriendRequest(userId, position);
    }
}
