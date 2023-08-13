package com.paranid5.tic_tac_toe.presentation.game_fragment;

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
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkManager;

import com.paranid5.tic_tac_toe.MainApplication;
import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.data.GameStatus;
import com.paranid5.tic_tac_toe.data.PlayerRole;
import com.paranid5.tic_tac_toe.data.PlayerType;
import com.paranid5.tic_tac_toe.databinding.FragmentGameBinding;
import com.paranid5.tic_tac_toe.domain.ReceiverManager;
import com.paranid5.tic_tac_toe.presentation.StateChangedCallback;
import com.paranid5.tic_tac_toe.presentation.UIStateChangesObserver;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class GameFragment extends Fragment implements UIStateChangesObserver, ReceiverManager {
    @NonNull
    private static final String TAG = GameFragment.class.getSimpleName();

    @NonNull
    private static final String FRAGMENT_LOCATION = "com.paranid5.tic_tac_toe.presentation.game_fragment";

    @NonNull
    private static String buildBroadcast(final @NonNull String action) {
        return String.format("%s.%s", FRAGMENT_LOCATION, action);
    }

    @NonNull
    public static final String Broadcast_PLAYER_MOVED = buildBroadcast("HOST_MOVED");

    @NonNull
    public static final String Broadcast_PLAYER_WON = buildBroadcast("PLAYER_WON");

    @NonNull
    public static final String Broadcast_DRAW = buildBroadcast("DRAW");

    @NonNull
    public static String PLAYER_TYPE_KEY = "player_type";

    @NonNull
    public static String PLAYER_ROLE_KEY = "player_role";

    @NonNull
    public static String CELL_KEY = "cell";

    @NonNull
    private FragmentGameBinding binding;

    @NonNull
    private GameFragmentViewModel viewModel;

    @Inject
    @NonNull
    MutableLiveData<Socket> clientState;

    @Nullable
    private Socket getClient() { return clientState.getValue(); }

    @Inject
    @NonNull
    AtomicReference<UUID> clientTaskIdState;

    @NonNull
    private final StateChangedCallback<GameFragmentUIHandler, Integer> cellClickedCallback = (handler, cellPos) -> {
        handler.onCellClicked(
                cellPos,
                viewModel,
                clientState.getValue(),
                requireContext()
        );

        viewModel.onCellClickedFinished();
    };

    @NonNull
    private final BroadcastReceiver playerMovedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @Nullable Context context, final @NonNull Intent intent) {
            receiveAndPostCell(intent);

            final PlayerRole nextMovingPlayer = viewModel.getCurrentMovingPlayer().nextRole();
            viewModel.postCurrentMovingPlayer(nextMovingPlayer);
        }
    };

    @NonNull
    private final BroadcastReceiver playerWonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            receiveAndPostCell(intent);

            final PlayerType victor = PlayerType.values()[intent.getIntExtra(PLAYER_TYPE_KEY, 0)];
            viewModel.postGameStatus(new GameStatus.Victor(victor));
            Log.d(TAG, String.format("Player %s has won", victor));

            stopNetworkLaunchers();
        }
    };

    @NonNull
    private final BroadcastReceiver drawReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            receiveAndPostCell(intent);

            viewModel.postGameStatus(new GameStatus.Draw());
            Log.d(TAG, "Draw");

            stopNetworkLaunchers();
        }
    };

    @NonNull
    public static GameFragment newInstance(
            final @NonNull PlayerType playerType,
            final @NonNull PlayerRole playerRole
    ) {
        final GameFragment fragment = new GameFragment();
        final Bundle args = new Bundle();

        args.putInt(PLAYER_ROLE_KEY, playerRole.ordinal());
        args.putInt(PLAYER_TYPE_KEY, playerType.ordinal());

        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public View onCreateView(
            final @NonNull LayoutInflater inflater,
            final @Nullable ViewGroup container,
            final @Nullable Bundle savedInstanceState
    ) {
        final PlayerType type = PlayerType.values()[requireArguments().getInt(PLAYER_TYPE_KEY)];
        final PlayerRole role = PlayerRole.values()[requireArguments().getInt(PLAYER_ROLE_KEY)];

        Log.d(TAG, String.format("Start game as %s %s", type, role));

        viewModel = new ViewModelProvider(this).get(GameFragmentViewModel.class);
        if (viewModel.getPlayerType() == null) viewModel.postPlayerType(type);
        if (viewModel.getPlayerRole() == null) viewModel.postPlayerRole(role);

        viewModel.startStatesObserving(this);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game, container, false);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopNetworkLaunchers();
    }

    @Override
    public void observeUIStateChanges() {
        cellClickedCallback.observe(this, viewModel.getCellClickedState(), viewModel.handler);
    }

    @NonNull
    @Override
    public Context getReceiverContext() { return requireContext(); }

    @Override
    public void registerReceivers() {
        registerReceiverCompat(playerMovedReceiver, Broadcast_PLAYER_MOVED);
        registerReceiverCompat(playerWonReceiver, Broadcast_PLAYER_WON);
        registerReceiverCompat(drawReceiver, Broadcast_DRAW);
    }

    @Override
    public void unregisterReceivers() {
        stopReceiver(playerMovedReceiver);
        stopReceiver(playerWonReceiver);
        stopReceiver(drawReceiver);
    }

    private void receiveAndPostCell(final @NonNull Intent intent) {
        final int cellPos = intent.getIntExtra(CELL_KEY, 0);
        final PlayerType movedPlayer = PlayerType.values()[intent.getIntExtra(PLAYER_TYPE_KEY, 0)];
        final Integer[] cells = viewModel.getCellsState().getValue();
        Log.d(TAG, String.format("Player %s moved to %d", movedPlayer, cellPos));

        cells[cellPos] = viewModel.getPlayerRoleByType(movedPlayer).ordinal();
        viewModel.postCellsState(cells);
    }

    private void stopNetworkLaunchers() {
        stopServiceIfBinded();

        try {
            stopClientIfLaunched();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void stopServiceIfBinded() {
        final MainApplication app = (MainApplication) requireActivity().getApplication();

        if (app.isGameServiceConnected) {
            app.unbindService(app.gameServiceConnection);
            app.isGameServiceConnected = false;
        }
    }

    private void stopClientIfLaunched() throws IOException {
        Log.d(TAG, "Stopping client");

        final UUID clientTaskId = clientTaskIdState.get();

        if (clientTaskId == null)
            return;

        WorkManager
                .getInstance(requireContext())
                .cancelWorkById(clientTaskIdState.get());

        clientTaskIdState.set(null);

        if (getClient() != null) {
            getClient().close();
            clientState.postValue(null);
        }
    }
}
