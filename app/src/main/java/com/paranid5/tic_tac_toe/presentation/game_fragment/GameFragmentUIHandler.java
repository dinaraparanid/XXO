package com.paranid5.tic_tac_toe.presentation.game_fragment;

import androidx.annotation.NonNull;

import com.paranid5.tic_tac_toe.data.PlayerRole;
import com.paranid5.tic_tac_toe.presentation.UIHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class GameFragmentUIHandler implements UIHandler {
    @Inject
    public GameFragmentUIHandler() {}

    public void onCellClicked(
            final int cellPosition,
            final @NonNull GameFragmentViewModel viewModel
    ) {
        final PlayerRole[] cells = viewModel.getCellsState().getValue();

        if (cells[cellPosition] != null)
            return;

        cells[cellPosition] = viewModel.getPlayerRole();
        viewModel.presenter.cellsState.postValue(cells);

        // TODO: Send to server
    }
}
