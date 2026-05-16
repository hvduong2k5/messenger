package com.midterm.team12345.ui.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public abstract class BaseViewModel extends ViewModel {
    protected final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    protected final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    public void showLoading() {
        _isLoading.setValue(true);
    }

    public void hideLoading() {
        _isLoading.setValue(false);
    }

    public void setError(String message) {
        _errorMessage.setValue(message);
    }
}
