package com.midterm.team12345.ui.auth;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.midterm.team12345.MainActivity;
import com.midterm.team12345.R;
import com.midterm.team12345.data.repository.AuthRepositoryImpl;
import com.midterm.team12345.databinding.ActivityLoginBinding;
import com.midterm.team12345.utils.Resource;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        // Khởi tạo ViewModel
        AuthViewModelFactory factory = new AuthViewModelFactory(AuthRepositoryImpl.getInstance(getApplication()));
        viewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(binding.loginMain, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupListeners();
        observeViewModel();
        validateForm();
    }

    private void setupListeners() {
        TextWatcher loginWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etUsernameEmail.addTextChangedListener(loginWatcher);
        binding.etPassword.addTextChangedListener(loginWatcher);

        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsernameEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            viewModel.login(username, password);
        });

        binding.btnCreateAccount.setOnClickListener(v -> {
            try {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "RegisterActivity chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateForm() {
        String email = binding.etUsernameEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        boolean isValid = !email.isEmpty() && password.length() >= 6;

        binding.btnLogin.setEnabled(isValid);
        if (isValid) {
            binding.btnLogin.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.messenger_blue)));
            binding.btnLogin.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            binding.btnLogin.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.btn_background_gray))); 
            binding.btnLogin.setTextColor(ContextCompat.getColor(this, R.color.btn_text_gray));
        }
    }

    private void observeViewModel() {
        viewModel.getLoginState().observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                setLoadingState(true);
            } else if (resource.status == Resource.Status.SUCCESS) {
                setLoadingState(false);
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (resource.status == Resource.Status.ERROR) {
                setLoadingState(false);
                Toast.makeText(this, resource.message != null ? resource.message : "Login failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnLogin.setText(isLoading ? "" : "LOG IN");
        binding.etUsernameEmail.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.btnCreateAccount.setEnabled(!isLoading);
    }
}
