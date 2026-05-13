package com.midterm.team12345.ui.chatdetail;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.midterm.team12345.databinding.ActivityChatDetailBinding;
import com.midterm.team12345.util.Resource;

public class ChatDetailActivity extends AppCompatActivity {

    private ActivityChatDetailBinding binding;
    private ChatDetailViewModel viewModel;
    private MessageAdapter adapter;
    private Long currentUserId = 1L; // Mock current user ID
    private Long conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        conversationId = getIntent().getLongExtra("CONVERSATION_ID", -1L);
        String partnerName = getIntent().getStringExtra("PARTNER_NAME");

        setupUI(partnerName);
        setupViewModel();
        setupObservers();
        
        viewModel.loadMessages(conversationId);
    }

    private void setupUI(String partnerName) {
        binding.tvPartnerName.setText(partnerName);
        
        adapter = new MessageAdapter(currentUserId);
        binding.rvMessages.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.et_message.getText().toString();
            if (!text.isEmpty()) {
                viewModel.sendMessage(text, currentUserId);
                binding.et_message.setText("");
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ChatDetailViewModel.class);
    }

    private void setupObservers() {
        viewModel.messageState.observe(this, resource -> {
            if (resource == null) return;
            
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                adapter.submitList(resource.data, () -> {
                    if (adapter.getItemCount() > 0) {
                        binding.rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                    }
                });
            }
        });
    }
}
