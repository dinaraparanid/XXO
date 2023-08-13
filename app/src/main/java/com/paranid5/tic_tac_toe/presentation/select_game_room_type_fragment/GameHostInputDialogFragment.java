package com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.databinding.DialogInputHostBinding;

import java.io.IOException;

public final class GameHostInputDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(final @Nullable Bundle savedInstanceState) {
        final DialogInputHostBinding dialogBinding = DialogInputHostBinding.inflate(getLayoutInflater());

        return new AlertDialog.Builder(requireContext())
                .setCancelable(false)
                .setTitle(R.string.host_ip)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(
                        R.string.ok,
                        (dialogInterface, i) -> {
                            try {
                                connectClientSocket(dialogBinding.hostInput.getText().toString());
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }

                            dismissGameHostInputDialog();
                        }
                )
                .setNegativeButton(
                        R.string.cancel,
                        (dialogInterface, i) -> dismissGameHostInputDialog()
                )
                .show();
    }
}
