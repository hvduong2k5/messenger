package com.midterm.team12345;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
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
        setupBadges();

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new ChatListFragment());
        }
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_chats) {
                fragment = new ChatListFragment();
            } else if (itemId == R.id.nav_people) {
                // fragment = new PeopleFragment(); // To be implemented
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

    private void setupBadges() {
        // Tạo badge cho mục People (số 2 màu xanh lá)
        BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_people);
        badge.setVisible(true);
        badge.setNumber(2);
        badge.setBackgroundColor(ContextCompat.getColor(this, R.color.badge_green));
        badge.setBadgeTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
