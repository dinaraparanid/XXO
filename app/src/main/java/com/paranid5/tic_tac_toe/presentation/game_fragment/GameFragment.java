package com.paranid5.tic_tac_toe.presentation.game_fragment;

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
import com.paranid5.tic_tac_toe.domain.network.ClientLauncher;
import com.paranid5.tic_tac_toe.presentation.UIStateChangesObserver;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;

@AndroidEntryPoint
public final class GameFragment extends Fragment implements UIStateChangesObserver {
    @NonNull
    private static final String TAG = GameFragment.class.getSimpleName();

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

    @Nullable
    private DisposableCompletableObserver clientTask;

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

        observeUIStateChanges();

        if (playerType.equals(PlayerType.CLIENT)) {
            Log.d(TAG, "Client is launching");
            Objects.requireNonNull(host);
            clientTask = ClientLauncher.launch(host, viewModel);
        }

        return binding.getRoot();
    }

    @Override
    public void observeUIStateChanges() {
        // TODO: Cells clicks
    }
}
