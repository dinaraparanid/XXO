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
import androidx.lifecycle.ViewModelProvider;

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.databinding.FragmentGameBinding;
import com.paranid5.tic_tac_toe.domain.ReceiverManager;
import com.paranid5.tic_tac_toe.presentation.UIStateChangesObserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
    public static String Broadcast_GAME_HOST = buildBroadcast("GAME_HOST");

    @NonNull
    public static String GAME_HOST_KEY = "game_host";

    @NonNull
    public static String PLAYER_TYPE = "player_type";

    @NonNull
    public static String PLAYER_ROLE = "player_role";

    @NonNull
    public static String HOST = "host";

    @NonNull
    private FragmentGameBinding binding;

    @NonNull
    private GameFragmentViewModel viewModel;

    @NonNull
    private PlayerType playerType;

    @NonNull
    private PlayerRole playerRole;

    @NonNull
    private Executor clientExecutor = Executors.newSingleThreadExecutor();

    @NonNull
    private final BroadcastReceiver gameHostReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @Nullable Context context, final @NonNull Intent intent) {
            final String host = intent.getStringExtra(GAME_HOST_KEY);
            Log.d(TAG, String.format("Host %s is received", host));
        }
    };

    GameFragment() {}

    @NonNull
    public static GameFragment newInstance(
            final @NonNull PlayerType playerType,
            final @NonNull PlayerRole playerRole,
            final @Nullable String host
    ) {
        final GameFragment fragment = new GameFragment();
        final Bundle args = new Bundle();

        args.putInt(PLAYER_ROLE, playerRole.ordinal());
        args.putInt(PLAYER_TYPE, playerType.ordinal());
        args.putString(HOST, host);

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
        playerType = PlayerType.values()[requireArguments().getInt(PLAYER_TYPE)];
        playerRole = PlayerRole.values()[requireArguments().getInt(PLAYER_ROLE)];
        final String host = requireArguments().getString(HOST);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game, container, false);
        viewModel = new ViewModelProvider(this).get(GameFragmentViewModel.class);
        binding.setViewModel(viewModel);

        registerReceivers();
        observeUIStateChanges();

        if (playerType.equals(PlayerType.CLIENT))
            clientExecutor.execute(() -> {
                try {
                    assert host != null;
                    launchClient(host);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            });

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }

    @NonNull
    @Override
    public Context getReceiverContext() { return requireContext(); }

    @Override
    public void registerReceivers() {
        registerReceiverCompat(gameHostReceiver, Broadcast_GAME_HOST);
    }

    @Override
    public void unregisterReceivers() {
        requireContext().unregisterReceiver(gameHostReceiver);
    }

    @Override
    public void observeUIStateChanges() {
        // TODO: Cells clicks
    }

    private void launchClient(final @NonNull String host) throws IOException {
        try (
                final Socket client = new Socket(host, 8080);
                final BufferedInputStream reader = new BufferedInputStream(client.getInputStream());
                final BufferedOutputStream writer = new BufferedOutputStream(client.getOutputStream())
        ) {
            while (true) {
                final byte[] data = new byte[2];
                reader.read(data);

                // TODO: Handle server requests
            }
        }
    }
}
