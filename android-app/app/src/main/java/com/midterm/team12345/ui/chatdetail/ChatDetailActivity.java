package com.midterm.team12345.ui.chatdetail;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.midterm.team12345.data.repository.ChatRepositoryImpl;
import com.midterm.team12345.databinding.ActivityChatDetailBinding;
import com.midterm.team12345.util.Resource;

public class ChatDetailActivity extends AppCompatActivity {

    private ActivityChatDetailBinding binding;
    private ChatDetailViewModel viewModel;
    private MessageAdapter adapter;
    private Long currentUserId = -1L; // Sẽ được cập nhật từ profile
    private Long conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        conversationId = getIntent().getLongExtra("CONVERSATION_ID", -1L);
        String partnerName = getIntent().getStringExtra("PARTNER_NAME");

        setupViewModel();
        setupObservers();
        
        // Lấy profile của tôi trước để biết currentUserId là ai
        viewModel.fetchMyProfile();
        setupUI(partnerName);
        
        viewModel.loadMessages(conversationId);
    }

    private void setupUI(String partnerName) {
        binding.tvPartnerName.setText(partnerName);
        
        // Khởi tạo adapter với ID tạm thời, sẽ update sau khi có profile
        adapter = new MessageAdapter(currentUserId);
        binding.rvMessages.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMessage.getText().toString();
            if (!text.isEmpty() && currentUserId != -1L) {
                viewModel.sendMessage(text, conversationId, currentUserId);
                binding.etMessage.setText("");
            } else if (currentUserId == -1L) {
                Toast.makeText(this, "Đang tải thông tin người dùng...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupViewModel() {
        ChatDetailViewModelFactory factory = new ChatDetailViewModelFactory(ChatRepositoryImpl.getInstance(getApplication()));
        viewModel = new ViewModelProvider(this, factory).get(ChatDetailViewModel.class);
    }

    private void setupObservers() {
        // Observe profile để lấy currentUserId
        viewModel.profileState.observe(this, resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                currentUserId = resource.data.getId();
                // Cập nhật lại ID cho adapter
                if (adapter != null) {
                    adapter.setCurrentUserId(currentUserId);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        viewModel.messageState.observe(this, resource -> {
            if (resource == null) return;
            
            switch (resource.status) {
                case LOADING:
                    binding.loadingProgressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.loadingProgressBar.setVisibility(View.GONE);
                    if (resource.data != null) {
                        adapter.submitList(resource.data, () -> {
                            if (adapter.getItemCount() > 0) {
                                binding.rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                            }
                        });
                    }
                    break;
                case ERROR:
                    binding.loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
