package com.midterm.team12345.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

public abstract class BaseActivity<VB extends ViewBinding, VM extends BaseViewModel> extends AppCompatActivity {

    protected VB binding;
    protected VM viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = inflateBinding(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = createViewModel();
        
        setupViews();
        observeViewModel();
    }

    protected abstract VB inflateBinding(LayoutInflater inflater);

    protected abstract VM createViewModel();

    protected abstract void setupViews();

    protected void observeViewModel() {
        if (viewModel != null) {
            viewModel.isLoading.observe(this, isLoading -> {
                if (isLoading) showLoading();
                else hideLoading();
            });

            viewModel.errorMessage.observe(this, message -> {
                if (message != null && !message.isEmpty()) {
                    showError(message);
                }
            });
        }
    }

    protected void showLoading() {
        // Implement default loading if any
    }

    protected void hideLoading() {
        // Hide default loading
    }

    protected void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
