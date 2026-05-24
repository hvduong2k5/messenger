package com.midterm.team12345.ui.settings;

import android.app.DatePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.midterm.team12345.R;
import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.data.repository.UserRepositoryImpl;
import com.midterm.team12345.databinding.ActivityEditProfileBinding;
import com.midterm.team12345.ui.base.BaseActivity;
import com.midterm.team12345.utils.Resource;

import java.util.Calendar;

public class EditProfileActivity extends BaseActivity<ActivityEditProfileBinding, EditProfileViewModel> {

    private TokenManager tokenManager;

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

        binding.etBirthday.setOnClickListener(v -> showDatePicker());

        binding.btnUpdate.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String status = binding.etBio.getText().toString().trim();
            // In a real app, you might have separate fields for passwords
            viewModel.updateProfile(email, status, null, null);
        });

        loadCurrentUserData();
    }

    private void loadCurrentUserData() {
        Long userId = tokenManager.getUserId();
        if (userId != null && userId != -1L) {
            viewModel.getLocalUser(userId).observe(this, user -> {
                if (user != null) {
                    binding.etFullName.setText(user.getUsername());
                    binding.etUsername.setText(user.getUsername());
                    binding.etBio.setText(user.getBio());
                    binding.etEmail.setText(user.getEmail());
                    
                    Glide.with(this)
                            .load(user.getAvatarUrl())
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .circleCrop()
                            .into(binding.ivAvatar);
                }
            });
        }
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> 
                        binding.etBirthday.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1), 
                year, month, day);
        datePickerDialog.show();
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel();
        
        viewModel.updateResult.observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, "Update failed: " + resource.message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
