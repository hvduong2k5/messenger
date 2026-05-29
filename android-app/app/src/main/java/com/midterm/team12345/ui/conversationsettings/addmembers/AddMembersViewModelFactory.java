package com.midterm.team12345.ui.conversationsettings.addmembers;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class AddMembersViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public AddMembersViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AddMembersViewModel.class)) {
            return (T) new AddMembersViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
