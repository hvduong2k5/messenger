package com.midterm.team12345.ui.conversationsettings;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.midterm.team12345.MainActivity;
import com.midterm.team12345.R;
import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.domain.model.Conversation;
import com.midterm.team12345.data.repository.ConversationRepositoryImpl;
import com.midterm.team12345.data.repository.FriendRepositoryImpl;
import com.midterm.team12345.databinding.ActivityConversationSettingsBinding;
import com.midterm.team12345.databinding.ItemSettingsRowBinding;
import com.midterm.team12345.ui.base.BaseActivity;
import com.midterm.team12345.utils.NavigationUtils;
import com.midterm.team12345.utils.Resource;

public class ConversationSettingsActivity extends BaseActivity<ActivityConversationSettingsBinding, ConversationSettingsViewModel> {

    private Conversation conversation;
    private Long partnerId = null;

    @Override
    protected ActivityConversationSettingsBinding inflateBinding(LayoutInflater inflater) {
        return ActivityConversationSettingsBinding.inflate(inflater);
    }

    @Override
    protected ConversationSettingsViewModel createViewModel() {
        ConversationSettingsViewModelFactory factory = new ConversationSettingsViewModelFactory(
                ConversationRepositoryImpl.getInstance(getApplication()),
                FriendRepositoryImpl.getInstance(getApplication())
        );
        return new ViewModelProvider(this, factory).get(ConversationSettingsViewModel.class);
    }

    @Override
    protected void setupViews() {
        conversation = (Conversation) getIntent().getSerializableExtra("conversation");
        if (conversation == null) {
            finish();
            return;
        }

        setupToolbar();
        setupHeader();
        setupSettingRows();
        setupListeners();
        
        if (!conversation.getGroup()) {
            findPartnerId();
        } else {
            checkAdminStatus();
        }
    }

