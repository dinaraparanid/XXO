package com.paranid5.tic_tac_toe.presentation.main_activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.paranid5.tic_tac_toe.presentation.ObservableViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public final class MainActivityViewModel extends ObservableViewModel<MainActivityPresenter, MainActivityUIHandler> {
    @NonNull
    private final MediatorLiveData<Boolean> isPlayButtonClickedStateMerger = new MediatorLiveData<>(false);

    @NonNull
    private final MutableLiveData<Boolean> isPlayButtonClickedMutableState = new MutableLiveData<>(false);

    @NonNull
    private final MediatorLiveData<Boolean> isSettingsButtonClickedStateMerger = new MediatorLiveData<>(false);

    @NonNull
    private final MutableLiveData<Boolean> isSettingsButtonClickedMutableState = new MutableLiveData<>(false);

    @Inject
    public MainActivityViewModel(
            final @NonNull MainActivityPresenter presenter,
            final @NonNull MainActivityUIHandler handler,
            final @NonNull SavedStateHandle savedStateHandle
    ) {
        super(presenter, handler);
        initCallbackObservers();
    }

    private void initCallbackObservers() {
        isPlayButtonClickedStateMerger.addSource(
                isPlayButtonClickedMutableState,
                isPlayButtonClickedStateMerger::postValue
        );

        isSettingsButtonClickedStateMerger.addSource(
                isSettingsButtonClickedMutableState,
                isSettingsButtonClickedMutableState::postValue
        );
    }

    @NonNull
    public LiveData<Boolean> getPlayButtonClickedState() {
        return isPlayButtonClickedMutableState;
    }

    public void onPlayButtonClickedFinished() {
        isPlayButtonClickedMutableState.postValue(false);
    }

    @NonNull
    public LiveData<Boolean> getSettingsButtonClickedState() {
        return isSettingsButtonClickedMutableState;
    }

    public void onSettingsButtonClickedFinished() {
        isSettingsButtonClickedMutableState.postValue(false);
    }
}
