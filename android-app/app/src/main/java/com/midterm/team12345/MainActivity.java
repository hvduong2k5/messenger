package com.midterm.team12345;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.data.remote.mqtt.MessagingService;
import com.midterm.team12345.databinding.ActivityMainBinding;
import com.midterm.team12345.ui.chatlist.ChatListFragment;
import com.midterm.team12345.ui.friends.FriendsFragment;
import com.midterm.team12345.ui.settings.MyProfileFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupNavigation();
        setupBadges();

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new ChatListFragment());
        }

        // Start MessagingService for MQTT real-time updates
        TokenManager tokenManager = new TokenManager(this);
        Long userId = tokenManager.getUserId();
        String username = tokenManager.getUsername();
        if (userId != null && userId != -1L && username != null) {
            Intent serviceIntent = new Intent(this, MessagingService.class);
            serviceIntent.putExtra(MessagingService.EXTRA_USERNAME, username);
            serviceIntent.putExtra(MessagingService.EXTRA_USER_ID, userId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_chats) {
                fragment = new ChatListFragment();
            } else if (itemId == R.id.nav_people) {
                fragment = new FriendsFragment();
            } else if (itemId == R.id.nav_settings) {
                // Sử dụng MyProfileFragment cho Issue #100
                fragment = new MyProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void setupBadges() {
        // Hiển thị badge số 2 màu hồng cho tab Friends (nav_people)
        BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_people);
        badge.setVisible(false);
        // badge.setNumber(2);
        badge.setBackgroundColor(getColor(R.color.messenger_pink));
    }

    public void switchToChats() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_chats);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
