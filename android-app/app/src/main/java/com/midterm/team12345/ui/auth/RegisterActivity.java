package com.midterm.team12345.ui.auth;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.midterm.team12345.MainActivity;
import com.midterm.team12345.R;
import com.midterm.team12345.data.repository.AuthRepositoryImpl;
import com.midterm.team12345.databinding.ActivityRegisterBinding;
import com.midterm.team12345.util.Resource;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        AuthViewModelFactory factory = new AuthViewModelFactory(AuthRepositoryImpl.getInstance(getApplication()));
        viewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(binding.registerRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupListeners();
        observeViewModel();
        validateForm();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etUsername.addTextChangedListener(watcher);
        binding.etEmail.addTextChangedListener(watcher);
        binding.etPasswordReg.addTextChangedListener(watcher);
        binding.etConfirmPassword.addTextChangedListener(watcher);

        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etUsername.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPasswordReg.getText().toString().trim();
            viewModel.register(email, email, password, name);
        });
    }

    private void observeViewModel() {
        viewModel.getRegisterState().observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                showLoading(true);
            } else if (resource.status == Resource.Status.SUCCESS) {
                showLoading(false);
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (resource.status == Resource.Status.ERROR) {
                showLoading(false);
                String errorMsg = resource.message != null ? resource.message : "Registration failed";
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void validateForm() {
        String name = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPasswordReg.getText().toString().trim();
        String confirm = binding.etConfirmPassword.getText().toString().trim();

        boolean isValid = !name.isEmpty() && 
                          Patterns.EMAIL_ADDRESS.matcher(email).matches() && 
                          password.length() >= 8 &&
                          confirm.equals(password);

        binding.btnRegister.setEnabled(isValid);
        
        int btnColor = isValid ? getColor(R.color.messenger_blue) : ColorStateList.valueOf(getColor(R.color.brand_1)).getDefaultColor();
        int textColor = isValid ? getColor(android.R.color.white) : getColor(R.color.gray_text);
        
        binding.btnRegister.setBackgroundTintList(ColorStateList.valueOf(btnColor));
        binding.btnRegister.setTextColor(textColor);
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!isLoading);
        binding.btnRegister.setText(isLoading ? "" : getString(R.string.btn_register));
    }
}
