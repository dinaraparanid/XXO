package com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.paranid5.tic_tac_toe.presentation.ObservableViewModel;
import com.paranid5.tic_tac_toe.presentation.game_fragment.PlayerRole;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public final class SelectGameRoomTypeViewModel extends ObservableViewModel<SelectGameRoomTypePresenter, SelectGameRoomTypeUIHandler> {
    @NonNull
    private static final String ROLES_KEY = "roles";

    @NonNull
    final SelectGameRoomTypePresenter presenter;

    @NonNull
    final SelectGameRoomTypeUIHandler handler;

    @Inject
    public SelectGameRoomTypeViewModel(
            final @NonNull SelectGameRoomTypeUIHandler handler,
            final @NonNull SavedStateHandle savedStateHandle
    ) {
        final PlayerRole[] roles = savedStateHandle.get(ROLES_KEY);

        this.presenter = roles != null
                ? new SelectGameRoomTypePresenter(roles)
                : new SelectGameRoomTypePresenter();

        this.handler = handler;
    }

    @Override
    @NonNull
    public SelectGameRoomTypePresenter getPresenter() {
        return presenter;
    }

    @Override
    @NonNull
    public SelectGameRoomTypeUIHandler getHandler() {
        return handler;
    }

    @NonNull
    private final MutableLiveData<Boolean> isCreateNewRoomButtonClickedMutableState = new MutableLiveData<>(false);

    @NonNull
    private final MutableLiveData<Boolean> isConnectRoomButtonClickedMutableState = new MutableLiveData<>(false);

    @NonNull
    private final MutableLiveData<Boolean> isGameCancelButtonClickedMutableState = new MutableLiveData<>(false);

    @NonNull
    public LiveData<Boolean> getCreateNewRoomButtonClickedState() {
        return isCreateNewRoomButtonClickedMutableState;
    }

    public void onCreateNewRoomButtonClicked() {
        isCreateNewRoomButtonClickedMutableState.postValue(true);
    }

    public void onCreateNewRoomButtonClickedFinished() {
        isCreateNewRoomButtonClickedMutableState.postValue(false);
    }

    @NonNull
    public LiveData<Boolean> getConnectRoomButtonClickedState() {
        return isConnectRoomButtonClickedMutableState;
    }

    public void onConnectRoomButtonClicked() {
        isConnectRoomButtonClickedMutableState.postValue(true);
    }

    public void onConnectRoomButtonClickedFinished() {
        isConnectRoomButtonClickedMutableState.postValue(false);
    }

    @NonNull
    public LiveData<Boolean> getGameCancelButtonClickedState() {
        return isGameCancelButtonClickedMutableState;
    }

    public void onGameCancelButtonClicked() {
        isGameCancelButtonClickedMutableState.postValue(true);
    }

    public void onGameCancelButtonClickedFinished() {
        isGameCancelButtonClickedMutableState.postValue(false);
    }
}
