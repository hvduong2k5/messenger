package com.midterm.team12345.ui.conversationsettings.addmembers;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.midterm.team12345.databinding.ActivityAddMembersBinding;
import com.midterm.team12345.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AddMembersActivity extends AppCompatActivity implements AddMembersAdapter.OnUserSelectedListener {

    public static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    public static final String EXTRA_EXISTING_PARTICIPANTS = "extra_existing_participants";

    private ActivityAddMembersBinding binding;
    private AddMembersViewModel viewModel;
    private AddMembersAdapter adapter;
    private Long conversationId;
    private Set<Long> selectedUserIds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMembersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        conversationId = getIntent().getLongExtra(EXTRA_CONVERSATION_ID, -1L);
        ArrayList<Long> existingIds = (ArrayList<Long>) getIntent().getSerializableExtra(EXTRA_EXISTING_PARTICIPANTS);

        if (conversationId == -1L) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID hội thoại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        AddMembersViewModelFactory factory = new AddMembersViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(AddMembersViewModel.class);
        viewModel.setExistingParticipants(existingIds != null ? existingIds : new ArrayList<>());
        viewModel.init(conversationId);

        setupUI();
        setupObservers();

        // Auto-show keyboard
        binding.etSearch.requestFocus();
        showKeyboard(binding.etSearch);
    }

    private void setupUI() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.inflateMenu(R.menu.menu_add_members);
        android.view.MenuItem doneItem = binding.toolbar.getMenu().findItem(R.id.action_done);
        if (doneItem != null) {
            doneItem.setEnabled(false); // Default disabled
        }
        
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_done) {
                if (selectedUserIds != null && !selectedUserIds.isEmpty()) {
                    viewModel.addParticipants(conversationId, selectedUserIds);
                }
                return true;
            }
            return false;
        });

        adapter = new AddMembersAdapter(this);
        binding.rvMembers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMembers.setAdapter(adapter);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.onSearchQueryChanged(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    private void setupObservers() {
        viewModel.friends.observe(this, friends -> {
            adapter.submitList(friends);
        });

        viewModel.addSuccessEvent.observe(this, unused -> {
            Toast.makeText(this, "Đã thêm thành viên thành công", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });

        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            android.view.MenuItem doneItem = binding.toolbar.getMenu().findItem(R.id.action_done);
            if (doneItem != null) {
                doneItem.setEnabled(!isLoading);
            }
        });
    }

    @Override
    public void onSelectionChanged(int count, Set<Long> selectedIds) {
        this.selectedUserIds = selectedIds;
        boolean hasSelection = count > 0;
        android.view.MenuItem doneItem = binding.toolbar.getMenu().findItem(R.id.action_done);
        if (doneItem != null) {
            doneItem.setEnabled(hasSelection);
        }
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
