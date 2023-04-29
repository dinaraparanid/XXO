package com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.databinding.FragmentSelectGameRoomTypeBinding;
import com.paranid5.tic_tac_toe.presentation.StateChangedCallback;
import com.paranid5.tic_tac_toe.presentation.UIStateChangesObserver;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class SelectGameRoomTypeFragment extends Fragment implements UIStateChangesObserver {
    @NonNull
    private FragmentSelectGameRoomTypeBinding binding;

    @NonNull
    private SelectGameRoomTypeViewModel viewModel;

    @NonNull
    private final StateChangedCallback<SelectGameRoomTypeUIHandler> createNewRoomButtonClickedCallback = handler -> {
        handler.onCreateNewRoomButtonClicked();
        viewModel.onCreateNewRoomButtonClickedFinished();
    };

    @NonNull
    private final StateChangedCallback<SelectGameRoomTypeUIHandler> connectRoomButtonClickedCallback = handler -> {
        handler.onConnectRoomButtonClicked();
        viewModel.onConnectRoomButtonClickedFinished();
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
    public void observeUIStateChanges() {
        createNewRoomButtonClickedCallback.observe(this, viewModel.getCreateNewRoomButtonClickedState(), viewModel.handler);
        connectRoomButtonClickedCallback.observe(this, viewModel.getConnectRoomButtonClickedState(), viewModel.handler);
    }
}
