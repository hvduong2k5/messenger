package com.midterm.team12345.ui.conversationsettings;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.midterm.team12345.MainActivity;
import com.midterm.team12345.R;
import com.midterm.team12345.domain.model.Conversation;
import com.midterm.team12345.data.repository.ConversationRepositoryImpl;
import com.midterm.team12345.data.repository.FriendRepositoryImpl;
import com.midterm.team12345.databinding.ActivityConversationSettingsBinding;
import com.midterm.team12345.databinding.ItemSettingsRowBinding;
import com.midterm.team12345.ui.base.BaseActivity;
import com.midterm.team12345.utils.Resource;

public class ConversationSettingsActivity extends BaseActivity<ActivityConversationSettingsBinding, ConversationSettingsViewModel> {

    private Conversation conversation;

    @Override
    protected ActivityConversationSettingsBinding inflateBinding(LayoutInflater inflater) {
        return ActivityConversationSettingsBinding.inflate(inflater);
    }

    @Override
    protected ConversationSettingsViewModel createViewModel() {
        // Thay thế ChatRepositoryImpl bằng ConversationRepositoryImpl và FriendRepositoryImpl chuyên biệt
        ConversationSettingsViewModelFactory factory = new ConversationSettingsViewModelFactory(
                ConversationRepositoryImpl.getInstance(getApplication()),
                FriendRepositoryImpl.getInstance(getApplication())
        );
        return new ViewModelProvider(this, factory).get(ConversationSettingsViewModel.class);
    }

    @Override
    protected void setupViews() {
        // Lấy dữ liệu hội thoại từ Intent
        conversation = (Conversation) getIntent().getSerializableExtra("conversation");
        if (conversation == null) {
            finish();
            return;
        }

        setupToolbar();
        setupHeader();
        setupSettingRows();
        setupListeners();
        checkAdminStatus();
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

        String avatarUrl = conversation.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.startsWith("http")) {
            avatarUrl = com.midterm.team12345.data.remote.RetrofitClient.getBaseUrl() + (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
        }

        Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .fallback(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder)
                .into(binding.ivAvatar);
    }

    private void setupSettingRows() {
        boolean isGroup = conversation.getGroup();

        // Cấu hình các dòng Setting chung
        bindRow(binding.itemColor, "Color", null, R.drawable.ic_check_circle);
        bindRow(binding.itemEmoji, "Emoji", "👍", R.drawable.ic_check_circle);
        bindRow(binding.itemNicknames, "Nicknames", null, R.drawable.ic_back_arrow);

        // Ẩn các dòng chưa cấu hình hoặc không dùng tới
        binding.itemSearch.getRoot().setVisibility(View.GONE);
        binding.itemNotifications.getRoot().setVisibility(View.GONE);
        binding.itemIgnore.getRoot().setVisibility(View.GONE);

        // Hiển thị các tính năng dựa trên loại hội thoại (Group vs 1-1)
        binding.itemBlock.getRoot().setVisibility(isGroup ? View.GONE : View.VISIBLE);
        binding.itemViewMembers.getRoot().setVisibility(isGroup ? View.VISIBLE : View.GONE);
        binding.itemAddMember.getRoot().setVisibility(View.GONE);

        if (isGroup) {
            binding.btnAddMember.setVisibility(View.GONE);
        } else {
            binding.btnAddMember.setVisibility(View.VISIBLE);
        }

        if (isGroup) {
            bindRow(binding.itemViewMembers, "View Members", null, R.drawable.ic_back_arrow);
            bindRow(binding.itemAddMember, "Add Member", null, R.drawable.ic_back_arrow);
        }
        binding.tvAddAction.setText(isGroup ? "Add" : "Profile");

        // Các hành động cảnh báo (Màu đỏ)
        bindRow(binding.itemLeave, isGroup ? "Leave Group" : "Delete Chat", null, 0);
        binding.itemLeave.tvTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

        bindRow(binding.itemDeleteHistory, "Delete History", null, 0);
        binding.itemDeleteHistory.tvTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }

    private void setupListeners() {
        binding.itemLeave.getRoot().setOnClickListener(v -> {
            if (conversation.getGroup()) {
                showConfirmDialog("Are you sure you want to leave this group?", () -> {
                    viewModel.leaveGroup(conversation.getConversationId());
                });
            } else {
                // Xử lý xóa chat hoặc block
                showConfirmDialog("Delete this conversation?", () -> {
                    // Logic xóa chat
                });
            }
        });

        binding.itemAddMember.getRoot().setOnClickListener(v -> openAddMembersScreen());

        binding.btnAddMember.setOnClickListener(v -> {
            if (conversation.getGroup()) {
                openAddMembersScreen();
            }
        });

        binding.itemViewMembers.getRoot().setOnClickListener(v -> {
            com.midterm.team12345.ui.groupinfo.GroupMembersActivity.start(this, conversation.getConversationId());
        });

        binding.itemBlock.getRoot().setOnClickListener(v -> {
            showConfirmDialog("Block this user?", () -> {
                // Logic block người dùng (thường qua FriendRepository)
                // viewModel.unfriend(partnerId); 
            });
        });
    }

