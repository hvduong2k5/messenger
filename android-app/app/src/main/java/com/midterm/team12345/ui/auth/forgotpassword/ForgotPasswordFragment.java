package com.midterm.team12345.ui.auth.forgotpassword;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import com.midterm.team12345.databinding.FragmentForgotPasswordBinding;
import com.midterm.team12345.ui.base.BaseFragment;
import com.midterm.team12345.utils.Resource;

public class ForgotPasswordFragment extends BaseFragment<FragmentForgotPasswordBinding, ForgotPasswordViewModel> {

    @Override
    protected FragmentForgotPasswordBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentForgotPasswordBinding.inflate(inflater, container, false);
    }

    @Override
    protected ForgotPasswordViewModel createViewModel() {
        return new ViewModelProvider(requireActivity()).get(ForgotPasswordViewModel.class);
    }

    @Override
    protected void setupViews() {
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setNewPassword(binding.etPassword.getText().toString());
                viewModel.setConfirmPassword(binding.etConfirmPassword.getText().toString());
                binding.btnVerify.setEnabled(viewModel.isPasswordValid());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etPassword.addTextChangedListener(passwordWatcher);
        binding.etConfirmPassword.addTextChangedListener(passwordWatcher);

        binding.btnVerify.setEnabled(viewModel.isPasswordValid());
        binding.btnVerify.setOnClickListener(v -> viewModel.resetPassword());

        binding.etPassword.requestFocus();
    }

    @Override
    protected void observeViewModel() {
        super.observeViewModel();
        viewModel.resetPasswordResult.observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            } else if (resource.status == Resource.Status.ERROR) {
                showError(resource.message);
            }
        });
    }
}
