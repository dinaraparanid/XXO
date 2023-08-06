package com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.domain.game_service.GameServiceAccessor;
import com.paranid5.tic_tac_toe.presentation.UIHandler;
import com.paranid5.tic_tac_toe.presentation.game_fragment.GameFragment;
import com.paranid5.tic_tac_toe.presentation.game_fragment.PlayerRole;
import com.paranid5.tic_tac_toe.presentation.game_fragment.PlayerType;

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

    public void onCreateNewRoomButtonClicked(
            final @NonNull FragmentManager fragmentManager,
            final @NonNull PlayerRole[] roles
    ) {
        gameServiceAccessor.bindService();
        switchToGameFragment(fragmentManager, PlayerType.HOST, roles[0], null);
    }

    public void onConnectRoomButtonClicked(
            final @NonNull FragmentManager fragmentManager,
            final @NonNull PlayerRole[] roles
    ) {
        // TODO: Real host name input
        final String host = "127.0.0.1";
        switchToGameFragment(fragmentManager, PlayerType.CLIENT, roles[1], host);
    }

    private void switchToGameFragment(
            final @NonNull FragmentManager fragmentManager,
            final @NonNull PlayerType playerType,
            final @NonNull PlayerRole playerRole,
            final @Nullable String host
    ) {
        fragmentManager
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        GameFragment.newInstance(playerType, playerRole, host),
                        null
                )
                .addToBackStack(null)
                .commit();
    }
}
