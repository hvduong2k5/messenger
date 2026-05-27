package com.midterm.team12345.ui.groupinfo;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.midterm.team12345.data.remote.dto.response.ParticipantResponseDTO;
import com.midterm.team12345.data.repository.ConversationRepositoryImpl;
import com.midterm.team12345.databinding.ActivityDeleteMemberBinding;
import com.midterm.team12345.ui.base.BaseActivity;
import com.midterm.team12345.utils.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DeleteMemberActivity extends BaseActivity<ActivityDeleteMemberBinding, GroupInfoViewModel> {

    private static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    private Long conversationId;
    private DeleteMemberAdapter adapter;
    private List<ParticipantResponseDTO> allMembers = new ArrayList<>();

    public static void start(Context context, Long conversationId) {
        Intent intent = new Intent(context, DeleteMemberActivity.class);
        intent.putExtra(EXTRA_CONVERSATION_ID, conversationId);
        context.startActivity(intent);
    }

    @Override
    protected ActivityDeleteMemberBinding inflateBinding(LayoutInflater inflater) {
        return ActivityDeleteMemberBinding.inflate(inflater);
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

        // Khởi tạo adapter với listener theo dõi thay đổi lựa chọn
        adapter = new DeleteMemberAdapter(count -> {
            binding.btnRemoveMembers.setText("Remove " + count + " Members");
            binding.btnRemoveMembers.setEnabled(count > 0);
        });

        binding.rvMembers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMembers.setAdapter(adapter);

        binding.btnRemoveMembers.setEnabled(false);
        binding.btnRemoveMembers.setOnClickListener(v -> {
            removeSelectedMembers();
        });

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

        viewModel.fetchGroupMembers(conversationId, true);
    }

    private void removeSelectedMembers() {
        Set<Long> selectedIds = adapter.getSelectedUserIds();
        if (selectedIds.isEmpty()) return;

        // Trong thực tế, bạn có thể gọi API xóa nhiều người cùng lúc 
        // hoặc lặp qua danh sách nếu API chỉ hỗ trợ xóa từng người.
        // Ở đây giả định lặp qua để thực hiện xóa.
        
        Toast.makeText(this, "Removing " + selectedIds.size() + " members...", Toast.LENGTH_SHORT).show();
        
        // Mock logic: Sau khi xóa thành công thì đóng màn hình hoặc tải lại
        for (Long userId : selectedIds) {
            viewModel.removeMember(conversationId, userId).observe(this, resource -> {
                // Xử lý kết quả (đơn giản hóa: chỉ log hoặc show toast)
            });
        }
        
        // Đóng màn hình hoặc thông báo hoàn tất
        finish();
    }

    private void filterMembers(String query) {
        if (query.isEmpty()) {
            adapter.submitList(allMembers);
        } else {
            List<ParticipantResponseDTO> filtered = allMembers.stream()
                    .filter(m -> m.getUsername().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            adapter.submitList(filtered);
        }
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel();
        viewModel.members.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                // Lọc bỏ chính mình (người đang thực hiện xóa) nếu cần, hoặc hiển thị hết
                allMembers = resource.data;
                adapter.submitList(allMembers);
            }
        });
    }
}
