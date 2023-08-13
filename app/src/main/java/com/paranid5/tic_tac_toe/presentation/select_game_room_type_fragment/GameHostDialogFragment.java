package com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.paranid5.tic_tac_toe.R;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class GameHostDialogFragment extends DialogFragment {
    private static final String HOST_KEY = "host";

    @Inject
    SelectGameRoomTypeUIHandler handler;

    @NonNull
    public static GameHostDialogFragment newInstance(final @NonNull String host) {
        final GameHostDialogFragment fragment = new GameHostDialogFragment();
        final Bundle args = new Bundle();
        args.putString(HOST_KEY, host);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final @Nullable Bundle savedInstanceState) {
        final String host = requireArguments().getString(HOST_KEY);

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.your_ip)
                .setMessage(host)
                .setNegativeButton(
                        R.string.cancel,
                        (dialogInterface, i) -> dialogInterface.dismiss()
                )
                .create();
    }
}
