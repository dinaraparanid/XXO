package com.paranid5.tic_tac_toe.presentation.game_fragment;

import androidx.annotation.NonNull;

import com.paranid5.tic_tac_toe.presentation.ObservableViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public final class GameFragmentViewModel extends ObservableViewModel<GameFragmentPresenter, GameFragmentUIHandler> {
    @NonNull
    final GameFragmentPresenter presenter;

    @NonNull
    final GameFragmentUIHandler handler;

    @Inject
    public GameFragmentViewModel(
            final @NonNull GameFragmentPresenter presenter,
            final @NonNull GameFragmentUIHandler handler
    ) {
        this.presenter = presenter;
        this.handler = handler;
    }

    @NonNull
    @Override
    public GameFragmentPresenter getPresenter() { return presenter; }

    @NonNull
    @Override
    public GameFragmentUIHandler getHandler() { return handler; }
}
