package com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.domain.game_service.GameServiceAccessor;
import com.paranid5.tic_tac_toe.presentation.UIHandler;
import com.paranid5.tic_tac_toe.presentation.game_fragment.GameFragment;
import com.paranid5.tic_tac_toe.data.PlayerRole;
import com.paranid5.tic_tac_toe.data.PlayerType;

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
        gameServiceAccessor.bindServiceOrSendWifiHostRequest();
    }

    public void onGameStartReceived(
            final @NonNull FragmentManager fragmentManager,
            final @NonNull PlayerType playerType,
            final @NonNull PlayerRole role
    ) { switchToGameFragment(fragmentManager, playerType, role); }

    private void switchToGameFragment(
            final @NonNull FragmentManager fragmentManager,
            final @NonNull PlayerType playerType,
            final @NonNull PlayerRole playerRole
    ) {
        fragmentManager
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        GameFragment.newInstance(playerType, playerRole),
                        null
                )
                .addToBackStack(null)
                .commit();
    }
}
