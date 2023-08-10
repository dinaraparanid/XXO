package com.paranid5.tic_tac_toe.di;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.presentation.game_fragment.GameFragmentPresenter;
import com.paranid5.tic_tac_toe.data.PlayerRole;
import com.paranid5.tic_tac_toe.data.PlayerType;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;

@AssistedFactory
public interface GameFragmentPresenterFactory {
    GameFragmentPresenter create(
            final @Assisted("type") @NonNull MutableLiveData<Integer> typeState,
            final @Assisted("role") @NonNull MutableLiveData<Integer> roleState,
            final @Assisted("cur_mov") @NonNull MutableLiveData<Integer> currentMovingPlayerState,
            final @NonNull MutableLiveData<Integer[]> cellsState
    );
}
