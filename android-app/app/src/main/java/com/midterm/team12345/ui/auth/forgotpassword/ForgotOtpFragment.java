package com.midterm.team12345.ui.auth.forgotpassword;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import com.midterm.team12345.databinding.FragmentForgotOtpBinding;
import com.midterm.team12345.ui.base.BaseFragment;

public class ForgotOtpFragment extends BaseFragment<FragmentForgotOtpBinding, ForgotPasswordViewModel> {

    private TextView[] otpBoxes;

    @Override
    protected FragmentForgotOtpBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentForgotOtpBinding.inflate(inflater, container, false);
    }

    @Override
    protected ForgotPasswordViewModel createViewModel() {
        return new ViewModelProvider(requireActivity()).get(ForgotPasswordViewModel.class);
    }

    @Override
    protected void setupViews() {
        binding.tvEmail.setText(viewModel.email.getValue());

        otpBoxes = new TextView[]{
                binding.tvOtp1, binding.tvOtp2, binding.tvOtp3,
                binding.tvOtp4, binding.tvOtp5, binding.tvOtp6
        };

        binding.etOtp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String otp = s.toString();
                for (int i = 0; i < otpBoxes.length; i++) {
                    if (i < otp.length()) {
                        otpBoxes[i].setText(String.valueOf(otp.charAt(i)));
                        otpBoxes[i].setTextColor(getResources().getColor(android.R.color.black));
                    } else {
                        otpBoxes[i].setText("_");
                        otpBoxes[i].setTextColor(getResources().getColor(android.R.color.darker_gray));
                    }
                }
                viewModel.setOtpCode(otp);
                binding.btnNext.setEnabled(viewModel.isOtpValid());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnNext.setEnabled(viewModel.isOtpValid());
        binding.btnNext.setOnClickListener(v -> {
            if (getActivity() instanceof ForgotPasswordActivity) {
                ((ForgotPasswordActivity) getActivity()).replaceFragment(new ForgotPasswordFragment(), true);
            }
        });

        binding.btnResend.setOnClickListener(v -> viewModel.requestForgotPassword());

        // Focus and show keyboard
        binding.etOtp.requestFocus();
        binding.llOtpContainer.setOnClickListener(v -> {
            binding.etOtp.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) 
                    requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(binding.etOtp, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        });
    }
}
