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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.midterm.team12345.MainActivity;
import com.midterm.team12345.R;
import com.midterm.team12345.data.repository.AuthRepositoryImpl;
import com.midterm.team12345.databinding.ActivityLoginBinding;
import com.midterm.team12345.util.Resource;

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
            String email = binding.etUsernameEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            viewModel.login(email, password);
        });

        binding.btnCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void validateForm() {
        String email = binding.etUsernameEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        boolean isValid = !email.isEmpty() && password.length() >= 6;

        binding.btnLogin.setEnabled(isValid);
        if (isValid) {
            binding.btnLogin.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.messenger_blue)));
            binding.btnLogin.setTextColor(getColor(android.R.color.white));
        } else {
            binding.btnLogin.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.brand_1))); 
            binding.btnLogin.setTextColor(getColor(R.color.gray_text));
        }
    }

    private void observeViewModel() {
        viewModel.getLoginState().observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                setLoadingState(true);
            } else if (resource.status == Resource.Status.SUCCESS) {
                setLoadingState(false);
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (resource.status == Resource.Status.ERROR) {
                setLoadingState(false);
                String message = resource.message != null ? resource.message : "Login failed";
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnLogin.setText(isLoading ? "" : getString(R.string.btn_login));
        binding.etUsernameEmail.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.btnCreateAccount.setEnabled(!isLoading);
    }
}
