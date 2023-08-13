package com.paranid5.tic_tac_toe.di;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.data.GameStatus;
import com.paranid5.tic_tac_toe.presentation.game_fragment.GameFragmentPresenter;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;

@AssistedFactory
public interface GameFragmentPresenterFactory {
    GameFragmentPresenter create(
            final @Assisted("type") @NonNull MutableLiveData<Integer> typeState,
            final @Assisted("role") @NonNull MutableLiveData<Integer> roleState,
            final @Assisted("cur_mov") @NonNull MutableLiveData<Integer> currentMovingPlayerState,
            final @Assisted @NonNull MutableLiveData<GameStatus> victoryState,
            final @NonNull MutableLiveData<Integer[]> cellsState
    );
}
