package com.midterm.team12345;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.databinding.ActivitySplashBinding;
import com.midterm.team12345.ui.auth.LoginActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private static final int SPLASH_DURATION = 1500;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);

        // Ensure local user profile exists for offline/local use
        Long userId = tokenManager.getUserId();
        String username = tokenManager.getUsername();
        if (userId != null && userId != -1L) {
            new Thread(() -> {
                try {
                    com.midterm.team12345.data.local.database.MessengerDatabase db =
                            com.midterm.team12345.data.local.database.MessengerDatabase.getInstance(this);
                    com.midterm.team12345.data.local.entity.UserEntity entity = db.userDao().getUserByIdSync(userId);
                    if (entity == null) {
                        entity = new com.midterm.team12345.data.local.entity.UserEntity();
                        entity.setId(userId);
                        entity.setUsername(username != null ? username : "");
                        db.userDao().insertUser(entity);
                    } else if (entity.getUsername() == null || entity.getUsername().isEmpty()) {
                        entity.setUsername(username != null ? username : "");
                        db.userDao().insertUser(entity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.splashRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Logo fade-in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        binding.ivLogo.startAnimation(fadeIn);

        // Transition logic
        new Handler(Looper.getMainLooper()).postDelayed(this::checkSessionAndNavigate, SPLASH_DURATION);
    }

    private void checkSessionAndNavigate() {
        String token = tokenManager.getToken();
        Intent intent;
        if (token != null && !token.isEmpty()) {
            // Already logged in - Navigate to MainActivity (which hosts ChatListFragment)
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            // Need to login
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
