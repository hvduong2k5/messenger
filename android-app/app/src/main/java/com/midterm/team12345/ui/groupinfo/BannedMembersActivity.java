package com.midterm.team12345.ui.groupinfo;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO;
import com.midterm.team12345.data.repository.ConversationRepositoryImpl;
import com.midterm.team12345.databinding.ActivityGroupMembersBinding;
import com.midterm.team12345.ui.base.BaseActivity;
import com.midterm.team12345.utils.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BannedMembersActivity extends BaseActivity<ActivityGroupMembersBinding, GroupInfoViewModel> {

    private static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    private Long conversationId;
    private BannedMemberAdapter adapter;
    private List<ParticipantResponseDTO> allBanned = new ArrayList<>();

    public static void start(Context context, Long conversationId) {
        Intent intent = new Intent(context, BannedMembersActivity.class);
        intent.putExtra(EXTRA_CONVERSATION_ID, conversationId);
        context.startActivity(intent);
    }

    @Override
    protected ActivityGroupMembersBinding inflateBinding(LayoutInflater inflater) {
        return ActivityGroupMembersBinding.inflate(inflater);
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

        binding.toolbar.setTitle("Banned Members");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new BannedMemberAdapter(member -> {
            // Handle unban
            viewModel.unbanMember(conversationId, member.getUserId());
        });
        binding.rvMembers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMembers.setAdapter(adapter);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMembers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        viewModel.fetchBannedMembers(conversationId);
    }

    private void filterMembers(String query) {
        if (query.isEmpty()) {
            adapter.submitList(allBanned);
        } else {
            List<ParticipantResponseDTO> filtered = allBanned.stream()
                    .filter(m -> m.getUsername().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            adapter.submitList(filtered);
        }
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel();
        viewModel.bannedMembers.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                allBanned = resource.data;
                adapter.submitList(allBanned);
            }
        });
    }
}
