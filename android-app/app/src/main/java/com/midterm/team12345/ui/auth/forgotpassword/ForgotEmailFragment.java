package com.midterm.team12345.ui.auth.forgotpassword;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import com.midterm.team12345.databinding.FragmentForgotEmailBinding;
import com.midterm.team12345.ui.base.BaseFragment;
import com.midterm.team12345.utils.Resource;

public class ForgotEmailFragment extends BaseFragment<FragmentForgotEmailBinding, ForgotPasswordViewModel> {

    @Override
    protected FragmentForgotEmailBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentForgotEmailBinding.inflate(inflater, container, false);
    }

    @Override
    protected ForgotPasswordViewModel createViewModel() {
        return new ViewModelProvider(requireActivity()).get(ForgotPasswordViewModel.class);
    }

    @Override
    protected void setupViews() {
        binding.etEmail.setText(viewModel.email.getValue());
        
        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setEmail(s.toString());
                binding.btnNext.setEnabled(viewModel.isEmailValid());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnNext.setEnabled(viewModel.isEmailValid());
        binding.btnNext.setOnClickListener(v -> viewModel.requestForgotPassword());

        binding.etEmail.requestFocus();
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel();
        viewModel.forgotPasswordResult.observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                if (getActivity() instanceof ForgotPasswordActivity) {
                    ((ForgotPasswordActivity) getActivity()).replaceFragment(new ForgotOtpFragment(), true);
                }
            } else if (resource.status == Resource.Status.ERROR) {
                binding.tilEmail.setError(resource.message);
            }
        });
    }
}
