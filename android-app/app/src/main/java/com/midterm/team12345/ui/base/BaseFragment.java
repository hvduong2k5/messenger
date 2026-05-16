package com.midterm.team12345.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

public abstract class BaseFragment<VB extends ViewBinding, VM extends BaseViewModel> extends Fragment {

    protected VB binding;
    protected VM viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = inflateBinding(inflater, container);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = createViewModel();
        setupViews();
        observeViewModel();
    }

    protected abstract VB inflateBinding(LayoutInflater inflater, ViewGroup container);

    protected abstract VM createViewModel();

    protected abstract void setupViews();

    protected void observeViewModel() {
        if (viewModel != null) {
            viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
                if (isLoading) showLoading();
                else hideLoading();
            });

            viewModel.errorMessage.observe(getViewLifecycleOwner(), message -> {
                if (message != null && !message.isEmpty()) {
                    showError(message);
                }
            });
        }
    }

    protected void showLoading() {}

    protected void hideLoading() {}

    protected void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
