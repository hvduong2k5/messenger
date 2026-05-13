package com.midterm.team12345.ui.creategroup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.midterm.team12345.data.repository.ChatRepositoryImpl;
import com.midterm.team12345.databinding.ActivityCreateGroupBinding;
import com.midterm.team12345.util.Resource;

import java.util.ArrayList;

public class CreateGroupActivity extends AppCompatActivity {

    private ActivityCreateGroupBinding binding;
    private CreateGroupViewModel viewModel;
    private MemberAdapter memberAdapter;
    private SelectedMemberAdapter selectedMemberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // In a real app, use Dependency Injection or a global repository instance
        CreateGroupViewModelFactory factory = new CreateGroupViewModelFactory(new ChatRepositoryImpl());
        viewModel = new ViewModelProvider(this, factory).get(CreateGroupViewModel.class);

        setupRecyclerViews();
        setupObservers();
        setupListeners();
    }

    private void setupRecyclerViews() {
        memberAdapter = new MemberAdapter(user -> viewModel.toggleUserSelection(user));
        binding.rvFriends.setAdapter(memberAdapter);

        selectedMemberAdapter = new SelectedMemberAdapter(user -> viewModel.toggleUserSelection(user));
        binding.rvSelectedMembers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvSelectedMembers.setAdapter(selectedMemberAdapter);
    }

    private void setupObservers() {
        viewModel.getFriends().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                memberAdapter.setMembers(resource.data);
            }
        });

        viewModel.getSelectedUsers().observe(this, users -> {
            selectedMemberAdapter.setData(users);
            memberAdapter.setSelectedIds(users);
            
            if (users.isEmpty()) {
                binding.rvSelectedMembers.setVisibility(View.GONE);
                binding.tvToolbarTitle.setText("New Group");
            } else {
                binding.rvSelectedMembers.setVisibility(View.VISIBLE);
                binding.tvToolbarTitle.setText("Selected: " + users.size());
            }
            
            binding.btnCreate.setEnabled(users.size() >= 2);
        });

        viewModel.getCreateState().observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnCreate.setEnabled(false);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCreate.setEnabled(true);
                if (resource.status == Resource.Status.SUCCESS) {
                    Toast.makeText(this, "Group created successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Or navigate to ChatDetailActivity
                } else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.btnCreate.setOnClickListener(v -> {
            String groupName = binding.etGroupName.getText().toString();
            viewModel.createGroup(groupName);
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter logic would go here, updating the memberAdapter
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