    private void openAddMembersScreen() {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            java.util.List<com.midterm.team12345.data.local.entity.ConversationParticipantEntity> participants = 
                com.midterm.team12345.data.local.database.DatabaseProvider.getInstance(getApplicationContext())
                    .getConversationParticipantDao()
                    .getParticipantsForConversationSync(conversation.getConversationId());
            
            java.util.ArrayList<Long> existingIds = new java.util.ArrayList<>();
            if (participants != null) {
                for (com.midterm.team12345.data.local.entity.ConversationParticipantEntity p : participants) {
                    existingIds.add(p.getUserId());
                }
            }
            
            runOnUiThread(() -> {
                Intent intent = new Intent(this, com.midterm.team12345.ui.conversationsettings.addmembers.AddMembersActivity.class);
                intent.putExtra(com.midterm.team12345.ui.conversationsettings.addmembers.AddMembersActivity.EXTRA_CONVERSATION_ID, conversation.getConversationId());
                intent.putExtra(com.midterm.team12345.ui.conversationsettings.addmembers.AddMembersActivity.EXTRA_EXISTING_PARTICIPANTS, existingIds);
                startActivity(intent);
            });
        });
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

    private void checkAdminStatus() {
        if (!conversation.getGroup()) {
            return;
        }

        com.midterm.team12345.data.local.TokenManager tokenManager = new com.midterm.team12345.data.local.TokenManager(this);
        Long myId = tokenManager.getUserId();
        Long conversationId = conversation.getConversationId();

        // 1. Check local database first
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            java.util.List<com.midterm.team12345.data.local.entity.ConversationParticipantEntity> participants = 
                com.midterm.team12345.data.local.database.DatabaseProvider.getInstance(getApplicationContext())
                    .getConversationParticipantDao()
                    .getParticipantsForConversationSync(conversationId);
            if (participants != null) {
                boolean amIAdmin = false;
                for (com.midterm.team12345.data.local.entity.ConversationParticipantEntity p : participants) {
                    if (p.getUserId().equals(myId)) {
                        amIAdmin = "ADMIN".equalsIgnoreCase(p.getRole()) || "OWNER".equalsIgnoreCase(p.getRole());
                        break;
                    }
                }
                final boolean finalAmIAdmin = amIAdmin;
                runOnUiThread(() -> {
                    if (finalAmIAdmin) {
                        binding.itemAddMember.getRoot().setVisibility(View.VISIBLE);
                        binding.btnAddMember.setVisibility(View.VISIBLE);
                    } else {
                        binding.itemAddMember.getRoot().setVisibility(View.GONE);
                        binding.btnAddMember.setVisibility(View.GONE);
                    }
                });
            }
        });

        // 2. Refresh from server to get accurate status and update local DB cache
        viewModel.getParticipants(conversationId).observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                java.util.List<com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO> remoteParticipants = resource.data.getContent();
                if (remoteParticipants != null) {
                    boolean amIAdmin = false;
                    java.util.List<com.midterm.team12345.data.local.entity.ConversationParticipantEntity> localEntities = new java.util.ArrayList<>();
                    for (com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO p : remoteParticipants) {
                        if (p.getUserId().equals(myId)) {
                            amIAdmin = "ADMIN".equalsIgnoreCase(p.getRole()) || "OWNER".equalsIgnoreCase(p.getRole());
                        }
                        localEntities.add(new com.midterm.team12345.data.local.entity.ConversationParticipantEntity(
                                conversationId, p.getUserId(), p.getRole(), p.getUsername(), null
                        ));
                    }
                    
                    if (amIAdmin) {
                        binding.itemAddMember.getRoot().setVisibility(View.VISIBLE);
                        binding.btnAddMember.setVisibility(View.VISIBLE);
                    } else {
                        binding.itemAddMember.getRoot().setVisibility(View.GONE);
                        binding.btnAddMember.setVisibility(View.GONE);
                    }

                    java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                        com.midterm.team12345.data.local.database.DatabaseProvider.getInstance(getApplicationContext())
                            .getConversationParticipantDao()
                            .insertParticipants(localEntities);
                    });
                }
            }
        });
    }
}
