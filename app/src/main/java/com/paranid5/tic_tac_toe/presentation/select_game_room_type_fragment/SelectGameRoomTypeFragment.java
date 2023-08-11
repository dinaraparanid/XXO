package com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.data.PlayerRole;
import com.paranid5.tic_tac_toe.data.PlayerType;
import com.paranid5.tic_tac_toe.databinding.DialogInputHostBinding;
import com.paranid5.tic_tac_toe.databinding.FragmentSelectGameRoomTypeBinding;
import com.paranid5.tic_tac_toe.domain.ReceiverManager;
import com.paranid5.tic_tac_toe.domain.network.ClientLauncher;
import com.paranid5.tic_tac_toe.presentation.StateChangedCallback;
import com.paranid5.tic_tac_toe.presentation.UIStateChangesObserver;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class SelectGameRoomTypeFragment extends Fragment implements UIStateChangesObserver, ReceiverManager {
    @NonNull
    private static final String TAG = SelectGameRoomTypeFragment.class.getSimpleName();

    @NonNull
    private static final String FRAGMENT_LOCATION = "com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment";

    @NonNull
    private static String buildBroadcast(final @NonNull String action) {
        return String.format("%s.%s", FRAGMENT_LOCATION, action);
    }

    @NonNull
    public static String Broadcast_GAME_HOST = buildBroadcast("GAME_HOST");

    @NonNull
    public static String Broadcast_GAME_START = buildBroadcast("GAME_START");

    @NonNull
    public static String GAME_HOST_KEY = "game_host";

    @NonNull
    public static String PLAYER_TYPE_KEY = "player_type";

    @NonNull
    public static String PLAYER_ROLE_KEY = "player_role";

    @NonNull
    private FragmentSelectGameRoomTypeBinding binding;

    @NonNull
    private SelectGameRoomTypeViewModel viewModel;

    @Nullable
    private DialogInterface showGameHostDialog;

    @Nullable
    private DialogInterface gameHostInputDialog;

    @Inject
    @NonNull
    AtomicReference<UUID> clientTaskIdState;

    @NonNull
    private final StateChangedCallback<SelectGameRoomTypeUIHandler, Void> createNewRoomButtonClickedCallback = (handler, t) -> {
        handler.onCreateNewRoomButtonClicked();
        viewModel.onCreateNewRoomButtonClickedFinished();
    };

    @NonNull
    private final StateChangedCallback<SelectGameRoomTypeUIHandler, Void> connectRoomButtonClickedCallback = (handler, t) -> {
        gameHostInputDialog = showGameHostInput();
        viewModel.onConnectRoomButtonClickedFinished();
    };

    @NonNull
    private final StateChangedCallback<SelectGameRoomTypeUIHandler, Void> gameCancelButtonClickedCallback = (handler, t) -> {
        handler.onGameCancelButtonClicked();
        viewModel.onGameCancelButtonClickedFinished();
    };

    @NonNull
    private final StateChangedCallback<SelectGameRoomTypeUIHandler, Pair<PlayerType, PlayerRole>> gameStartReceivedCallback = (handler, typeToRole) -> {
        Objects.requireNonNull(typeToRole);

        handler.onGameStartReceived(
                getParentFragmentManager(),
                typeToRole.first,
                typeToRole.second
        );

        viewModel.onGameStartReceivedFinished();
    };

    @NonNull
    private final BroadcastReceiver gameHostReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @Nullable Context context, final @NonNull Intent intent) {
            final String host = intent.getStringExtra(GAME_HOST_KEY);
            Objects.requireNonNull(host);

            Log.d(TAG, String.format("Host %s is received", host));
            showGameHostDialog = showGameHost(host);
        }
    };

    @NonNull
    private final BroadcastReceiver gameStartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @Nullable Context context, final @NonNull Intent intent) {
            final PlayerType playerType = PlayerType.values()[intent.getIntExtra(PLAYER_TYPE_KEY, 0)];
            final PlayerRole role = PlayerRole.values()[intent.getIntExtra(PLAYER_ROLE_KEY, 0)];
            Log.d(TAG, String.format("Game is started as %s", role));

            dismissShowGameHostDialog();
            dismissGameHostInputDialog();
            viewModel.onGameStartReceived(playerType, role);
        }
    };

    @NonNull
    @Override
    public View onCreateView(
            final @NonNull LayoutInflater inflater,
            final @Nullable ViewGroup container,
            final @Nullable Bundle savedInstanceState
    ) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_game_room_type, container, false);
        viewModel = new ViewModelProvider(this).get(SelectGameRoomTypeViewModel.class);
        binding.setViewModel(viewModel);

        observeUIStateChanges();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceivers();
        dismissShowGameHostDialog();
        dismissGameHostInputDialog();
    }

    @Override
    public void observeUIStateChanges() {
        createNewRoomButtonClickedCallback.observe(this, viewModel.getCreateNewRoomButtonClickedState(), viewModel.handler);
        connectRoomButtonClickedCallback.observe(this, viewModel.getConnectRoomButtonClickedState(), viewModel.handler);
        gameCancelButtonClickedCallback.observe(this, viewModel.getGameCancelButtonClickedState(), viewModel.handler);
        gameStartReceivedCallback.observe(this, viewModel.getGameStartReceivedState(), viewModel.handler);
    }

    @NonNull
    @Override
    public Context getReceiverContext() { return requireContext(); }

    @Override
    public void registerReceivers() {
        registerReceiverCompat(gameHostReceiver, Broadcast_GAME_HOST);
        registerReceiverCompat(gameStartReceiver, Broadcast_GAME_START);
    }

    @Override
    public void unregisterReceivers() {
        stopReceiver(gameHostReceiver);
        stopReceiver(gameStartReceiver);
    }

    private AlertDialog showGameHost(final @NonNull String host) {
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

    private void dismissShowGameHostDialog() {
        Log.d(TAG, "Stopping show host dialog");

        if (showGameHostDialog != null) {
            showGameHostDialog.dismiss();
            showGameHostDialog = null;
            Log.d(TAG, "Dialog is stopped");
        }
    }

    private AlertDialog showGameHostInput() {
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

                            dialogInterface.dismiss();
                        }
                )
                .setNegativeButton(
                        R.string.cancel,
                        (dialogInterface, i) -> dialogInterface.dismiss()
                )
                .show();
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

    private void dismissGameHostInputDialog() {
        if (gameHostInputDialog != null) {
            gameHostInputDialog.dismiss();
            gameHostInputDialog = null;
        }
    }
}
