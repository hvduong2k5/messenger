package com.midterm.team12345.ui.groupinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.data.repository.ConversationRepositoryImpl;
import com.midterm.team12345.databinding.ActivityGroupInfoBinding;
import com.midterm.team12345.databinding.ItemSettingsRowMainBinding;
import com.midterm.team12345.ui.base.BaseActivity;
import com.midterm.team12345.utils.Resource;

public class GroupInfoActivity extends BaseActivity<ActivityGroupInfoBinding, GroupInfoViewModel> {

    public static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    private Long conversationId;

    @Override
    protected ActivityGroupInfoBinding inflateBinding(LayoutInflater inflater) {
        return ActivityGroupInfoBinding.inflate(inflater);
    }

    @Override
    protected GroupInfoViewModel createViewModel() {
        GroupInfoViewModelFactory factory = new GroupInfoViewModelFactory(
                ConversationRepositoryImpl.getInstance(getApplication())
        );
        return new ViewModelProvider(this, factory).get(GroupInfoViewModel.class);
    }

    @Override
    protected void setupViews() {
        conversationId = getIntent().getLongExtra(EXTRA_CONVERSATION_ID, -1L);
        if (conversationId == -1L) {
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupActionButtons();
        setupMenuRows();

        viewModel.fetchGroupDetails(conversationId);
    }

    private void setupActionButtons() {
        // Button 1: Audio
        binding.btnAudio.ivActionIcon.setImageResource(R.drawable.ic_call);
        binding.btnAudio.tvActionLabel.setText("Audio");
        binding.btnAudio.getRoot().setOnClickListener(v -> {
            Toast.makeText(this, "Audio Call Clicked", Toast.LENGTH_SHORT).show();
        });

        // Button 2: Video (Changed label from "Call" to "Video")
        binding.btnCall.ivActionIcon.setImageResource(R.drawable.ic_video_call);
        binding.btnCall.tvActionLabel.setText("Video");
        binding.btnCall.getRoot().setOnClickListener(v -> {
            Toast.makeText(this, "Video Call Clicked", Toast.LENGTH_SHORT).show();
        });

        // Button 3: Add
        binding.btnAddMember.ivActionIcon.setImageResource(R.drawable.ic_add_friend);
        binding.btnAddMember.tvActionLabel.setText("Add");
        binding.btnAddMember.getRoot().setOnClickListener(v -> {
            Toast.makeText(this, "Add Member Quick Clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupMenuRows() {
        // View Members
        setupRow(binding.itemViewMembers.getRoot(), "View Members", R.drawable.ic_person, R.color.gray_text);
        binding.itemViewMembers.getRoot().setOnClickListener(v -> {
            GroupMembersActivity.start(this, conversationId);
        });

        // Add Member Action
        setupRow(binding.itemAddMember.getRoot(), "Add Member", android.R.drawable.ic_menu_add, R.color.messenger_blue);
        binding.itemAddMember.getRoot().setOnClickListener(v -> {
            Toast.makeText(this, "Add Member screen will be implemented in next issue", Toast.LENGTH_SHORT).show();
        });

        // Delete Member Action
        setupRow(binding.itemDeleteMember.getRoot(), "Delete Member", R.drawable.ic_remove_friend, R.color.messenger_pink);
        binding.itemDeleteMember.getRoot().setOnClickListener(v -> {
            DeleteMemberActivity.start(this, conversationId);
        });
    }

    private void setupRow(View rowView, String title, int iconRes, int colorRes) {
        ItemSettingsRowMainBinding rowBinding = ItemSettingsRowMainBinding.bind(rowView);
        rowBinding.tvTitle.setText(title);
        rowBinding.ivIcon.setImageResource(iconRes);
        rowBinding.ivIcon.setBackgroundResource(R.drawable.bg_settings_icon_circle);
        rowBinding.ivIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(colorRes)));
        rowBinding.ivChevron.setVisibility(View.VISIBLE);
        rowBinding.tvValue.setVisibility(View.GONE);
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel();

        viewModel.groupInfo.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                updateGroupUI(resource.data);
            }
        });

        viewModel.members.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                // Update total members count
                binding.tvMemberCount.setText(resource.data.size() + " Members");
            }
        });
    }

    private void updateGroupUI(ConversationResponseDTO group) {
        binding.tvGroupName.setText(group.getName());
        
        Glide.with(this)
                .load(group.getAvatarUrl())
                .placeholder(R.drawable.ic_avatar_placeholder)
                .circleCrop()
                .into(binding.ivGroupAvatar);
    }
}
