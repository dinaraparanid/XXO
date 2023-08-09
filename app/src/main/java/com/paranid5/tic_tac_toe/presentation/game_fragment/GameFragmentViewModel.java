package com.paranid5.tic_tac_toe.presentation.game_fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.paranid5.tic_tac_toe.data.PlayerRole;
import com.paranid5.tic_tac_toe.data.PlayerType;
import com.paranid5.tic_tac_toe.di.GameFragmentPresenterFactory;
import com.paranid5.tic_tac_toe.presentation.ObservableViewModel;
import com.paranid5.tic_tac_toe.presentation.StateChangedCallback;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public final class GameFragmentViewModel extends ObservableViewModel<GameFragmentPresenter, GameFragmentUIHandler> {
    @NonNull
    private static final String PLAYER_TYPE = "player_type";

    @NonNull
    private static final String PLAYER_ROLE = "player_role";

    @NonNull
    private static final String CURRENT_MOVING_PLAYER = "cur_mov_player";

    @NonNull
    private static final String CELLS = "cells";

    @NonNull
    final GameFragmentPresenter presenter;

    @NonNull
    final GameFragmentUIHandler handler;

    @NonNull
    final SavedStateHandle savedStateHandle;

    @Inject
    public GameFragmentViewModel(
            final @NonNull GameFragmentUIHandler handler,
            final @NonNull GameFragmentPresenterFactory presenterFactory,
            final @NonNull SavedStateHandle savedStateHandle
    ) {
        this.savedStateHandle = savedStateHandle;

        final MutableLiveData<PlayerType> type =
                savedStateHandle.getLiveData(PLAYER_TYPE, null);

        final MutableLiveData<PlayerRole> role =
                savedStateHandle.getLiveData(PLAYER_ROLE, null);

        final MutableLiveData<PlayerRole> currentMovingPlayerState =
                savedStateHandle.getLiveData(CURRENT_MOVING_PLAYER, PlayerRole.CROSS);

        final MutableLiveData<PlayerRole[]> cellsState =
                savedStateHandle.getLiveData(CELLS, new PlayerRole[9]);

        this.presenter = presenterFactory.create(type, role, currentMovingPlayerState, cellsState);
        this.handler = handler;
    }

    @NonNull
    @Override
    public GameFragmentPresenter getPresenter() { return presenter; }

    @NonNull
    @Override
    public GameFragmentUIHandler getHandler() { return handler; }

    @NonNull
    final MutableLiveData<StateChangedCallback.State<Integer>> isCellClickedMutableState =
            new MutableLiveData<>(new StateChangedCallback.State<>());

    @NonNull
    public LiveData<StateChangedCallback.State<Integer>> getCellClickedState() {
        return isCellClickedMutableState;
    }

    public void onCellClicked(final int cellPosition) {
        isCellClickedMutableState.postValue(
                new StateChangedCallback.State<>(true, cellPosition)
        );
    }

    public void onCellClickedFinished() {
        isCellClickedMutableState.postValue(
                new StateChangedCallback.State<>(false)
        );
    }

    @NonNull
    public LiveData<PlayerType> getPlayerTypeState() { return presenter.typeState; }

    @Nullable
    public PlayerType getPlayerType() { return getPlayerTypeState().getValue(); }

    public void postPlayerType(final @NonNull PlayerType playerType) {
        presenter.typeState.postValue(playerType);
        savedStateHandle.set(PLAYER_TYPE, playerType);
    }

    @NonNull
    public LiveData<PlayerRole> getPlayerRoleState() { return presenter.roleState; }

    @Nullable
    public PlayerRole getPlayerRole() { return getPlayerRoleState().getValue(); }

    public void postPlayerRole(final @NonNull PlayerRole playerRole) {
        presenter.roleState.postValue(playerRole);
        savedStateHandle.set(PLAYER_ROLE, playerRole);
    }

    @NonNull
    public LiveData<PlayerRole> getCurrentMovingPlayerState() {
        return presenter.currentMovingPlayerState;
    }

    @Nullable
    public PlayerRole getCurrentMovingPlayer() {
        return getCurrentMovingPlayerState().getValue();
    }

    public void postCurrentMovingPlayer(final @NonNull PlayerRole currentMovingPlayer) {
        presenter.currentMovingPlayerState.postValue(currentMovingPlayer);
        savedStateHandle.set(CURRENT_MOVING_PLAYER, currentMovingPlayer);
    }

    @NonNull
    public LiveData<PlayerRole[]> getCellsState() {
        return presenter.cellsState;
    }

    public void startStatesObserving(final @NonNull LifecycleOwner owner) {
        presenter.startStatesObserving(owner);
    }
}
