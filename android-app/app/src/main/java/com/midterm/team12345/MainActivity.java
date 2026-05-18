package com.midterm.team12345;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.midterm.team12345.databinding.ActivityMainBinding;
import com.midterm.team12345.ui.chatlist.ChatListFragment;
import com.midterm.team12345.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupNavigation();

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new ChatListFragment());
        }

        // Start MessagingService for MQTT real-time updates
        com.midterm.team12345.data.local.TokenManager tokenManager = new com.midterm.team12345.data.local.TokenManager(this);
        Long userId = tokenManager.getUserId();
        String username = tokenManager.getUsername();
        if (userId != null && username != null) {
            android.content.Intent serviceIntent = new android.content.Intent(this, com.midterm.team12345.data.remote.mqtt.MessagingService.class);
            serviceIntent.putExtra(com.midterm.team12345.data.remote.mqtt.MessagingService.EXTRA_USERNAME, username);
            serviceIntent.putExtra(com.midterm.team12345.data.remote.mqtt.MessagingService.EXTRA_USER_ID, userId);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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
                fragment = new com.midterm.team12345.ui.friends.FriendsFragment();
            } else if (itemId == R.id.nav_settings) {
                fragment = new SettingsFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }



    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
