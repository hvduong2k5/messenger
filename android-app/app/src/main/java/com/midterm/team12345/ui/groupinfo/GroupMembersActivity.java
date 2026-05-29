package com.midterm.team12345.ui.groupinfo;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

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

public class GroupMembersActivity extends BaseActivity<ActivityGroupMembersBinding, GroupInfoViewModel> {

    private static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    private Long conversationId;
    private GroupMemberAdapter adapter;
    private List<ParticipantResponseDTO> allMembers = new ArrayList<>();
    private boolean amIAdmin = false;

    public static void start(Context context, Long conversationId) {
        Intent intent = new Intent(context, GroupMembersActivity.class);
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

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        com.midterm.team12345.data.local.TokenManager tokenManager = new com.midterm.team12345.data.local.TokenManager(this);
        Long myId = tokenManager.getUserId();

        adapter = new GroupMemberAdapter((member, anchor) -> {
            if (!amIAdmin) return;
            if (member.getUserId().equals(myId)) return;

            android.widget.PopupMenu popup = new android.widget.PopupMenu(this, anchor);
            if ("ADMIN".equalsIgnoreCase(member.getRole())) {
                popup.getMenu().add(0, 2, 0, "Gỡ tư cách Admin");
            } else {
                popup.getMenu().add(0, 1, 0, "Chỉ định làm Admin");
            }
            popup.getMenu().add(0, 3, 0, "Xóa khỏi nhóm");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 3) {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setMessage("Bạn có chắc chắn muốn xóa " + member.getUsername() + " khỏi nhóm?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            viewModel.removeMember(conversationId, member.getUserId()).observe(this, resource -> {
                                if (resource.status == Resource.Status.SUCCESS) {
                                    android.widget.Toast.makeText(this, "Đã xóa thành viên khỏi nhóm", android.widget.Toast.LENGTH_SHORT).show();
                                    viewModel.fetchGroupMembers(conversationId, true);
                                } else if (resource.status == Resource.Status.ERROR) {
                                    android.widget.Toast.makeText(this, "Lỗi: " + resource.message, android.widget.Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Không", null)
                        .show();
                } else {
                    String newRole = item.getItemId() == 1 ? "ADMIN" : "MEMBER";
                    viewModel.updateParticipantRole(conversationId, member.getUserId(), newRole);
                }
                return true;
            });
            popup.show();
        });
        binding.rvMembers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMembers.setAdapter(adapter);

        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            java.util.List<com.midterm.team12345.data.local.entity.ConversationParticipantEntity> participants = 
                com.midterm.team12345.data.local.database.DatabaseProvider.getInstance(getApplicationContext())
                    .getConversationParticipantDao()
                    .getParticipantsForConversationSync(conversationId);
            if (participants != null) {
                for (com.midterm.team12345.data.local.entity.ConversationParticipantEntity p : participants) {
                    if (p.getUserId().equals(myId)) {
                        amIAdmin = "ADMIN".equalsIgnoreCase(p.getRole()) || "OWNER".equalsIgnoreCase(p.getRole());
                        break;
                    }
                }
                runOnUiThread(() -> {
                    adapter.setAdminStatus(myId, amIAdmin);
                });
            }
        });

        binding.rvMembers.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == adapter.getItemCount() - 1) {
                    viewModel.fetchGroupMembers(conversationId, false);
                }
            }
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            private Runnable searchRunnable;
            private android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                searchRunnable = () -> viewModel.searchMembers(s.toString());
                handler.postDelayed(searchRunnable, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        viewModel.init(conversationId);
        viewModel.fetchGroupMembers(conversationId, true);
    }



    @Override
    protected void observeViewModel() {
        super.observeViewModel();
        viewModel.members.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                allMembers = resource.data;
                com.midterm.team12345.data.local.TokenManager tokenManager = new com.midterm.team12345.data.local.TokenManager(this);
                Long myId = tokenManager.getUserId();
                for (ParticipantResponseDTO m : allMembers) {
                    if (m.getUserId().equals(myId)) {
                        amIAdmin = "ADMIN".equalsIgnoreCase(m.getRole()) || "OWNER".equalsIgnoreCase(m.getRole());
                        break;
                    }
                }
                adapter.setAdminStatus(myId, amIAdmin);
                adapter.submitList(allMembers);
            }
        });

        viewModel.updateRoleState.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                android.widget.Toast.makeText(this, "Cập nhật quyền thành công", android.widget.Toast.LENGTH_SHORT).show();
            } else if (resource.status == Resource.Status.ERROR) {
                android.widget.Toast.makeText(this, "Lỗi: " + resource.message, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}
