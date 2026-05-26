package com.midterm.team12345.ui.auth.forgotpassword;

import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import com.midterm.team12345.R;
import com.midterm.team12345.data.repository.AuthRepositoryImpl;
import com.midterm.team12345.databinding.ActivityForgotPasswordBinding;
import com.midterm.team12345.ui.base.BaseActivity;

public class ForgotPasswordActivity extends BaseActivity<ActivityForgotPasswordBinding, ForgotPasswordViewModel> {

    @Override
    protected ActivityForgotPasswordBinding inflateBinding(LayoutInflater inflater) {
        return ActivityForgotPasswordBinding.inflate(inflater);
    }

    @Override
    protected ForgotPasswordViewModel createViewModel() {
        ForgotPasswordViewModelFactory factory = new ForgotPasswordViewModelFactory(
                AuthRepositoryImpl.getInstance(getApplication())
        );
        return new ViewModelProvider(this, factory).get(ForgotPasswordViewModel.class);
    }

    @Override
    protected void setupViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            replaceFragment(new ForgotEmailFragment(), false);
        }
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel();
        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
        hideKeyboard();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
