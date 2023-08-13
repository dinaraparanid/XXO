package com.paranid5.tic_tac_toe.presentation.game_fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.paranid5.tic_tac_toe.data.GameStatus;
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
    private static final String GAME_STATUS = "game_status";

    @NonNull
    private static final String CELLS = "cells";

    @NonNull
    final GameFragmentPresenter presenter;

    @NonNull
    final GameFragmentUIHandler handler;

    @NonNull
    private final SavedStateHandle savedStateHandle;

    @Inject
    public GameFragmentViewModel(
            final @NonNull GameFragmentUIHandler handler,
            final @NonNull GameFragmentPresenterFactory presenterFactory,
            final @NonNull SavedStateHandle savedStateHandle
    ) {
        this.savedStateHandle = savedStateHandle;

        final MutableLiveData<Integer> type =
                savedStateHandle.getLiveData(PLAYER_TYPE, null);

        final MutableLiveData<Integer> role =
                savedStateHandle.getLiveData(PLAYER_ROLE, null);

        final MutableLiveData<Integer> currentMovingPlayerState =
                savedStateHandle.getLiveData(CURRENT_MOVING_PLAYER, PlayerRole.CROSS.ordinal());

        final MutableLiveData<GameStatus> gameStatusState =
                savedStateHandle.getLiveData(GAME_STATUS, new GameStatus.Playing());

        final MutableLiveData<Integer[]> cellsState =
                savedStateHandle.getLiveData(CELLS, new Integer[9]);

        this.presenter = presenterFactory.create(
                type,
                role,
                currentMovingPlayerState,
                gameStatusState,
                cellsState
        );

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
    public LiveData<Integer> getPlayerTypeState() { return presenter.playerTypeState; }

    @Nullable
    public PlayerType getPlayerType() {
        final Integer typeOrd = getPlayerTypeState().getValue();
        return typeOrd != null ? PlayerType.values()[typeOrd] : null;
    }

    public void postPlayerType(final @NonNull PlayerType playerType) {
        presenter.playerTypeState.postValue(playerType.ordinal());
        savedStateHandle.set(PLAYER_TYPE, playerType.ordinal());
    }

    @NonNull
    public LiveData<Integer> getPlayerRoleState() { return presenter.roleState; }

    @Nullable
    public PlayerRole getPlayerRole() {
        final Integer roleOrd = getPlayerRoleState().getValue();
        return roleOrd != null ? PlayerRole.values()[roleOrd] : null;
    }

    public void postPlayerRole(final @NonNull PlayerRole playerRole) {
        presenter.roleState.postValue(playerRole.ordinal());
        savedStateHandle.set(PLAYER_ROLE, playerRole.ordinal());
    }

    @NonNull
    public LiveData<Integer> getCurrentMovingPlayerState() {
        return presenter.currentMovingPlayerState;
    }

    @Nullable
    public PlayerRole getCurrentMovingPlayer() {
        final Integer roleOrd = getCurrentMovingPlayerState().getValue();
        return roleOrd != null ? PlayerRole.values()[roleOrd] : null;
    }

    public void postCurrentMovingPlayer(final @NonNull PlayerRole currentMovingPlayer) {
        presenter.currentMovingPlayerState.postValue(currentMovingPlayer.ordinal());
        savedStateHandle.set(CURRENT_MOVING_PLAYER, currentMovingPlayer.ordinal());
    }

    public void postGameStatus(final @NonNull GameStatus status) {
        presenter.gameStatusState.postValue(status);
        savedStateHandle.set(GAME_STATUS, status);
    }

    @NonNull
    public LiveData<Integer[]> getCellsState() {
        return presenter.cellsState;
    }

    public void postCellsState(final @NonNull Integer[] cells) {
        presenter.cellsState.postValue(cells);
        savedStateHandle.set(CELLS, cells);
    }

    public void startStatesObserving(final @NonNull LifecycleOwner owner) {
        presenter.startStatesObserving(owner);
    }

    @NonNull
    public PlayerRole getPlayerRoleByType(final @NonNull PlayerType type) {
        return type.equals(getPlayerType()) ? getPlayerRole() : getPlayerRole().nextRole();
    }
}
