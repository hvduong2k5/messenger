package com.midterm.team12345.ui.settings;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.data.repository.AuthRepositoryImpl;
import com.midterm.team12345.data.repository.UserRepositoryImpl;
import com.midterm.team12345.databinding.FragmentMyProfileBinding;
import com.midterm.team12345.databinding.ItemSettingsRowMainBinding;
import com.midterm.team12345.ui.auth.LoginActivity;
import com.midterm.team12345.ui.base.BaseFragment;

public class MyProfileFragment extends BaseFragment<FragmentMyProfileBinding, MyProfileViewModel> {

    @Override
    protected FragmentMyProfileBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentMyProfileBinding.inflate(inflater, container, false);
    }

    @Override
    protected MyProfileViewModel createViewModel() {
        MyProfileViewModelFactory factory = new MyProfileViewModelFactory(
                UserRepositoryImpl.getInstance(requireActivity().getApplication()),
                AuthRepositoryImpl.getInstance(requireActivity().getApplication()),
                new TokenManager(requireContext())
        );
        return new ViewModelProvider(this, factory).get(MyProfileViewModel.class);
    }

    @Override
    protected void setupViews() {
        // Ẩn nút back nếu là fragment chính trong tab bar (Lưu ý UX)
        binding.toolbar.setNavigationIcon(null);
        
        setupMenuUI();

        binding.ivSettingsGear.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "General Settings", Toast.LENGTH_SHORT).show();
        });

        viewModel.refreshProfile();
    }

    private void setupMenuUI() {
        // Hide Dark Mode and other out-of-scope settings/preferences
        binding.itemDarkMode.getRoot().setVisibility(View.GONE);
        binding.itemPrivacy.getRoot().setVisibility(View.GONE);
        binding.itemNotifications.getRoot().setVisibility(View.GONE);
        binding.itemStorageData.getRoot().setVisibility(View.GONE);
        binding.itemHelp.getRoot().setVisibility(View.GONE);
        binding.itemInviteFriend.getRoot().setVisibility(View.GONE);
        binding.tvHeaderPreferences.setVisibility(View.GONE);

        setupRow(binding.itemActiveStatus, "Active Status", "On", R.drawable.bg_online_status, R.color.badge_green, false);
        
        setupRow(binding.itemMyAccount, "My Account", null, R.drawable.ic_person, R.color.messenger_blue, false);
        binding.itemMyAccount.getRoot().setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        // Nút Logout đỏ
        binding.itemLogout.tvTitle.setText("Log Out");
        binding.itemLogout.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.messenger_pink));
        binding.itemLogout.ivIcon.setImageResource(R.drawable.ic_lock);
        binding.itemLogout.ivIcon.setBackgroundResource(R.drawable.bg_settings_icon_circle);
        binding.itemLogout.ivIcon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.messenger_pink)));
        binding.itemLogout.ivChevron.setVisibility(View.GONE);
        binding.itemLogout.getRoot().setOnClickListener(v -> showLogoutConfirmation());
    }

    private void setupRow(ItemSettingsRowMainBinding row, String title, String value, int iconRes, int circleColorRes, boolean isSwitch) {
        row.tvTitle.setText(title);
        row.ivIcon.setImageResource(iconRes);
        row.ivIcon.setBackgroundResource(R.drawable.bg_settings_icon_circle);
        row.ivIcon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), circleColorRes)));
        
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

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> viewModel.logout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel();
        
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.tvFullName.setText(user.getUsername());
                binding.tvUsername.setText("@" + user.getUsername().toLowerCase().replace(" ", "_"));
                binding.tvBio.setText(user.getBio() != null ? user.getBio() : "Available");
                binding.onlineIndicator.setVisibility((user.getIsOnline() != null && user.getIsOnline()) ? View.VISIBLE : View.GONE);

                Glide.with(this)
                        .load(user.getAvatarUrl())
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .fallback(R.drawable.ic_avatar_placeholder)
                        .error(R.drawable.ic_avatar_placeholder)
                        .circleCrop()
                        .into(binding.ivAvatar);
            }
        });

        viewModel.logoutEvent.observe(getViewLifecycleOwner(), unused -> {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            // Hiển thị loading nếu cần
        });
    }
}
