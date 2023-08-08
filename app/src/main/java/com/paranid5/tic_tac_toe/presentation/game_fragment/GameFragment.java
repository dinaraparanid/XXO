package com.paranid5.tic_tac_toe.presentation.game_fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.databinding.FragmentGameBinding;
import com.paranid5.tic_tac_toe.presentation.UIStateChangesObserver;

import javax.inject.Inject;

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
    private FragmentGameBinding binding;

    @NonNull
    private GameFragmentViewModel viewModel;

    @NonNull
    @Inject
    MutableLiveData<DisposableCompletableObserver> clientTaskState;

    @Nullable
    private DisposableCompletableObserver getClientTask() { return clientTaskState.getValue(); }

    GameFragment() {}

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

        viewModel = new ViewModelProvider(this).get(GameFragmentViewModel.class);
        viewModel.postPlayerType(type);
        viewModel.postPlayerRole(role);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game, container, false);
        binding.setViewModel(viewModel);

        observeUIStateChanges();
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopClient();
    }

    @Override
    public void observeUIStateChanges() {
        // TODO: Cells clicks
    }

    private void stopClient() {
        if (getClientTask() != null) {
            getClientTask().dispose();
            clientTaskState.postValue(null);
        };
    }
}
