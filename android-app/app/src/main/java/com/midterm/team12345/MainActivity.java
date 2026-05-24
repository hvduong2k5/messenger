package com.midterm.team12345;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.badge.BadgeDrawable;
import com.midterm.team12345.databinding.ActivityMainBinding;
import com.midterm.team12345.ui.friends.FriendsFragment;
import com.midterm.team12345.ui.settings.MyProfileFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            loadFragment(new FriendsFragment());
        }

        setupNavigation();
        setupBadges();
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_chats) {
                // Future: loadFragment(new ChatListFragment());
                return true;
            } else if (itemId == R.id.nav_people) {
                loadFragment(new FriendsFragment());
                return true;
            } else if (itemId == R.id.nav_settings) {
                loadFragment(new MyProfileFragment());
                return true;
            }
            return false;
        });
        
        // Mặc định chọn tab People (Friends) khi vào app
        binding.bottomNavigation.setSelectedItemId(R.id.nav_people);
    }

    private void setupBadges() {
        // Badge số 2 màu hồng cho tab Friends như trong thiết kế
        BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_people);
        badge.setVisible(true);
        badge.setNumber(2);
        badge.setBackgroundColor(getColor(R.color.messenger_pink));
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
