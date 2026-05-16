package com.midterm.team12345.ui.conversationsettings;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.midterm.team12345.MainActivity;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.dto.response.ConversationResponse;
import com.midterm.team12345.data.repository.ChatRepositoryImpl;
import com.midterm.team12345.databinding.ActivityConversationSettingsBinding;
import com.midterm.team12345.databinding.ItemSettingsRowBinding;
import com.midterm.team12345.ui.base.BaseActivity;
import com.midterm.team12345.utils.Resource;

public class ConversationSettingsActivity extends BaseActivity<ActivityConversationSettingsBinding, ConversationSettingsViewModel> {

    private ConversationResponse conversation;

    @Override
    protected ActivityConversationSettingsBinding inflateBinding(LayoutInflater inflater) {
        return ActivityConversationSettingsBinding.inflate(inflater);
    }

    @Override
    protected ConversationSettingsViewModel createViewModel() {
        // Dependency Injection thủ công (Sẽ dùng Hilt/Koin trong tương lai)
        ConversationSettingsViewModelFactory factory = new ConversationSettingsViewModelFactory(
                ChatRepositoryImpl.getInstance(getApplication())
        );
        return new ViewModelProvider(this, factory).get(ConversationSettingsViewModel.class);
    }

    @Override
    protected void setupViews() {
        // Lấy dữ liệu hội thoại từ Intent
        conversation = (ConversationResponse) getIntent().getSerializableExtra("conversation");
        if (conversation == null) {
            finish();
            return;
        }

        setupToolbar();
        setupHeader();
        setupSettingRows();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupHeader() {
        binding.tvName.setText(conversation.getConversationName());
        binding.tvSubInfo.setText(conversation.getGroup() ? "Group Chat" : "Messenger");

        Glide.with(this)
                .load(conversation.getAvatarUrl())
                .placeholder(R.drawable.ic_avatar_placeholder)
                .into(binding.ivAvatar);
    }

    private void setupSettingRows() {
        boolean isGroup = conversation.getGroup();

        // Cấu hình các dòng Setting chung
        bindRow(binding.itemColor, "Color", null, R.drawable.ic_check_circle);
        bindRow(binding.itemEmoji, "Emoji", "👍", R.drawable.ic_check_circle);
        bindRow(binding.itemNicknames, "Nicknames", null, R.drawable.ic_back_arrow);

        // Hiển thị các tính năng dựa trên loại hội thoại (Group vs 1-1)
        binding.itemBlock.getRoot().setVisibility(isGroup ? View.GONE : View.VISIBLE);
        binding.itemViewMembers.getRoot().setVisibility(isGroup ? View.VISIBLE : View.GONE);
        binding.tvAddAction.setText(isGroup ? "Add" : "Profile");

        // Các hành động cảnh báo (Màu đỏ)
        bindRow(binding.itemLeave, isGroup ? "Leave Group" : "Delete Chat", null, 0);
        binding.itemLeave.tvTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

        bindRow(binding.itemDeleteHistory, "Delete History", null, 0);
        binding.itemDeleteHistory.tvTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }

    /**
     * Helper để bind dữ liệu vào layout include item_settings_row
     */
    private void bindRow(ItemSettingsRowBinding row, String title, String value, int iconRes) {
        row.tvTitle.setText(title);
        row.tvValue.setVisibility(value != null ? View.VISIBLE : View.GONE);
        if (value != null) row.tvValue.setText(value);
        row.ivTrailing.setVisibility(iconRes != 0 ? View.VISIBLE : View.GONE);
        if (iconRes != 0) row.ivTrailing.setImageResource(iconRes);
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel(); // Lắng nghe Loading và Error mặc định

        // Lắng nghe kết quả thực hiện hành động
        viewModel.actionState.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                // Nếu là xóa hoặc rời nhóm thì chuyển hướng về màn hình chính
                navigateToMain();
            }
        });
    }



    private void showConfirmDialog(String message, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> onConfirm.run())
                .setNegativeButton("No", null)
                .show();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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