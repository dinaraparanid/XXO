package com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment;

import androidx.annotation.NonNull;

import com.paranid5.tic_tac_toe.domain.game_service.GameServiceAccessor;
import com.paranid5.tic_tac_toe.presentation.UIHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class SelectGameRoomTypeUIHandler implements UIHandler {
    @NonNull
    private final GameServiceAccessor gameServiceAccessor;

    @Inject
    public SelectGameRoomTypeUIHandler(final @NonNull GameServiceAccessor gameServiceAccessor) {
        this.gameServiceAccessor = gameServiceAccessor;
    }

    public void onCreateNewRoomButtonClicked() {
        gameServiceAccessor.startService();
    }

    public void onConnectRoomButtonClicked() {
        // TODO: connect to game session
    }
}