    private void findPartnerId() {
        Long myId = TokenManager.getInstance(this).getUserId();
        viewModel.getParticipants(conversation.getConversationId()).observe(this, resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                java.util.List<com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO> list = resource.data.getContent();
                if (list != null) {
                    for (com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO p : list) {
                        if (!p.getUserId().equals(myId)) {
                            partnerId = p.getUserId();
                            break;
                        }
                    }
                }
            }
        });
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

        binding.itemColor.getRoot().setVisibility(View.GONE);
        binding.itemEmoji.getRoot().setVisibility(View.GONE);
        binding.itemNicknames.getRoot().setVisibility(View.GONE);
        binding.itemSearch.getRoot().setVisibility(View.GONE);
        binding.itemNotifications.getRoot().setVisibility(View.GONE);
        binding.itemIgnore.getRoot().setVisibility(View.GONE);
        binding.itemBlock.getRoot().setVisibility(View.GONE);
        binding.itemDeleteHistory.getRoot().setVisibility(View.GONE);

        // Hide headers
        binding.tvHeaderPrivacy.setVisibility(View.GONE);

        // Hide calling/mute buttons from quick actions
        binding.btnAudioCall.setVisibility(View.GONE);
        binding.btnVideoCall.setVisibility(View.GONE);
        binding.btnMute.setVisibility(View.GONE);

        if (isGroup) {
            binding.tvHeaderMoreActions.setVisibility(View.VISIBLE);
            binding.itemViewMembers.getRoot().setVisibility(View.VISIBLE);
            binding.itemAddMember.getRoot().setVisibility(View.GONE); // Admin only, checkAdminStatus toggles
            bindRow(binding.itemViewMembers, "View Members", null, R.drawable.ic_back_arrow);
            binding.tvAddAction.setText("Add");

            binding.itemLeave.getRoot().setVisibility(View.VISIBLE);
            bindRow(binding.itemLeave, "Leave Group", null, 0);
            binding.itemLeave.tvTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            binding.tvHeaderMoreActions.setVisibility(View.GONE);
            binding.itemViewMembers.getRoot().setVisibility(View.GONE);
            binding.itemAddMember.getRoot().setVisibility(View.GONE);
            binding.tvAddAction.setText("Profile");

            binding.itemLeave.getRoot().setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        binding.itemLeave.getRoot().setOnClickListener(v -> {
            if (conversation.getGroup()) {
                showConfirmDialog("Are you sure you want to leave this group?", () -> {
                    viewModel.leaveGroup(conversation.getConversationId());
                });
            } else {
                showConfirmDialog("Delete this conversation?", () -> {
                    // Logic to delete conversation
                });
            }
        });

        binding.itemAddMember.getRoot().setOnClickListener(v -> {
            if (conversation.getGroup()) {
                openAddMembersScreen();
            } else {
                if (partnerId != null) {
                    NavigationUtils.navigateToProfile(this, partnerId, partnerId);
                } else {
                    Toast.makeText(this, "Đang tải thông tin cá nhân...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnAddMember.setOnClickListener(v -> {
            if (conversation.getGroup()) {
                openAddMembersScreen();
            } else {
                if (partnerId != null) {
                    NavigationUtils.navigateToProfile(this, partnerId, partnerId);
                } else {
                    Toast.makeText(this, "Đang tải thông tin cá nhân...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.itemViewMembers.getRoot().setOnClickListener(v -> {
            com.midterm.team12345.ui.groupinfo.GroupMembersActivity.start(this, conversation.getConversationId());
        });

        binding.itemBlock.getRoot().setOnClickListener(v -> {
            showConfirmDialog("Block this user?", () -> {
                // Logic block user
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

    private void bindRow(ItemSettingsRowBinding row, String title, String value, int iconRes) {
        row.tvTitle.setText(title);
        row.tvValue.setVisibility(value != null ? View.VISIBLE : View.GONE);
        if (value != null) row.tvValue.setText(value);
        row.ivTrailing.setVisibility(iconRes != 0 ? View.VISIBLE : View.GONE);
        if (iconRes != 0) row.ivTrailing.setImageResource(iconRes);
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel();
        viewModel.actionState.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
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

        Long myId = TokenManager.getInstance(this).getUserId();
        Long conversationId = conversation.getConversationId();

        // 1. Check local Room DB first for instant UI response
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            java.util.List<com.midterm.team12345.data.local.entity.ConversationParticipantEntity> localParts = 
                com.midterm.team12345.data.local.database.DatabaseProvider.getInstance(getApplicationContext())
                    .getConversationParticipantDao()
                    .getParticipantsForConversationSync(conversationId);
            if (localParts != null && !localParts.isEmpty()) {
                boolean amIAdmin = false;
                for (com.midterm.team12345.data.local.entity.ConversationParticipantEntity p : localParts) {
                    if (p.getUserId().equals(myId)) {
                        amIAdmin = "ADMIN".equalsIgnoreCase(p.getRole()) || "OWNER".equalsIgnoreCase(p.getRole());
                        break;
                    }
                }
                final boolean finalAmIAdmin = amIAdmin;
                runOnUiThread(() -> updateAdminUi(finalAmIAdmin));
            }
        });

        // 2. Fetch from remote API to ensure correctness and handle cases where local DB is empty
        viewModel.getParticipants(conversationId).observe(this, resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                java.util.List<com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO> remoteParts = resource.data.getContent();
                if (remoteParts != null) {
                    boolean amIAdmin = false;
                    for (com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO p : remoteParts) {
                        if (p.getUserId().equals(myId)) {
                            amIAdmin = "ADMIN".equalsIgnoreCase(p.getRole()) || "OWNER".equalsIgnoreCase(p.getRole());
                            break;
                        }
                    }
                    updateAdminUi(amIAdmin);
                }
            }
        });
    }

    private void updateAdminUi(boolean amIAdmin) {
        if (amIAdmin) {
            binding.itemAddMember.getRoot().setVisibility(View.VISIBLE);
            binding.btnAddMember.setVisibility(View.VISIBLE);
            bindRow(binding.itemAddMember, "Add Member", null, R.drawable.ic_back_arrow);
        } else {
            binding.itemAddMember.getRoot().setVisibility(View.GONE);
            binding.btnAddMember.setVisibility(View.GONE);
        }
    }
}
