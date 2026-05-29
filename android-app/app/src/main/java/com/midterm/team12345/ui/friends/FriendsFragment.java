package com.midterm.team12345.ui.friends;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.dto.response.FriendRequestResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserResponseDTO;
import com.midterm.team12345.data.remote.dto.response.UserSearchResponseDTO;
import com.midterm.team12345.databinding.FragmentFriendsBinding;
import com.midterm.team12345.ui.base.BaseFragment;
import com.midterm.team12345.ui.chatdetail.ChatDetailActivity;
import com.midterm.team12345.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends BaseFragment<FragmentFriendsBinding, FriendsViewModel> {

    private TitleAdapter titleAdapter;
    private SearchAdapter searchAdapter;
    private RequestsEntryAdapter requestsEntryAdapter;
    private FriendsListAdapter friendsListAdapter;
    private UserSearchAdapter userSearchAdapter;

    private ConcatAdapter concatAdapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected FragmentFriendsBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentFriendsBinding.inflate(inflater, container, false);
    }

    @Override
    protected FriendsViewModel createViewModel() {
        FriendsViewModelFactory factory = new FriendsViewModelFactory(
                com.midterm.team12345.data.repository.FriendRepositoryImpl.getInstance(requireContext()),
                com.midterm.team12345.data.repository.UserRepositoryImpl.getInstance(requireActivity().getApplication())
        );
        return new ViewModelProvider(this, factory).get(FriendsViewModel.class);
    }

    @Override
    protected void setupViews() {
        layoutManager = new LinearLayoutManager(requireContext());
        setupRecyclerView();
        setupAlphabetIndex();

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.loadData();
        });

        // Load initial data
        viewModel.loadData();
    }

    private void setupRecyclerView() {
        titleAdapter = new TitleAdapter();

        // Implement Search input with TextWatcher
        searchAdapter = new SearchAdapter(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                if (query.trim().isEmpty()) {
                    showDefaultMode();
                } else {
                    showLocalSearchMode();
                }
                viewModel.onLocalSearch(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        }, v -> {
            Intent intent = new Intent(requireContext(), AddFriendsActivity.class);
            startActivity(intent);
        });

        requestsEntryAdapter = new RequestsEntryAdapter(v -> showPendingRequestsBottomSheet());

        friendsListAdapter = new FriendsListAdapter(new FriendsListAdapter.OnFriendActionListener() {
            @Override
            public void onCall(UserResponseDTO user) {
                Log.d("FriendsFragment", "Calling: " + user.getUsername());
                Toast.makeText(requireContext(), "Đang gọi " + user.getUsername() + "...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVideoCall(UserResponseDTO user) {
                Log.d("FriendsFragment", "Video Calling: " + user.getUsername());
                Toast.makeText(requireContext(), "Đang gọi video " + user.getUsername() + "...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFriendClick(UserResponseDTO user) {
                Log.d("FriendsFragment", "Opening Chat with: " + user.getUsername());
                // Open chat session with friend
                Intent intent = new Intent(requireContext(), ChatDetailActivity.class);
                intent.putExtra("PARTNER_NAME", user.getUsername());
                intent.putExtra("PARTNER_ID", user.getId());
                startActivity(intent);
            }

            @Override
            public void onProfileLongClick(UserResponseDTO user) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Hủy kết bạn")
                        .setMessage("Bạn có chắc chắn muốn hủy kết bạn với " + user.getUsername() + " không?")
                        .setPositiveButton("Hủy kết bạn", (dialog, which) -> {
                            viewModel.onUnfriend(user.getId());
                            Toast.makeText(requireContext(), "Đã hủy kết bạn với " + user.getUsername(), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        userSearchAdapter = new UserSearchAdapter(new UserSearchAdapter.OnUserSearchActionListener() {
            @Override
            public void onAddFriend(Long userId) {
                viewModel.onAddFriend(userId);
                Toast.makeText(requireContext(), "Đã gửi lời mời kết bạn!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMessageClick(UserSearchResponseDTO user) {
                Intent intent = new Intent(requireContext(), ChatDetailActivity.class);
                intent.putExtra("PARTNER_NAME", user.getUsername());
                intent.putExtra("PARTNER_ID", user.getId());
                startActivity(intent);
            }

            @Override
            public void onAcceptRequest(Long userId) {
                viewModel.onAcceptRequest(userId);
                Toast.makeText(requireContext(), "Đã chấp nhận kết bạn!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRejectRequest(Long userId) {
                viewModel.onRejectRequest(userId);
                Toast.makeText(requireContext(), "Đã từ chối lời mời!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelRequest(Long userId) {
                viewModel.onCancelFriendRequest(userId);
                Toast.makeText(requireContext(), "Đã hủy yêu cầu kết bạn!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUnfriend(Long userId) {
                viewModel.onUnfriend(userId);
                Toast.makeText(requireContext(), "Đã hủy kết bạn!", Toast.LENGTH_SHORT).show();
            }
        });

        concatAdapter = new ConcatAdapter(
                titleAdapter,
                searchAdapter,
                requestsEntryAdapter,
                friendsListAdapter
        );

        binding.rvFriends.setLayoutManager(layoutManager);
        binding.rvFriends.setAdapter(concatAdapter);
    }

    private void showDefaultMode() {
        binding.llAlphabetIndex.setVisibility(View.VISIBLE);
        if (concatAdapter.getAdapters().contains(userSearchAdapter)) {
            concatAdapter.removeAdapter(userSearchAdapter);
        }
        if (!concatAdapter.getAdapters().contains(requestsEntryAdapter)) {
            concatAdapter.addAdapter(2, requestsEntryAdapter);
        }
        if (!concatAdapter.getAdapters().contains(friendsListAdapter)) {
            concatAdapter.addAdapter(3, friendsListAdapter);
        }
        
        // Update empty state based on default friends list
        List<Object> currentList = friendsListAdapter.getCurrentList();
        if (currentList == null || currentList.isEmpty()) {
            binding.tvEmptyState.setText("Danh sách bạn bè trống");
            binding.tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void showLocalSearchMode() {
        binding.llAlphabetIndex.setVisibility(View.GONE);
        if (concatAdapter.getAdapters().contains(requestsEntryAdapter)) {
            concatAdapter.removeAdapter(requestsEntryAdapter);
        }

        if (!concatAdapter.getAdapters().contains(friendsListAdapter)) {
            concatAdapter.addAdapter(friendsListAdapter);
        }
        if (concatAdapter.getAdapters().contains(userSearchAdapter)) {
            concatAdapter.removeAdapter(userSearchAdapter);
        }
        binding.tvEmptyState.setVisibility(View.GONE);
    }

    private void showPendingRequestsBottomSheet() {
        viewModel.fetchPendingRequests();

        com.google.android.material.bottomsheet.BottomSheetDialog dialog = 
                new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 64, 48, 48);
        layout.setBackgroundColor(android.graphics.Color.TRANSPARENT);

        TextView title = new TextView(requireContext());
        title.setText("Lời mời kết bạn");
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getResources().getColor(R.color.black));
        title.setPadding(16, 0, 16, 32);
        layout.addView(title);

        RecyclerView rv = new RecyclerView(requireContext());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        FriendRequestAdapter requestAdapter = new FriendRequestAdapter(new FriendRequestAdapter.OnRequestActionListener() {
            @Override
            public void onConfirm(Long senderId) {
                viewModel.onAcceptRequest(senderId);
                Toast.makeText(requireContext(), "Đã chấp nhận kết bạn!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(Long senderId) {
                viewModel.onRejectRequest(senderId);
                Toast.makeText(requireContext(), "Đã từ chối lời mời!", Toast.LENGTH_SHORT).show();
            }
        });

        rv.setAdapter(requestAdapter);
        layout.addView(rv);

        TextView emptyView = new TextView(requireContext());
        emptyView.setText("Không có lời mời kết bạn nào");
        emptyView.setGravity(android.view.Gravity.CENTER);
        emptyView.setPadding(0, 64, 0, 64);
        emptyView.setTextSize(16);
        emptyView.setTextColor(getResources().getColor(R.color.gray_text));
        emptyView.setVisibility(View.GONE);
        layout.addView(emptyView);

        dialog.setContentView(layout);

        dialog.setOnShowListener(dialogInterface -> {
            com.google.android.material.bottomsheet.BottomSheetDialog d = (com.google.android.material.bottomsheet.BottomSheetDialog) dialogInterface;
            android.view.View bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackground(androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.bg_bottom_sheet));
            }
        });

        viewModel.pendingRequests.observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    if (resource.data.isEmpty()) {
                        rv.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        rv.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                        requestAdapter.submitList(resource.data);
                    }
                }
            }
        });

        dialog.show();
    }

    private void setupAlphabetIndex() {
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
                int position = i + titleAdapter.getItemCount() + searchAdapter.getItemCount() 
                               + (concatAdapter.getAdapters().contains(requestsEntryAdapter) ? requestsEntryAdapter.getItemCount() : 0);
                layoutManager.scrollToPositionWithOffset(position, 0);
                break;
            }
        }
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel();

        viewModel.currentUserProfile.observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                String avatarUrl = resource.data.getAvatarUrl();
                titleAdapter.setAvatarUrl(avatarUrl);
            }
        });

        viewModel.friendsListGrouped.observe(getViewLifecycleOwner(), list -> {
            friendsListAdapter.submitList(list);
            binding.swipeRefresh.setRefreshing(false);
            if (binding.llAlphabetIndex.getVisibility() == View.VISIBLE) {
                if (list == null || list.isEmpty()) {
                    binding.tvEmptyState.setText("Danh sách bạn bè trống");
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    binding.tvEmptyState.setVisibility(View.GONE);
                }
            }
        });

        viewModel.searchResults.observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    userSearchAdapter.submitList(resource.data);
                    if (binding.llAlphabetIndex.getVisibility() == View.GONE) {
                        if (resource.data.isEmpty()) {
                            binding.tvEmptyState.setText("Không tìm thấy kết quả nào");
                            binding.tvEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            binding.tvEmptyState.setVisibility(View.GONE);
                        }
                    }
                }
            }
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
