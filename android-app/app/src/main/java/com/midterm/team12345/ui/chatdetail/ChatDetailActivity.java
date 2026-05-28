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
import com.midterm.team12345.data.remote.dto.response.ConversationResponseDTO;
import com.midterm.team12345.domain.model.Conversation;
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
    private SelectedFilesAdapter selectedFilesAdapter;
    private Long currentUserId = -1L;
    private Long partnerId = -1L;
    private Long conversationId;
    private Conversation conversation;
    private boolean shouldScrollToBottom = false;

    // File Picker Launcher
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    handlePickedFiles(result.getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            androidx.core.graphics.Insets ime = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime());
            int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
            return insets;
        });

        Object conversationExtra = getIntent().getSerializableExtra("conversation");
        if (conversationExtra instanceof Conversation) {
            conversation = (Conversation) conversationExtra;
        } else if (conversationExtra instanceof ConversationResponseDTO) {
            ConversationResponseDTO conversationDto = (ConversationResponseDTO) conversationExtra;
            long timestamp = 0;
            String timeStr = (conversationDto.getLastMessageCreatedAt() != null)
                    ? conversationDto.getLastMessageCreatedAt()
                    : conversationDto.getUpdatedAt();
            if (timeStr != null && !timeStr.isEmpty()) {
                try {
                    timestamp = Long.parseLong(timeStr);
                } catch (NumberFormatException ignored) {
                }
            }
            conversation = new Conversation(
                    conversationDto.getId(),
                    conversationDto.getName(),
                    conversationDto.getIsGroup(),
                    conversationDto.getAvatarUrl(),
                    timestamp,
                    conversationDto.getLastMessageContent(),
                    null,
                    timestamp,
                    conversationDto.getUnreadCount() != null ? conversationDto.getUnreadCount().intValue() : 0);
        }

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
        if (conversationId != null && conversationId != -1L) {
            new java.lang.Thread(() -> {
                com.midterm.team12345.data.local.database.MessengerDatabase db = 
                        com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(this);
                com.midterm.team12345.data.local.entity.ConversationEntity cEntity = 
                        db.conversationDao().getConversationByIdSync(conversationId);
                if (cEntity != null && !cEntity.getIsGroup()) {
                    Long myId = com.midterm.team12345.data.local.TokenManager.getInstance(this).getUserId();
                    java.util.List<com.midterm.team12345.data.local.entity.ConversationParticipantEntity> parts = 
                            db.conversationParticipantDao().getParticipantsForConversationSync(conversationId);
                    if (parts != null) {
                        for (com.midterm.team12345.data.local.entity.ConversationParticipantEntity p : parts) {
                            if (!p.getUserId().equals(myId)) {
                                partnerId = p.getUserId();
                                runOnUiThread(() -> {
                                    if (adapter != null) {
                                        adapter.setPartnerId(partnerId);
                                    }
                                });
                                break;
                            }
                        }
                    }
                }
            }).start();
            viewModel.loadMessages(conversationId);
        } else {
            Long partnerIdExtra = getIntent().getLongExtra("PARTNER_ID", -1L);
            String partnerName = getIntent().getStringExtra("PARTNER_NAME");
            if (partnerIdExtra != -1L) {
                new java.lang.Thread(() -> {
                    com.midterm.team12345.data.local.database.MessengerDatabase db = 
                            com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(this);
                    Long existingId = db.conversationDao().findOneToOneConversationWithPartnerSync(partnerIdExtra);
                    runOnUiThread(() -> {
                        if (existingId != null && existingId != -1L) {
                            conversationId = existingId;
                            partnerId = partnerIdExtra;
                            if (adapter != null) {
                                adapter.setPartnerId(partnerId);
                            }
                            db.conversationDao().getConversationById(conversationId).observe(ChatDetailActivity.this, entity -> {
                                if (entity != null) {
                                    conversation = com.midterm.team12345.data.mapper.ConversationMapper.toDomain(entity);
                                    binding.tvPartnerName.setText(conversation.getConversationName());
                                    String avatarUrl = conversation.getAvatarUrl();
                                    if (avatarUrl != null && !avatarUrl.startsWith("http")) {
                                        avatarUrl = com.midterm.team12345.data.remote.RetrofitClient.getBaseUrl()
                                                + (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
                                    }
                                    Glide.with(this)
                                            .load(avatarUrl)
                                            .placeholder(R.drawable.ic_avatar_placeholder)
                                            .fallback(R.drawable.ic_avatar_placeholder)
                                            .error(R.drawable.ic_avatar_placeholder)
                                            .into(binding.ivPartnerAvatar);
                                    
                                    if (adapter != null) {
                                        adapter.setRecipientAvatarUrl(conversation.getAvatarUrl());
                                        adapter.notifyDataSetChanged();
                                    }
                                    updateCallingButtonsVisibility();
                                }
                            });
                            viewModel.loadMessages(conversationId);
                        } else {
                            viewModel.startConversationWithPartner(partnerIdExtra, partnerName);
                        }
                    });
                }).start();
            }
        }
    }

    private void setupUI() {
        if (conversation != null) {
            binding.tvPartnerName.setText(conversation.getConversationName());
            String avatarUrl = conversation.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.startsWith("http")) {
                avatarUrl = com.midterm.team12345.data.remote.RetrofitClient.getBaseUrl()
                        + (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
            }
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .fallback(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
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
        if (conversation != null) {
            adapter.setRecipientAvatarUrl(conversation.getAvatarUrl());
        }
        binding.rvMessages.setAdapter(adapter);
        binding.rvMessages.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                androidx.recyclerview.widget.LinearLayoutManager layoutManager = 
                        (androidx.recyclerview.widget.LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;
                
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();
                
                if (totalItemCount > 0 && lastVisibleItemPosition >= totalItemCount - 5) {
                    viewModel.loadMoreMessages();
                }
            }
        });

        selectedFilesAdapter = new SelectedFilesAdapter(file -> viewModel.removeSelectedFile(file));
        binding.rvSelectedFiles.setAdapter(selectedFilesAdapter);

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
                shouldScrollToBottom = true;
                viewModel.sendMessage(text, conversationId, currentUserId);
                binding.etMessage.setText("");
            } else if (currentUserId == -1L) {
                Toast.makeText(this, "Đang tải thông tin người dùng...", Toast.LENGTH_SHORT).show();
            }
        });
        updateCallingButtonsVisibility();
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
        if (inputStream == null)
            return null;

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
        com.midterm.team12345.data.local.database.MessengerDatabase db = 
                com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(this);
        ChatDetailViewModelFactory factory = new ChatDetailViewModelFactory(
                MessageRepositoryImpl.getInstance(this),
                ConversationRepositoryImpl.getInstance(getApplication()),
                UserRepositoryImpl.getInstance(getApplication()),
                db.conversationDao(),
                db.messageDao());
        viewModel = new ViewModelProvider(this, factory).get(ChatDetailViewModel.class);
        Long myId = com.midterm.team12345.data.local.TokenManager.getInstance(this).getUserId();
        viewModel.setCurrentUserId(myId);
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
            if (resource == null)
                return;

            switch (resource.status) {
                case LOADING:
                    binding.loadingProgressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.loadingProgressBar.setVisibility(View.GONE);
                    if (resource.data != null) {
                        int previousItemCount = adapter.getItemCount();
                        adapter.submitList(resource.data, () -> {
                            if (adapter.getItemCount() > 0) {
                                androidx.recyclerview.widget.LinearLayoutManager layoutManager = 
                                        (androidx.recyclerview.widget.LinearLayoutManager) binding.rvMessages.getLayoutManager();
                                int firstVisible = layoutManager != null ? layoutManager.findFirstVisibleItemPosition() : -1;
                                
                                if (previousItemCount == 0 || shouldScrollToBottom || firstVisible <= 1) {
                                    binding.rvMessages.scrollToPosition(0);
                                    shouldScrollToBottom = false;
                                }
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

        viewModel.createConversationState.observe(this, resource -> {
            if (resource == null)
                return;

            if (resource.status == Resource.Status.LOADING) {
                binding.loadingProgressBar.setVisibility(View.VISIBLE);
            } else if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                binding.loadingProgressBar.setVisibility(View.GONE);
                ConversationResponseDTO dto = resource.data;
                conversationId = dto.getId();
 
                conversation = new Conversation(
                        dto.getId(),
                        dto.getName(),
                        dto.getIsGroup(),
                        dto.getAvatarUrl(),
                        System.currentTimeMillis(),
                        dto.getLastMessageContent(),
                        null,
                        System.currentTimeMillis(),
                        dto.getUnreadCount() != null ? dto.getUnreadCount().intValue() : 0);
 
                if (adapter != null) {
                    adapter.setRecipientAvatarUrl(conversation.getAvatarUrl());
                    adapter.notifyDataSetChanged();
                }

                binding.tvPartnerName.setText(conversation.getConversationName());
                String avatarUrl = conversation.getAvatarUrl();
                if (avatarUrl != null && !avatarUrl.startsWith("http")) {
                    avatarUrl = com.midterm.team12345.data.remote.RetrofitClient.getBaseUrl()
                            + (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
                }
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .fallback(R.drawable.ic_avatar_placeholder)
                        .error(R.drawable.ic_avatar_placeholder)
                        .into(binding.ivPartnerAvatar);
                updateCallingButtonsVisibility();
            } else if (resource.status == Resource.Status.ERROR) {
                binding.loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Không thể tạo cuộc hội thoại: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe danh sách file đã chọn để cập nhật UI nếu cần
        viewModel.selectedFiles.observe(this, files -> {
            if (files != null && !files.isEmpty()) {
                binding.btnAttach.setColorFilter(getResources().getColor(R.color.messenger_blue));
                selectedFilesAdapter.setFiles(files);
                binding.rvSelectedFiles.setVisibility(View.VISIBLE);
            } else {
                binding.btnAttach.clearColorFilter();
                selectedFilesAdapter.setFiles(new ArrayList<>());
                binding.rvSelectedFiles.setVisibility(View.GONE);
            }
        });
    }

    private void updateCallingButtonsVisibility() {
        if (conversation != null) {
            if (conversation.getGroup()) {
                binding.btnCall.setVisibility(View.GONE);
                binding.btnVideoCall.setVisibility(View.GONE);
            } else {
                binding.btnCall.setVisibility(View.VISIBLE);
                binding.btnVideoCall.setVisibility(View.VISIBLE);
            }
        } else {
            Long partnerIdExtra = getIntent().getLongExtra("PARTNER_ID", -1L);
            if (partnerIdExtra != -1L) {
                binding.btnCall.setVisibility(View.VISIBLE);
                binding.btnVideoCall.setVisibility(View.VISIBLE);
            } else {
                binding.btnCall.setVisibility(View.GONE);
                binding.btnVideoCall.setVisibility(View.GONE);
            }
        }
    }
}
