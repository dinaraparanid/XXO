package com.paranid5.tic_tac_toe.presentation.game_fragment;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.BR;
import com.paranid5.tic_tac_toe.presentation.ObservablePresenter;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

public final class GameFragmentPresenter extends ObservablePresenter {
    @NonNull
    public MutableLiveData<PlayerType> typeState;

    @NonNull
    public MutableLiveData<PlayerRole> roleState;

    @NonNull
    public MutableLiveData<PlayerRole> currentMovingPlayerState;

    @NonNull
    public MediatorLiveData<Boolean> isMovingState;

    @Bindable
    public boolean getMoving() { return isMovingState.getValue(); }

    @AssistedInject
    public GameFragmentPresenter(
            final @Assisted @NonNull MutableLiveData<PlayerType> typeState,
            final @Assisted("role") @NonNull MutableLiveData<PlayerRole> roleState,
            final @Assisted("cur_mov") @NonNull MutableLiveData<PlayerRole> currentMovingPlayerState
    ) {
        this.typeState = typeState;
        this.roleState = roleState;
        this.currentMovingPlayerState = currentMovingPlayerState;
        isMovingState = new MediatorLiveData<>(currentMovingPlayerState.getValue() == roleState.getValue());

        isMovingState.addSource(
                currentMovingPlayerState,
                currentMovingPlayer -> {
                    isMovingState.setValue(currentMovingPlayer == this.roleState.getValue());
                    notifyPropertyChanged(BR.moving);
                }
        );
    }
}
