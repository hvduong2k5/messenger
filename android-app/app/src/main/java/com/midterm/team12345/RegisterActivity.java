package com.midterm.team12345;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.midterm.team12345.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.registerRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupListeners();
    }

    private void setupListeners() {
        // Quay lại màn hình Login
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // Nút Đăng ký
        binding.btnRegister.setOnClickListener(v -> {
            performRegister();
        });

        // Lắng nghe thay đổi để validate và bật/tắt nút Đăng ký
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateAndEnableButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etFullName.addTextChangedListener(validationWatcher);
        binding.etUsername.addTextChangedListener(validationWatcher);
        binding.etEmail.addTextChangedListener(validationWatcher);
        binding.etPasswordReg.addTextChangedListener(validationWatcher);
        binding.etConfirmPassword.addTextChangedListener(validationWatcher);
        
        // Xử lý mất focus để hiện lỗi (setError)
        binding.etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String email = binding.etEmail.getText().toString().trim();
                if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.tilEmail.setError(getString(R.string.error_invalid_email));
                } else {
                    binding.tilEmail.setError(null);
                }
            }
        });
    }

    private void validateAndEnableButton() {
        String name = binding.etFullName.getText().toString().trim();
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPasswordReg.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        boolean isDataValid = !name.isEmpty() && 
                             !username.isEmpty() && 
                             Patterns.EMAIL_ADDRESS.matcher(email).matches() && 
                             password.length() >= 6 && 
                             confirmPassword.equals(password);

        binding.btnRegister.setEnabled(isDataValid);
    }

    private void performRegister() {
        // Trạng thái Loading
        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText("");
        binding.progressBar.setVisibility(View.VISIBLE);

        // Giả lập gọi API đăng ký (2 giây)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRegister.setText(R.string.btn_register);
            binding.btnRegister.setEnabled(true);
            
            // Đăng ký thành công -> Vào Main
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finishAffinity();
        }, 2000);
    }
}
