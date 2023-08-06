package com.paranid5.tic_tac_toe.presentation.select_game_type_fragment;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.presentation.ObservableViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public final class SelectGameTypeViewModel extends ObservableViewModel<SelectGameTypePresenter, SelectGameTypeUIHandler> {

    @NonNull
    SelectGameTypePresenter presenter;

    @NonNull
    SelectGameTypeUIHandler handler;

    @Inject
    public SelectGameTypeViewModel(
            final @NonNull SelectGameTypePresenter presenter,
            final @NonNull SelectGameTypeUIHandler handler
    ) {
        this.presenter = presenter;
        this.handler = handler;
    }

    @Override
    @NonNull
    public SelectGameTypePresenter getPresenter() { return presenter; }

    @Override
    @NonNull
    public SelectGameTypeUIHandler getHandler() { return handler; }

    @NonNull
    private final MutableLiveData<Boolean> isSingleplayerButtonClickedMutableState = new MutableLiveData<>(false);

    @NonNull
    private final MutableLiveData<Boolean> isMultiplayerButtonClickedMutableState = new MutableLiveData<>(false);

    @NonNull
    public LiveData<Boolean> getSingleplayerButtonClickedState() {
        return isSingleplayerButtonClickedMutableState;
    }

    public void onSingleplayerButtonClicked() {
        isSingleplayerButtonClickedMutableState.postValue(true);
    }

    public void onSingleplayerButtonClickedFinished() {
        isSingleplayerButtonClickedMutableState.postValue(false);
    }

    @NonNull
    public LiveData<Boolean> getMultiplayerButtonClickedState() {
        return isMultiplayerButtonClickedMutableState;
    }

    public void onMultiplayerButtonClicked() {
        isMultiplayerButtonClickedMutableState.postValue(true);
    }

    public void onMultiplayerButtonClickedFinished() {
        isMultiplayerButtonClickedMutableState.postValue(false);
    }
}
