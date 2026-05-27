package com.midterm.team12345.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.local.entity.UserEntity;
import com.midterm.team12345.data.remote.dto.response.FriendshipStatus;
import com.midterm.team12345.data.repository.FriendRepositoryImpl;
import com.midterm.team12345.data.repository.UserRepositoryImpl;
import com.midterm.team12345.databinding.ActivityUserProfileBinding;
import com.midterm.team12345.ui.chatdetail.ChatDetailActivity;

public class UserProfileActivity extends AppCompatActivity {

    private ActivityUserProfileBinding binding;
    private UserProfileViewModel viewModel;
    private Long targetUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        targetUserId = getIntent().getLongExtra("TARGET_USER_ID", -1L);
        if (targetUserId == -1L) {
            finish();
            return;
        }

        setupViewModel();
        setupUI();
        observeViewModel();

        viewModel.fetchUserProfile();
    }

    private void setupViewModel() {
        UserProfileViewModelFactory factory = new UserProfileViewModelFactory(
                UserRepositoryImpl.getInstance(getApplication()),
                FriendRepositoryImpl.getInstance(getApplication())
        );
        viewModel = new ViewModelProvider(this, factory).get(UserProfileViewModel.class);
        viewModel.setTargetUserId(targetUserId);
    }

    private void setupUI() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.itemSharedMedia.tvTitle.setText("Shared Media");
        binding.itemSharedMedia.ivTrailing.setImageResource(R.drawable.ic_chevron_right);
        
        binding.itemCommonGroups.tvTitle.setText("Groups in Common");
        binding.itemCommonGroups.ivTrailing.setImageResource(R.drawable.ic_chevron_right);
        
        binding.itemBlock.tvTitle.setText("Block");
        binding.itemBlock.tvTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        binding.itemBlock.ivTrailing.setVisibility(View.GONE);

        binding.btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatDetailActivity.class);
            intent.putExtra("PARTNER_ID", targetUserId);
            // Intent.FLAG_ACTIVITY_SINGLE_TOP to optimize navigation
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        viewModel.getUser().observe(this, user -> {
            if (user != null) {
                updateUI(user);
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.errorMessage.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.statusMessageEvent.observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(UserEntity user) {
        binding.tvFullName.setText(user.getUsername());
        binding.tvUsername.setText("@" + user.getUsername());
        binding.tvBio.setText(user.getBio() != null && !user.getBio().isEmpty() ? user.getBio() : "No bio yet");

        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.startsWith("http")) {
            avatarUrl = com.midterm.team12345.data.remote.RetrofitClient.getBaseUrl() + (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
        }

        Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .circleCrop()
                .into(binding.ivAvatar);

        FriendshipStatus status = user.getFriendshipStatus();
        if (status == null) status = FriendshipStatus.STRANGER;

        binding.btnFriendAction.setVisibility(View.VISIBLE);
        binding.btnFriendAction.setEnabled(true);

        switch (status) {
            case STRANGER:
                binding.btnFriendAction.setText("Add Friend");
                binding.btnFriendAction.setBackgroundTintList(null); // Primary color
                binding.btnFriendAction.setOnClickListener(v -> viewModel.sendFriendRequest());
                break;
            case FRIEND:
                binding.btnFriendAction.setText("Unfriend");
                binding.btnFriendAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF0F0F0));
                binding.btnFriendAction.setOnClickListener(v -> viewModel.unfriend());
                break;
            case SENDER_PENDING:
                binding.btnFriendAction.setText("Cancel Request");
                binding.btnFriendAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF0F0F0));
                binding.btnFriendAction.setOnClickListener(v -> viewModel.cancelFriendRequest());
                break;
            case RECEIVER_PENDING:
                binding.btnFriendAction.setText("Accept Request");
                binding.btnFriendAction.setOnClickListener(v -> viewModel.acceptFriendRequest());
                break;
            case SELF:
                binding.btnFriendAction.setVisibility(View.GONE);
                binding.btnMessage.setVisibility(View.GONE);
                break;
        }
    }
}
