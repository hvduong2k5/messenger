package com.midterm.team12345.ui.settings;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.midterm.team12345.R;
import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.data.local.entity.UserEntity;
import com.midterm.team12345.data.remote.dto.request.UpdateProfileRequestDTO;
import com.midterm.team12345.data.repository.UserRepositoryImpl;
import com.midterm.team12345.databinding.ActivityEditProfileBinding;
import com.midterm.team12345.ui.base.BaseActivity;
import com.midterm.team12345.utils.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class EditProfileActivity extends BaseActivity<ActivityEditProfileBinding, EditProfileViewModel> {

    private TokenManager tokenManager;
    private UserEntity currentUser;
    private Uri selectedImageUri;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    protected ActivityEditProfileBinding inflateBinding(LayoutInflater inflater) {
        return ActivityEditProfileBinding.inflate(inflater);
    }

    @Override
    protected EditProfileViewModel createViewModel() {
        EditProfileViewModelFactory factory = new EditProfileViewModelFactory(
                UserRepositoryImpl.getInstance(getApplication())
        );
        return new ViewModelProvider(this, factory).get(EditProfileViewModel.class);
    }

    @Override
    protected void setupViews() {
        tokenManager = new TokenManager(this);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // 1. Tích hợp Image Picker
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                Glide.with(this).load(uri).circleCrop().into(binding.ivAvatar);
            }
        });

        binding.ivChangeAvatar.setOnClickListener(v -> 
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build())
        );

        // 2. Nút Lưu
        binding.btnUpdate.setOnClickListener(v -> performUpdate());

        setupChangeDetection();
        loadCurrentUserData();
    }

    private void loadCurrentUserData() {
        Long userId = tokenManager.getUserId();
        if (userId != null) {
            viewModel.getLocalUser(userId).observe(this, user -> {
                if (user != null && currentUser == null) {
                    currentUser = user;
                    binding.tvDisplayFullName.setText(user.getUsername());
                    binding.etEmail.setText(user.getEmail());
                    
                    Glide.with(this)
                            .load(user.getAvatarUrl())
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .fallback(R.drawable.ic_avatar_placeholder)
                            .error(R.drawable.ic_avatar_placeholder)
                            .circleCrop()
                            .into(binding.ivAvatar);
                }
            });
        }
    }

    private void setupChangeDetection() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (currentUser != null) {
                    String inputEmail = binding.etEmail.getText().toString().trim();
                    String inputPassword = binding.etNewPassword.getText().toString().trim();
                    boolean passwordRequired = !inputEmail.equals(currentUser.getEmail()) || !inputPassword.isEmpty();
                    binding.layoutSecurity.setVisibility(passwordRequired ? View.VISIBLE : View.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        binding.etEmail.addTextChangedListener(watcher);
        binding.etNewPassword.addTextChangedListener(watcher);
    }

    private void performUpdate() {
        String email = binding.etEmail.getText().toString().trim();
        String status = currentUser != null ? currentUser.getBio() : null;
        String newPassword = binding.etNewPassword.getText().toString().trim();
        String oldPassword = binding.etCurrentPassword.getText().toString().trim();

        if (binding.layoutSecurity.getVisibility() == View.VISIBLE && oldPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu hiện tại để xác nhận", Toast.LENGTH_SHORT).show();
            return;
        }

        MultipartBody.Part avatarPart = null;
        if (selectedImageUri != null) {
            avatarPart = prepareFilePart("avatar", selectedImageUri);
        }

        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO();
        if (!email.equals(currentUser.getEmail())) dto.setEmail(email);
        dto.setStatus(status);
        if (!newPassword.isEmpty()) dto.setPassword(newPassword);
        if (!oldPassword.isEmpty()) dto.setOldPassword(oldPassword);

        RequestBody dataPayload = RequestBody.create(
                MediaType.parse("application/json"), new Gson().toJson(dto));

        viewModel.updateProfile(avatarPart, dataPayload);
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        try {
            File file = new File(getCacheDir(), "upload_avatar.jpg");
            InputStream is = getContentResolver().openInputStream(fileUri);
            FileOutputStream os = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) os.write(buffer, 0, length);
            os.close(); is.close();
            return MultipartBody.Part.createFormData(partName, file.getName(), 
                RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), file));
        } catch (Exception e) { return null; }
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel();
        viewModel.updateResult.observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (resource.status == Resource.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else if (resource.status == Resource.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
