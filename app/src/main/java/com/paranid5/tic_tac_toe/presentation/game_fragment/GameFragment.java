package com.paranid5.tic_tac_toe.presentation.game_fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkManager;

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.data.GameStatus;
import com.paranid5.tic_tac_toe.data.PlayerRole;
import com.paranid5.tic_tac_toe.data.PlayerType;
import com.paranid5.tic_tac_toe.databinding.FragmentGameBinding;
import com.paranid5.tic_tac_toe.domain.ReceiverManager;
import com.paranid5.tic_tac_toe.domain.game_service.GameServiceAccessor;
import com.paranid5.tic_tac_toe.domain.utils.network.DefaultDisposableCompletable;
import com.paranid5.tic_tac_toe.presentation.StateChangedCallback;
import com.paranid5.tic_tac_toe.presentation.UIStateChangesObserver;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

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
    public static final String Broadcast_PLAYER_LEFT = buildBroadcast("PLAYER_LEFT");

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

    @Inject
    @NonNull
    GameServiceAccessor serviceAccessor;

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
    private final BroadcastReceiver playerLeftReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            stopNetwork();

            Toast.makeText(requireContext(), R.string.opponent_left, Toast.LENGTH_LONG).show();
            viewModel.postGameStatus(new GameStatus.Victor(viewModel.getPlayerType()));
            unregisterReceivers();
        }
    };

    @NonNull
    private final BroadcastReceiver playerWonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            receiveAndPostCell(intent);

            final PlayerType victor = PlayerType.values()[intent.getIntExtra(PLAYER_TYPE_KEY, 0)];
            viewModel.postGameStatus(new GameStatus.Victor(victor));
            unregisterReceivers();
            stopNetwork();
            Log.d(TAG, String.format("Player %s has won", victor));
        }
    };

    @NonNull
    private final BroadcastReceiver drawReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            receiveAndPostCell(intent);

            viewModel.postGameStatus(new GameStatus.Draw());
            unregisterReceivers();
            stopNetwork();
            Log.d(TAG, "Draw");
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
        registerReceivers();
        return binding.getRoot();
    }

    @Override
    public void onAttach(final @NonNull Context context) {
        super.onAttach(context);

        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(onBackPressedCallback());
    }

    @NonNull
    private OnBackPressedCallback onBackPressedCallback() {
        return new OnBackPressedCallback(true) {
            @SuppressLint("CheckResult")
            @Override
            public void handleOnBackPressed() {
                unregisterReceivers();
                viewModel.handler.sendHostLeft(requireContext());

                if (getClient() != null)
                    viewModel.handler
                            .sendClientLeft(Objects.requireNonNull(getClient()))
                            .andThen((CompletableSource) s -> stopClientIfLaunched())
                            .subscribeWith(DefaultDisposableCompletable.disposableCompletableObserver());

                getParentFragmentManager().popBackStack();
            }
        };
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
        registerReceiverCompat(playerLeftReceiver, Broadcast_PLAYER_LEFT);
        registerReceiverCompat(playerWonReceiver, Broadcast_PLAYER_WON);
        registerReceiverCompat(drawReceiver, Broadcast_DRAW);
    }

    @Override
    public void unregisterReceivers() {
        try {
            stopReceiver(playerMovedReceiver);
            stopReceiver(playerLeftReceiver);
            stopReceiver(playerWonReceiver);
            stopReceiver(drawReceiver);
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void receiveAndPostCell(final @NonNull Intent intent) {
        final int cellPos = intent.getIntExtra(CELL_KEY, 0);
        final PlayerType movedPlayer = PlayerType.values()[intent.getIntExtra(PLAYER_TYPE_KEY, 0)];
        final Integer[] cells = viewModel.getCellsState().getValue();
        Log.d(TAG, String.format("Player %s moved to %d", movedPlayer, cellPos));

        cells[cellPos] = viewModel.getPlayerRoleByType(movedPlayer).ordinal();
        viewModel.postCellsState(cells);
    }

    private void stopNetwork() {
        stopServiceIfLaunched();
        stopClientIfLaunched();
    }

    private void stopServiceIfLaunched() {
        try {
            serviceAccessor.stopServiceIfLaunched();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private DisposableCompletableObserver stopClientIfLaunched() {
        Log.d(TAG, "Stopping client");

        final UUID clientTaskId = clientTaskIdState.get();

        if (clientTaskId == null)
            return null;

        return Completable.fromRunnable(() -> {
                    WorkManager
                            .getInstance(requireContext())
                            .cancelWorkById(clientTaskId);

                    clientTaskIdState.set(null);

                    try {
                        getClient().close();
                        clientState.postValue(null);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribeWith(DefaultDisposableCompletable.disposableCompletableObserver());
    }
}
