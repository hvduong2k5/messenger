package com.midterm.team12345.ui.auth.forgotpassword;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.midterm.team12345.data.remote.dto.request.ForgotPasswordRequestDTO;
import com.midterm.team12345.data.remote.dto.request.ResetPasswordRequestDTO;
import com.midterm.team12345.data.remote.dto.response.ForgotPasswordResponseDTO;
import com.midterm.team12345.domain.repository.AuthRepository;
import com.midterm.team12345.ui.base.BaseViewModel;
import com.midterm.team12345.utils.Resource;

public class ForgotPasswordViewModel extends BaseViewModel {
    private final AuthRepository authRepository;

    private final MutableLiveData<String> _email = new MutableLiveData<>("");
    public final LiveData<String> email = _email;

    private final MutableLiveData<String> _otpCode = new MutableLiveData<>("");
    public final LiveData<String> otpCode = _otpCode;

    private final MutableLiveData<String> _newPassword = new MutableLiveData<>("");
    public final LiveData<String> newPassword = _newPassword;

    private final MutableLiveData<String> _confirmPassword = new MutableLiveData<>("");
    public final LiveData<String> confirmPassword = _confirmPassword;

    private final MutableLiveData<Resource<ForgotPasswordResponseDTO>> _forgotPasswordResult = new MutableLiveData<>();
    public final LiveData<Resource<ForgotPasswordResponseDTO>> forgotPasswordResult = _forgotPasswordResult;

    private final MutableLiveData<Resource<ForgotPasswordResponseDTO>> _resetPasswordResult = new MutableLiveData<>();
    public final LiveData<Resource<ForgotPasswordResponseDTO>> resetPasswordResult = _resetPasswordResult;

    public ForgotPasswordViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void setEmail(String email) {
        _email.setValue(email);
    }

    public void setOtpCode(String otp) {
        _otpCode.setValue(otp);
    }

    public void setNewPassword(String password) {
        _newPassword.setValue(password);
    }

    public void setConfirmPassword(String password) {
        _confirmPassword.setValue(password);
    }

    public boolean isEmailValid() {
        String emailStr = _email.getValue();
        return emailStr != null && android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches();
    }

    public boolean isPasswordValid() {
        String passwordStr = _newPassword.getValue();
        String confirmStr = _confirmPassword.getValue();
        return passwordStr != null && passwordStr.length() >= 8 && passwordStr.equals(confirmStr);
    }

    public boolean isOtpValid() {
        String otpStr = _otpCode.getValue();
        return otpStr != null && otpStr.length() == 6;
    }

    public void requestForgotPassword() {
        if (!isEmailValid()) {
            _errorMessage.setValue("Invalid email format");
            return;
        }

        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO(_email.getValue());
        _forgotPasswordResult.setValue(Resource.loading(null));
        _isLoading.setValue(true);
        
        authRepository.forgotPassword(request).observeForever(resource -> {
            _forgotPasswordResult.setValue(resource);
            if (resource.status != Resource.Status.LOADING) {
                _isLoading.setValue(false);
            }
            if (resource.status == Resource.Status.ERROR) {
                _errorMessage.setValue(resource.message);
            }
        });
    }

    public void resetPassword() {
        if (!isEmailValid() || !isOtpValid() || !isPasswordValid()) {
            _errorMessage.setValue("Please check your input");
            return;
        }

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO(
                _email.getValue(),
                _otpCode.getValue(),
                _newPassword.getValue()
        );

        _resetPasswordResult.setValue(Resource.loading(null));
        _isLoading.setValue(true);

        authRepository.resetPassword(request).observeForever(resource -> {
            _resetPasswordResult.setValue(resource);
            if (resource.status != Resource.Status.LOADING) {
                _isLoading.setValue(false);
            }
            if (resource.status == Resource.Status.ERROR) {
                _errorMessage.setValue(resource.message);
            }
        });
    }
}
