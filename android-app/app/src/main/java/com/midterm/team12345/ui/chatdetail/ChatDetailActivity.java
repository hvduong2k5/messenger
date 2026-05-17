package com.midterm.team12345.ui.chatdetail;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.remote.dto.response.ConversationResponse;
import com.midterm.team12345.data.repository.ConversationRepositoryImpl;
import com.midterm.team12345.data.repository.MessageRepositoryImpl;
import com.midterm.team12345.data.repository.UserRepositoryImpl;
import com.midterm.team12345.databinding.ActivityChatDetailBinding;
import com.midterm.team12345.ui.conversationsettings.ConversationSettingsActivity;
import com.midterm.team12345.utils.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    private ActivityChatDetailBinding binding;
    private ChatDetailViewModel viewModel;
    private MessageAdapter adapter;
    private Long currentUserId = -1L;
    private Long conversationId;
    private ConversationResponse conversation;

    // File Picker Launcher
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    handlePickedFiles(result.getData());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        conversation = (ConversationResponse) getIntent().getSerializableExtra("conversation");
        
        if (conversation != null) {
            conversationId = conversation.getConversationId();
        } else {
            conversationId = getIntent().getLongExtra("CONVERSATION_ID", -1L);
            if (conversationId == -1L) {
                conversationId = getIntent().getLongExtra("conversationId", -1L);
            }
            if (conversationId == -1L) {
                conversationId = getIntent().getLongExtra("conversation_id", -1L);
            }
        }

        setupViewModel();
        setupObservers();
        
        viewModel.fetchMyProfile();
        setupUI();
        
        viewModel.loadMessages(conversationId);
    }

    private void setupUI() {
        if (conversation != null) {
            binding.tvPartnerName.setText(conversation.getConversationName());
            Glide.with(this)
                    .load(conversation.getAvatarUrl())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .into(binding.ivPartnerAvatar);
        } else {
            String partnerName = getIntent().getStringExtra("PARTNER_NAME");
            binding.tvPartnerName.setText(partnerName != null ? partnerName : "Chat");
        }

        View.OnClickListener goToSettings = v -> {
            if (conversation != null) {
                Intent intent = new Intent(ChatDetailActivity.this, ConversationSettingsActivity.class);
                intent.putExtra("conversation", conversation);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Đang tải thông tin hội thoại...", Toast.LENGTH_SHORT).show();
            }
        };

        binding.tvPartnerName.setOnClickListener(goToSettings);
        binding.ivPartnerAvatar.setOnClickListener(goToSettings);

        adapter = new MessageAdapter(currentUserId);
        binding.rvMessages.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> finish());

        // Setup Attach Button
        binding.btnAttach.setOnClickListener(v -> openFilePicker());
        
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMessage.getText().toString();
            // Cho phép gửi nếu có text hoặc có file đính kèm
            List<File> selectedFiles = viewModel.selectedFiles.getValue();
            boolean hasFiles = selectedFiles != null && !selectedFiles.isEmpty();

            if ((!text.trim().isEmpty() || hasFiles) && currentUserId != -1L) {
                if (conversationId == null || conversationId == -1L) {
                    Toast.makeText(this, "Lỗi: Không tìm thấy ID cuộc hội thoại!", Toast.LENGTH_SHORT).show();
                    return;
                }
                viewModel.sendMessage(text, conversationId, currentUserId);
                binding.etMessage.setText("");
            } else if (currentUserId == -1L) {
                Toast.makeText(this, "Đang tải thông tin người dùng...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Cho phép chọn mọi loại file
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Cho phép chọn nhiều file
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Files"));
    }

    private void handlePickedFiles(Intent data) {
        if (data.getClipData() != null) {
            // Nhiều file
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri uri = data.getClipData().getItemAt(i).getUri();
                addFileFromUri(uri);
            }
        } else if (data.getData() != null) {
            // Một file
            addFileFromUri(data.getData());
        }
    }

    private void addFileFromUri(Uri uri) {
        try {
            File file = getFileFromUri(uri);
            if (file != null) {
                viewModel.addSelectedFile(file);
                Toast.makeText(this, "Đã chọn: " + file.getName(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể mở file", Toast.LENGTH_SHORT).show();
        }
    }

    private File getFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) return null;

        // Tạo file tạm trong cache của app
        String fileName = "temp_file_" + System.currentTimeMillis();
        // Cố gắng lấy tên thật nếu có thể (tùy chọn)
        File tempFile = new File(getCacheDir(), fileName);
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();

        return tempFile;
    }

    private void setupViewModel() {
        ChatDetailViewModelFactory factory = new ChatDetailViewModelFactory(
                MessageRepositoryImpl.getInstance(this),
                ConversationRepositoryImpl.getInstance(this),
                UserRepositoryImpl.getInstance(this)
        );
        viewModel = new ViewModelProvider(this, factory).get(ChatDetailViewModel.class);
    }

    private void setupObservers() {
        viewModel.profileState.observe(this, resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                currentUserId = resource.data.getId();
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
                                binding.rvMessages.smoothScrollToPosition(0);
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

        // Observe danh sách file đã chọn để cập nhật UI nếu cần
        viewModel.selectedFiles.observe(this, files -> {
            if (!files.isEmpty()) {
                binding.btnAttach.setColorFilter(getResources().getColor(R.color.messenger_blue));
            } else {
                binding.btnAttach.clearColorFilter();
            }
        });
    }
}
