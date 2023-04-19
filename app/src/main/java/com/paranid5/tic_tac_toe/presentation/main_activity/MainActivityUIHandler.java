package com.paranid5.tic_tac_toe.presentation.main_activity;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.paranid5.tic_tac_toe.presentation.UIHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class MainActivityUIHandler implements UIHandler {
    @Inject
    public MainActivityUIHandler() {}

    public void onPlayButtonClicked(final @NonNull Context context) {
        Toast.makeText(
                context.getApplicationContext(),
                "Play button clicked",
                Toast.LENGTH_SHORT
        ).show();
    }

    public void onSettingsButtonClicked(final @NonNull Context context) {
        Toast.makeText(
                context.getApplicationContext(),
                "TODO: Settings button",
                Toast.LENGTH_SHORT
        ).show();
    }
}
