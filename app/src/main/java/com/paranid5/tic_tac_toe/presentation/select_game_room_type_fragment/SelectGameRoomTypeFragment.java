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
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.databinding.DialogInputHostBinding;
import com.paranid5.tic_tac_toe.databinding.FragmentSelectGameRoomTypeBinding;
import com.paranid5.tic_tac_toe.domain.ReceiverManager;
import com.paranid5.tic_tac_toe.presentation.StateChangedCallback;
import com.paranid5.tic_tac_toe.presentation.UIStateChangesObserver;
import com.paranid5.tic_tac_toe.presentation.game_fragment.PlayerRole;
import com.paranid5.tic_tac_toe.presentation.game_fragment.PlayerType;

import java.util.Objects;

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
    private FragmentSelectGameRoomTypeBinding binding;

    @NonNull
    private SelectGameRoomTypeViewModel viewModel;

    @Nullable
    private DialogInterface showGameHostDialog;

    @NonNull
    public PlayerRole[] getRoles() { return viewModel.presenter.roles; }

    @NonNull
    private final StateChangedCallback<SelectGameRoomTypeUIHandler> createNewRoomButtonClickedCallback = handler -> {
        handler.onCreateNewRoomButtonClicked();
        viewModel.onCreateNewRoomButtonClickedFinished();
    };

    @NonNull
    private final StateChangedCallback<SelectGameRoomTypeUIHandler> connectRoomButtonClickedCallback = handler -> {
        showGameHostInput();
        viewModel.onConnectRoomButtonClickedFinished();
    };

    @NonNull
    private final StateChangedCallback<SelectGameRoomTypeUIHandler> gameCancelButtonClickedCallback = handler -> {
        handler.onGameCancelButtonClicked();
        viewModel.onGameCancelButtonClickedFinished();
    };

    @NonNull
    private final StateChangedCallback<SelectGameRoomTypeUIHandler> gameStartReceivedCallback = handler -> {
        handler.onGameStartReceived(getParentFragmentManager(), PlayerType.HOST, getRoles(), null);
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
            Log.d(TAG, "Game is started");
            dismissShowGameHostDialog();
            viewModel.onGameStartReceived();
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
        registerReceivers();
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
        dismissShowGameHostDialog();
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
                .setCancelable(false)
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
        if (showGameHostDialog != null)
            showGameHostDialog.dismiss();
        showGameHostDialog = null;
    }

    private void showGameHostInput() {
        final DialogInputHostBinding dialogBinding = DialogInputHostBinding.inflate(getLayoutInflater());

        new AlertDialog.Builder(requireContext())
                .setCancelable(false)
                .setTitle(R.string.host_ip)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(
                        R.string.ok,
                        (dialogInterface, i) -> viewModel.handler.onHostInputDone(
                                getParentFragmentManager(),
                                getRoles(),
                                dialogBinding.hostInput.getText().toString()
                        )
                )
                .setNegativeButton(
                        R.string.cancel,
                        (dialogInterface, i) -> dialogInterface.dismiss()
                )
                .show();
    }
}
