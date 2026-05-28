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
        viewModel.fetchFriendshipStatus();
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

        binding.btnAudio.setOnClickListener(v -> {
            Toast.makeText(this, "Cuộc gọi âm thanh đang phát triển...", Toast.LENGTH_SHORT).show();
        });

        binding.btnVideo.setOnClickListener(v -> {
            Toast.makeText(this, "Cuộc gọi video đang phát triển...", Toast.LENGTH_SHORT).show();
        });

        binding.btnChat.setOnClickListener(v -> {
            Long fromChatPartnerId = getIntent().getLongExtra("FROM_CHAT_PARTNER_ID", -1L);
            if (fromChatPartnerId != -1L && targetUserId.equals(fromChatPartnerId)) {
                Intent intent = new Intent(this, ChatDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, ChatDetailActivity.class);
                intent.putExtra("PARTNER_ID", targetUserId);
                UserEntity user = viewModel.getUser().getValue();
                if (user != null) {
                    intent.putExtra("PARTNER_NAME", user.getUsername());
                }
                startActivity(intent);
            }
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
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            binding.tvBio.setText(user.getBio());
            binding.tvBio.setVisibility(View.VISIBLE);
        } else {
            binding.tvBio.setVisibility(View.GONE);
        }

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

        int colorBlue = getResources().getColor(R.color.messenger_blue);
        int colorGrey = getResources().getColor(R.color.btn_background_gray);
        int colorTextWhite = getResources().getColor(R.color.white);
        int colorTextBlack = getResources().getColor(R.color.black);

        binding.btnFriendAction.setVisibility(View.VISIBLE);
        binding.btnFriendAction.setEnabled(true);
        binding.layoutFriendRequestActions.setVisibility(View.GONE);
        binding.layoutActionButtons.setVisibility(View.VISIBLE);

        switch (status) {
            case STRANGER:
                binding.btnFriendAction.setText("Add Friend");
                binding.btnFriendAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorBlue));
                binding.btnFriendAction.setTextColor(colorTextWhite);
                binding.btnFriendAction.setOnClickListener(v -> viewModel.sendFriendRequest());

                binding.btnAdd.setVisibility(View.VISIBLE);
                binding.btnAdd.setOnClickListener(v -> viewModel.sendFriendRequest());
                break;
            case FRIEND:
                binding.btnFriendAction.setText("Unfriend");
                binding.btnFriendAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorGrey));
                binding.btnFriendAction.setTextColor(colorTextBlack);
                binding.btnFriendAction.setOnClickListener(v -> viewModel.unfriend());

                binding.btnAdd.setVisibility(View.GONE);
                break;
            case SENDER_PENDING:
                binding.btnFriendAction.setText("Cancel Request");
                binding.btnFriendAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorGrey));
                binding.btnFriendAction.setTextColor(colorTextBlack);
                binding.btnFriendAction.setOnClickListener(v -> viewModel.cancelFriendRequest());

                binding.btnAdd.setVisibility(View.GONE);
                break;
            case RECEIVER_PENDING:
                binding.btnFriendAction.setVisibility(View.GONE);
                binding.layoutFriendRequestActions.setVisibility(View.VISIBLE);
                binding.btnAcceptRequest.setOnClickListener(v -> viewModel.acceptFriendRequest());
                binding.btnRejectRequest.setOnClickListener(v -> viewModel.rejectFriendRequest());

                binding.btnAdd.setVisibility(View.GONE);
                break;
            case SELF:
                binding.btnFriendAction.setVisibility(View.GONE);
                binding.layoutActionButtons.setVisibility(View.GONE);
                break;
        }
    }
}
