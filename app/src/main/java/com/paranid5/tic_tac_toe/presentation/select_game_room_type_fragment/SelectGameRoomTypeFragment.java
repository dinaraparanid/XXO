package com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.data.PlayerRole;
import com.paranid5.tic_tac_toe.data.PlayerType;
import com.paranid5.tic_tac_toe.databinding.FragmentSelectGameRoomTypeBinding;
import com.paranid5.tic_tac_toe.domain.ReceiverManager;
import com.paranid5.tic_tac_toe.presentation.StateChangedCallback;
import com.paranid5.tic_tac_toe.presentation.UIStateChangesObserver;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class SelectGameRoomTypeFragment extends Fragment implements UIStateChangesObserver, ReceiverManager {
    @NonNull
    private static final String TAG = SelectGameRoomTypeFragment.class.getSimpleName();

    @NonNull
    private static final String GAME_HOST_DIALOG_TAG = GameHostDialogFragment.class.getSimpleName();

    @NonNull
    private static final String GAME_HOST_INPUT_DIALOG_TAG = GameHostInputDialogFragment.class.getSimpleName();

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
    private DialogFragment gameHostDialog;

    @Nullable
    private DialogFragment gameHostInputDialog;

    @NonNull
    private final StateChangedCallback<SelectGameRoomTypeUIHandler, Void> createNewRoomButtonClickedCallback = (handler, t) -> {
        handler.onCreateNewRoomButtonClicked();
        viewModel.onCreateNewRoomButtonClickedFinished();
    };

    @NonNull
    private final StateChangedCallback<SelectGameRoomTypeUIHandler, Void> connectRoomButtonClickedCallback = (handler, t) -> {
        final FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        final Fragment prev = getChildFragmentManager().findFragmentByTag(GAME_HOST_INPUT_DIALOG_TAG);

        if (prev != null)
            ft.remove(prev);

        ft.addToBackStack(null);

        gameHostInputDialog = new GameHostInputDialogFragment();
        gameHostInputDialog.show(getChildFragmentManager(), GAME_HOST_INPUT_DIALOG_TAG);
        viewModel.onConnectRoomButtonClickedFinished();
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

            gameHostDialog = GameHostDialogFragment.newInstance(host);
            gameHostDialog.show(getChildFragmentManager(), GAME_HOST_DIALOG_TAG);
        }
    };

    @NonNull
    private final BroadcastReceiver gameStartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @Nullable Context context, final @NonNull Intent intent) {
            final PlayerType playerType = PlayerType.values()[intent.getIntExtra(PLAYER_TYPE_KEY, 0)];
            final PlayerRole role = PlayerRole.values()[intent.getIntExtra(PLAYER_ROLE_KEY, 0)];
            Log.d(TAG, String.format("Game is started as %s", role));

            dismissGameHostDialog();
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
        dismissGameHostDialog();
        dismissGameHostInputDialog();
    }

    @Override
    public void observeUIStateChanges() {
        createNewRoomButtonClickedCallback.observe(this, viewModel.getCreateNewRoomButtonClickedState(), viewModel.handler);
        connectRoomButtonClickedCallback.observe(this, viewModel.getConnectRoomButtonClickedState(), viewModel.handler);
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

    private void dismissGameHostDialog() {
        if (gameHostDialog != null) {
            gameHostDialog.dismissAllowingStateLoss();
            gameHostDialog = null;
        }
    }

    private void dismissGameHostInputDialog() {
        if (gameHostInputDialog != null) {
            gameHostInputDialog.dismissAllowingStateLoss();
            gameHostInputDialog = null;
        }
    }
}
