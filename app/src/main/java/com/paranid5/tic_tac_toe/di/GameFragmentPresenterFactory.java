package com.paranid5.tic_tac_toe.di;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.presentation.game_fragment.GameFragmentPresenter;
import com.paranid5.tic_tac_toe.presentation.game_fragment.PlayerRole;
import com.paranid5.tic_tac_toe.presentation.game_fragment.PlayerType;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;

@AssistedFactory
public interface GameFragmentPresenterFactory {
    GameFragmentPresenter create(
            final @NonNull MutableLiveData<PlayerType> typeState,
            final @Assisted("role") @NonNull MutableLiveData<PlayerRole> roleState,
            final @Assisted("cur_mov") @NonNull MutableLiveData<PlayerRole> currentMovingPlayerState
    );
}
