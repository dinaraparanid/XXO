package com.paranid5.tic_tac_toe.presentation.main_fragment;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.presentation.ObservableViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public final class MainFragmentViewModel extends ObservableViewModel<MainFragmentPresenter, MainFragmentUIHandler> {

    @NonNull
    final MainFragmentPresenter presenter;

    @NonNull
    final MainFragmentUIHandler handler;

    @Inject
    public MainFragmentViewModel(
            final @NonNull MainFragmentPresenter presenter,
            final @NonNull MainFragmentUIHandler handler
    ) {
        this.presenter = presenter;
        this.handler = handler;
    }

    @Override
    @NonNull
    public MainFragmentPresenter getPresenter() { return presenter; }

    @Override
    @NonNull
    public MainFragmentUIHandler getHandler() { return handler; }

    @NonNull
    private final MutableLiveData<Boolean> isPlayButtonClickedMutableState = new MutableLiveData<>(false);

    @NonNull
    private final MutableLiveData<Boolean> isSettingsButtonClickedMutableState = new MutableLiveData<>(false);

    @NonNull
    public LiveData<Boolean> getPlayButtonClickedState() {
        return isPlayButtonClickedMutableState;
    }

    public void onPlayButtonClicked() { isPlayButtonClickedMutableState.postValue(true); }

    public void onPlayButtonClickedFinished() {
        isPlayButtonClickedMutableState.postValue(false);
    }

    @NonNull
    public LiveData<Boolean> getSettingsButtonClickedState() {
        return isSettingsButtonClickedMutableState;
    }

    public void onSettingsButtonClicked() {
        isSettingsButtonClickedMutableState.postValue(true);
    }

    public void onSettingsButtonClickedFinished() {
        isSettingsButtonClickedMutableState.postValue(false);
    }
}
