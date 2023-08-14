package com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.databinding.DialogInputHostBinding;
import com.paranid5.tic_tac_toe.domain.network.ClientLauncher;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class GameHostInputDialogFragment extends DialogFragment {
    @Inject
    @NonNull
    AtomicReference<UUID> clientTaskIdState;

    @NonNull
    @Override
    public Dialog onCreateDialog(final @Nullable Bundle savedInstanceState) {
        final DialogInputHostBinding dialogBinding = DialogInputHostBinding.inflate(getLayoutInflater());

        return new MaterialAlertDialogBuilder(requireContext())
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

                            dismiss();
                        }
                )
                .setNegativeButton(
                        R.string.cancel,
                        (dialogInterface, i) -> dismiss()
                )
                .create();
    }

    private void connectClientSocket(final @NonNull String host) throws IOException {
        final OneTimeWorkRequest clientTask = new OneTimeWorkRequest
                .Builder(ClientLauncher.class)
                .setInputData(
                        new Data.Builder()
                                .putString(ClientLauncher.HOST_KEY, host)
                                .build()
                )
                .build();

        clientTaskIdState.set(clientTask.getId());
        WorkManager.getInstance(requireContext()).enqueue(clientTask);
    }
}
