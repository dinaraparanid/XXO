package com.paranid5.tic_tac_toe.presentation.main_fragment;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.presentation.ObservableViewModel;
import com.paranid5.tic_tac_toe.presentation.StateChangedCallback;

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
    private final MutableLiveData<StateChangedCallback.State<Void>> isPlayButtonClickedMutableState =
            new MutableLiveData<>(new StateChangedCallback.State<>());

    @NonNull
    public LiveData<StateChangedCallback.State<Void>> getPlayButtonClickedState() {
        return isPlayButtonClickedMutableState;
    }

    public void onPlayButtonClicked() {
        isPlayButtonClickedMutableState.postValue(
                new StateChangedCallback.State<>(true)
        );
    }

    public void onPlayButtonClickedFinished() {
        isPlayButtonClickedMutableState.postValue(
                new StateChangedCallback.State<>(false)
        );
    }

    @NonNull
    private final MutableLiveData<StateChangedCallback.State<Void>> isSettingsButtonClickedMutableState =
            new MutableLiveData<>(new StateChangedCallback.State<>());

    @NonNull
    public LiveData<StateChangedCallback.State<Void>> getSettingsButtonClickedState() {
        return isSettingsButtonClickedMutableState;
    }

    public void onSettingsButtonClicked() {
        isSettingsButtonClickedMutableState.postValue(
                new StateChangedCallback.State<>(true)
        );
    }

    public void onSettingsButtonClickedFinished() {
        isSettingsButtonClickedMutableState.postValue(
                new StateChangedCallback.State<>(false)
        );
    }
}
