package com.paranid5.tic_tac_toe.presentation.main_fragment;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.presentation.UIHandler;
import com.paranid5.tic_tac_toe.presentation.select_game_type_fragment.SelectGameTypeFragment;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class MainFragmentUIHandler implements UIHandler {
    @Inject
    public MainFragmentUIHandler() {}

    public void onPlayButtonClicked(final @NonNull FragmentManager fragmentManager) {
        fragmentManager
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        SelectGameTypeFragment.class,
                        null
                )
                .addToBackStack(null)
                .commit();
    }

    public void onSettingsButtonClicked(final @NonNull Context context) {
        Toast.makeText(
                context.getApplicationContext(),
                "TODO: Settings button",
                Toast.LENGTH_SHORT
        ).show();
    }
}
