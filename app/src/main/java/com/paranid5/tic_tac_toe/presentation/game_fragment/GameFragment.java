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

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.data.PlayerRole;
import com.paranid5.tic_tac_toe.data.PlayerType;
import com.paranid5.tic_tac_toe.databinding.FragmentGameBinding;
import com.paranid5.tic_tac_toe.domain.ReceiverManager;
import com.paranid5.tic_tac_toe.presentation.StateChangedCallback;
import com.paranid5.tic_tac_toe.presentation.UIStateChangesObserver;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
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
    public static String PLAYER_TYPE = "player_type";

    @NonNull
    public static String PLAYER_ROLE = "player_role";

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
            final int cellPos = intent.getByteExtra(CELL_KEY, (byte) 0);
            final PlayerType movedPlayer = PlayerType.values()[intent.getIntExtra(PLAYER_TYPE, 0)];

            final PlayerType curPlayer = Objects.requireNonNull(viewModel.getPlayerType());
            final PlayerRole curRole = Objects.requireNonNull(viewModel.getPlayerRole());
            final Integer[] cells = viewModel.getCellsState().getValue();
            final PlayerRole movedRole = movedPlayer == curPlayer ? curRole : curRole.nextRole();

            cells[cellPos] = movedRole.ordinal();
            viewModel.postCellsState(cells);

            final PlayerRole nextMovingPlayer = viewModel.getCurrentMovingPlayer().nextRole();
            viewModel.postCurrentMovingPlayer(nextMovingPlayer);
        }
    };

    @NonNull
    private final BroadcastReceiver playerWonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            final PlayerType victor = PlayerType.values()[intent.getIntExtra(PLAYER_TYPE, 0)];

            // TODO: Stop game and show winner
        }
    };

    @NonNull
    public static GameFragment newInstance(
            final @NonNull PlayerType playerType,
            final @NonNull PlayerRole playerRole
    ) {
        final GameFragment fragment = new GameFragment();
        final Bundle args = new Bundle();

        args.putInt(PLAYER_ROLE, playerRole.ordinal());
        args.putInt(PLAYER_TYPE, playerType.ordinal());

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
        final PlayerType type = PlayerType.values()[requireArguments().getInt(PLAYER_TYPE)];
        final PlayerRole role = PlayerRole.values()[requireArguments().getInt(PLAYER_ROLE)];

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

        try {
            stopClient();
        } catch (final IOException e) {
            e.printStackTrace();
        }
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
    }

    @Override
    public void unregisterReceivers() {
        stopReceiver(playerMovedReceiver);
        stopReceiver(playerWonReceiver);
    }

    private void stopClient() throws IOException {
        Log.d(TAG, "Stopping client");

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
