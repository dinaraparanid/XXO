package com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.paranid5.tic_tac_toe.R;

public final class GameHostDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(final @Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.your_ip)
                .setMessage(host)
                .setNegativeButton(
                        R.string.cancel,
                        (dialogInterface, i) -> {
                            viewModel.onGameCancelButtonClicked();
                            dialogInterface.dismiss();
                        }
                )
                .show();
    }
}
