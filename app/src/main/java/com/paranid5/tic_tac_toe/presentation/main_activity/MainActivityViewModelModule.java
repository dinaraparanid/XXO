package com.paranid5.tic_tac_toe.presentation.main_activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.SavedStateHandle;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ViewModelComponent;
import dagger.hilt.android.scopes.ViewModelScoped;

@Module
@InstallIn(ViewModelComponent.class)
public final class MainActivityViewModelModule {
    @Provides
    @ViewModelScoped
    public static MainActivityPresenter providePresenter(final @NonNull SavedStateHandle savedStateHandle) {
        return new MainActivityPresenter();
    }

    @Provides
    @ViewModelScoped
    public static MainActivityUIHandler provideUIHandler() {
        return new MainActivityUIHandler();
    }
}
