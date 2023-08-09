package com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.data.PlayerType;
import com.paranid5.tic_tac_toe.presentation.ObservableViewModel;
import com.paranid5.tic_tac_toe.presentation.StateChangedCallback;
import com.paranid5.tic_tac_toe.data.PlayerRole;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public final class SelectGameRoomTypeViewModel extends ObservableViewModel<SelectGameRoomTypePresenter, SelectGameRoomTypeUIHandler> {
    @NonNull
    final SelectGameRoomTypePresenter presenter;

    @NonNull
    final SelectGameRoomTypeUIHandler handler;

    @Inject
    public SelectGameRoomTypeViewModel(
            final @NonNull SelectGameRoomTypePresenter presenter,
            final @NonNull SelectGameRoomTypeUIHandler handler
    ) {
        this.presenter = presenter;
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
    private final MutableLiveData<StateChangedCallback.State<Void>> isCreateNewRoomButtonClickedMutableState =
            new MutableLiveData<>(new StateChangedCallback.State<>());

    @NonNull
    public LiveData<StateChangedCallback.State<Void>> getCreateNewRoomButtonClickedState() {
        return isCreateNewRoomButtonClickedMutableState;
    }

    public void onCreateNewRoomButtonClicked() {
        isCreateNewRoomButtonClickedMutableState.postValue(
                new StateChangedCallback.State<>(true)
        );
    }

    public void onCreateNewRoomButtonClickedFinished() {
        isCreateNewRoomButtonClickedMutableState.postValue(
                new StateChangedCallback.State<>(false)
        );
    }

    @NonNull
    private final MutableLiveData<StateChangedCallback.State<Void>> isConnectRoomButtonClickedMutableState =
            new MutableLiveData<>(new StateChangedCallback.State<>());

    @NonNull
    public LiveData<StateChangedCallback.State<Void>> getConnectRoomButtonClickedState() {
        return isConnectRoomButtonClickedMutableState;
    }

    public void onConnectRoomButtonClicked() {
        isConnectRoomButtonClickedMutableState.postValue(
                new StateChangedCallback.State<>(true)
        );
    }

    public void onConnectRoomButtonClickedFinished() {
        isConnectRoomButtonClickedMutableState.postValue(
                new StateChangedCallback.State<>(false)
        );
    }

    @NonNull
    private final MutableLiveData<StateChangedCallback.State<Void>> isGameCancelButtonClickedMutableState =
            new MutableLiveData<>(new StateChangedCallback.State<>());

    @NonNull
    public LiveData<StateChangedCallback.State<Void>> getGameCancelButtonClickedState() {
        return isGameCancelButtonClickedMutableState;
    }

    public void onGameCancelButtonClicked() {
        isGameCancelButtonClickedMutableState.postValue(
                new StateChangedCallback.State<>(true)
        );
    }

    public void onGameCancelButtonClickedFinished() {
        isGameCancelButtonClickedMutableState.postValue(
                new StateChangedCallback.State<>(false)
        );
    }

    @NonNull
    final MutableLiveData<StateChangedCallback.State<Pair<PlayerType, PlayerRole>>> isGameStartReceivedMutableState =
            new MutableLiveData<>(new StateChangedCallback.State<>());

    @NonNull
    public LiveData<StateChangedCallback.State<Pair<PlayerType, PlayerRole>>> getGameStartReceivedState() {
        return isGameStartReceivedMutableState;
    }

    public void onGameStartReceived(
            final @NonNull PlayerType playerType,
            final @NonNull PlayerRole role
    ) {
        isGameStartReceivedMutableState.postValue(
                new StateChangedCallback.State<>(true, new Pair<>(playerType, role))
        );
    }

    public void onGameStartReceivedFinished() {
        isGameStartReceivedMutableState.postValue(
                new StateChangedCallback.State<>(false)
        );
    }
}
