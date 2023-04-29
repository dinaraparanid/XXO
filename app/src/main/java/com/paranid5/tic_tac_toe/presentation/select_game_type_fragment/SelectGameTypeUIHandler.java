package com.paranid5.tic_tac_toe.presentation.select_game_type_fragment;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.presentation.UIHandler;
import com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment.SelectGameRoomTypeFragment;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class SelectGameTypeUIHandler implements UIHandler {

    @Inject
    public SelectGameTypeUIHandler() {}

    public void onSingleplayerButtonClicked(final @NonNull Context context) {
        Toast.makeText(
                context,
                "TODO: Singleplayer",
                Toast.LENGTH_SHORT
        ).show();
    }

    public void onMultiplayerButtonClicked(final @NonNull FragmentManager fragmentManager) {
        fragmentManager
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        SelectGameRoomTypeFragment.class,
                        null
                )
                .addToBackStack(null)
                .commit();
    }
}
