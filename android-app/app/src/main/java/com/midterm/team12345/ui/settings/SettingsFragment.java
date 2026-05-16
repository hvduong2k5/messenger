package com.midterm.team12345.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.data.repository.ChatRepositoryImpl;
import com.midterm.team12345.databinding.FragmentSettingsBinding;
import com.midterm.team12345.databinding.ItemSettingsRowMainBinding;
import com.midterm.team12345.ui.auth.LoginActivity;
import com.midterm.team12345.ui.chatlist.ChatListViewModel;
import com.midterm.team12345.ui.chatlist.ChatListViewModelFactory;
import com.midterm.team12345.utils.Resource;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private TokenManager tokenManager;
    private ChatListViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = new TokenManager(requireContext());
        
        ChatListViewModelFactory factory = new ChatListViewModelFactory(ChatRepositoryImpl.getInstance(requireActivity().getApplication()));
        viewModel = new ViewModelProvider(this, factory).get(ChatListViewModel.class);

        setupUI();
        observeViewModel();
        viewModel.fetchMyProfile();
    }

    private void observeViewModel() {
        viewModel.profileState.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                binding.tvUserName.setText(resource.data.getUsername());
                Glide.with(this)
                        .load(resource.data.getAvatarUrl())
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .error(R.drawable.ic_avatar_placeholder)
                        .into(binding.ivUserAvatar);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUI() {
        // Dark Mode
        setupRow(binding.itemDarkMode, "Dark Mode", null, android.R.drawable.ic_menu_month, true);
        
        // Active Status
        setupRow(binding.itemActiveStatus, "Active Status", "On", android.R.drawable.presence_online, false);
        binding.itemActiveStatus.tvValue.setVisibility(View.VISIBLE);

        // Edit Profile
        setupRow(binding.itemEditProfile, "Edit Profile", null, android.R.drawable.ic_menu_edit, false);

        // Notifications & Sounds
        setupRow(binding.itemNotifications, "Notifications & Sounds", null, android.R.drawable.ic_lock_silent_mode, false);

        // People
        setupRow(binding.itemPeople, "People", null, android.R.drawable.ic_menu_manage, false);

        // Messaging Settings
        setupRow(binding.itemMessaging, "Messaging Settings", null, android.R.drawable.stat_notify_chat, false);

        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void setupRow(ItemSettingsRowMainBinding row, String title, String value, int iconRes, boolean isSwitch) {
        row.tvTitle.setText(title);
        row.ivIcon.setImageResource(iconRes);
        if (isSwitch) {
            row.switchWidget.setVisibility(View.VISIBLE);
            row.ivChevron.setVisibility(View.GONE);
        } else {
            row.switchWidget.setVisibility(View.GONE);
            row.ivChevron.setVisibility(View.VISIBLE);
        }
        if (value != null) {
            row.tvValue.setText(value);
            row.tvValue.setVisibility(View.VISIBLE);
        }
    }

    private void logout() {
        tokenManager.clear();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
