package com.midterm.team12345.ui.conversationsettings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.midterm.team12345.MainActivity;
import com.midterm.team12345.R;
import com.midterm.team12345.data.dto.ConversationResponse;
import com.midterm.team12345.databinding.ActivityConversationSettingsBinding;
import com.midterm.team12345.util.Resource;

public class ConversationSettingsActivity extends AppCompatActivity {

    private ActivityConversationSettingsBinding binding;
    private ConversationSettingsViewModel viewModel;
    private ConversationResponse conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConversationSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ConversationSettingsViewModel.class);

        conversation = (ConversationResponse) getIntent().getSerializableExtra("conversation");
        if (conversation == null) {
            finish();
            return;
        }

        initUI();
        setupObservers();
        setupListeners();
    }

    private void initUI() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.tvName.setText(conversation.getConversationName());
        Glide.with(this)
                .load(conversation.getAvatarUrl())
                .placeholder(R.drawable.ic_avatar_placeholder)
                .into(binding.ivAvatar);

        boolean isGroup = conversation.getGroup();
        binding.tvSubInfo.setText(isGroup ? "Group Chat" : "Messenger");
        binding.tvSubInfo.setVisibility(View.VISIBLE);

        // Configure rows
        setupSettingRow(binding.itemColor, "Color", null, R.drawable.ic_check_circle);
        setupSettingRow(binding.itemEmoji, "Emoji", "👍", R.drawable.ic_check_circle);
        setupSettingRow(binding.itemNicknames, "Nicknames", null, R.drawable.ic_back_arrow); // Use back arrow as chevron

        setupSettingRow(binding.itemSearch, "Search in Conversation", null, R.drawable.ic_person); // Placeholder icon
        setupSettingRow(binding.itemViewMembers, "View Members", null, R.drawable.ic_person);

        setupSettingRow(binding.itemNotifications, "Notifications", "On", R.drawable.ic_back_arrow);
        setupSettingRow(binding.itemIgnore, "Ignore Messages", null, R.drawable.ic_person);
        setupSettingRow(binding.itemBlock, "Block", null, R.drawable.ic_person);

        setupSettingRow(binding.itemLeave, isGroup ? "Leave Group" : "Delete Chat", null, 0);
        binding.itemLeave.tvTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        binding.itemLeave.ivTrailing.setVisibility(View.GONE);

        setupSettingRow(binding.itemDeleteHistory, "Delete History", null, 0);
        binding.itemDeleteHistory.tvTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        binding.itemDeleteHistory.ivTrailing.setVisibility(View.GONE);

        // Visibility based on type
        if (isGroup) {
            binding.itemBlock.getRoot().setVisibility(View.GONE);
            binding.itemViewMembers.getRoot().setVisibility(View.VISIBLE);
            binding.tvAddAction.setText("Add");
        } else {
            binding.itemBlock.getRoot().setVisibility(View.VISIBLE);
            binding.itemViewMembers.getRoot().setVisibility(View.GONE);
            binding.tvAddAction.setText("Profile");
        }
    }

    private void setupSettingRow(com.midterm.team12345.databinding.ItemSettingsRowBinding row, String title, String value, int iconRes) {
        row.tvTitle.setText(title);
        if (value != null) {
            row.tvValue.setVisibility(View.VISIBLE);
            row.tvValue.setText(value);
        } else {
            row.tvValue.setVisibility(View.GONE);
        }
        if (iconRes != 0) {
            row.ivTrailing.setImageResource(iconRes);
            row.ivTrailing.setVisibility(View.VISIBLE);
        } else {
            row.ivTrailing.setVisibility(View.GONE);
        }
    }

    private void setupObservers() {
        viewModel.getActionState().observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                if (resource.status == Resource.Status.SUCCESS) {
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    // Handle navigation if needed (e.g., after leaving group)
                } else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupListeners() {
        binding.btnMute.setOnClickListener(v -> viewModel.muteNotifications(true));

        binding.itemLeave.getRoot().setOnClickListener(v -> {
            String message = conversation.getGroup() ? "Are you sure you want to leave this group?" : "Are you sure you want to delete this chat?";
            showConfirmDialog(message, () -> {
                viewModel.leaveConversation(conversation.getConversationId());
                navigateToMain();
            });
        });

        binding.itemDeleteHistory.getRoot().setOnClickListener(v -> {
            showConfirmDialog("Clear all messages in this conversation?", () -> {
                viewModel.deleteHistory(conversation.getConversationId());
            });
        });

        if (!conversation.getGroup()) {
            binding.itemBlock.getRoot().setOnClickListener(v -> {
                showConfirmDialog("Block this user?", () -> {
                    viewModel.blockUser(123L); // Mock ID
                });
            });
        }
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
}
