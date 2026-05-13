package com.midterm.team12345;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.midterm.team12345.network.ApiService;
import com.midterm.team12345.network.AuthResponse;
import com.midterm.team12345.network.LoginRequest;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextInputEditText usernameEdit, passwordEdit;
    private MaterialButton loginButton, createAccountButton;
    private TextView forgotPasswordButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        initViews();
        initNetwork();
        setupListeners();
    }

    private void initViews() {
        usernameEdit = findViewById(R.id.username_edit);
        passwordEdit = findViewById(R.id.password_edit);
        loginButton = findViewById(R.id.login_button);
        createAccountButton = findViewById(R.id.create_account_button);
        forgotPasswordButton = findViewById(R.id.forgot_password_button);
    }

    private void initNetwork() {
        // Use 10.0.2.2 to point to your computer's localhost from the Android emulator
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void setupListeners() {
        TextWatcher loginTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        usernameEdit.addTextChangedListener(loginTextWatcher);
        passwordEdit.addTextChangedListener(loginTextWatcher);

        loginButton.setOnClickListener(v -> performLogin());

        createAccountButton.setOnClickListener(v -> {
            Toast.makeText(this, "Registration feature is coming soon", Toast.LENGTH_SHORT).show();
        });

        forgotPasswordButton.setOnClickListener(v -> {
            Toast.makeText(this, "Please contact support to reset your password", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLoginButtonState() {
        String username = usernameEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        // Button is enabled if fields are not empty and password is at least 6 characters
        boolean isEnabled = !username.isEmpty() && !password.isEmpty() && password.length() >= 6;
        loginButton.setEnabled(isEnabled);

        if (isEnabled) {
            loginButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.messenger_blue));
            loginButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            loginButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.btn_background_gray));
            loginButton.setTextColor(ContextCompat.getColor(this, R.color.btn_text_gray));
        }
    }

    private void performLogin() {
        String username = usernameEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        if (!isValidUsername(username)) {
            usernameEdit.setError("Please enter a valid email or phone number");
            usernameEdit.requestFocus();
            return;
        }

        // Map to LoginRequestDTO (usernameOrEmail, password)
        LoginRequest loginRequest = new LoginRequest(username, password);
        
        setLoadingState(true);

        apiService.login(loginRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                setLoadingState(false);
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginSuccess(response.body());
                } else {
                    handleLoginError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                setLoadingState(false);
                Log.e(TAG, "Network Error: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Connection failed. Please check your server.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        loginButton.setEnabled(!isLoading);
        loginButton.setText(isLoading ? "LOGGING IN..." : "LOG IN");
        usernameEdit.setEnabled(!isLoading);
        passwordEdit.setEnabled(!isLoading);
    }

    private void handleLoginSuccess(AuthResponse authResponse) {
        // Store JWT Token in SharedPreferences
        SharedPreferences prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        prefs.edit().putString("access_token", authResponse.getAccessToken()).apply();
        
        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
        
        // Navigate to MainActivity and clear backstack
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleLoginError(Response<AuthResponse> response) {
        if (response.code() == 401) {
            Toast.makeText(this, "Incorrect email/phone or password", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidUsername(String username) {
        boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(username).matches();
        boolean isPhone = Pattern.compile("^[0-9]{10,11}$").matcher(username).matches();
        return isEmail || isPhone;
    }
}
