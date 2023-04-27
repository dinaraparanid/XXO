package com.paranid5.tic_tac_toe.presentation.select_game_type_fragment;

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
import com.paranid5.tic_tac_toe.databinding.FragmentSelectGameTypeBinding;
import com.paranid5.tic_tac_toe.presentation.StateChangedCallback;
import com.paranid5.tic_tac_toe.presentation.UIStateChangesObserver;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class SelectGameTypeFragment extends Fragment implements UIStateChangesObserver {

    @NonNull
    private FragmentSelectGameTypeBinding binding;

    @NonNull
    private SelectGameTypeViewModel viewModel;

    @NonNull
    private final StateChangedCallback<SelectGameTypeUIHandler> singleplayerButtonClickedCallback = handler -> {
        handler.onSingleplayerButtonClicked(requireContext());
        viewModel.onSingleplayerButtonClickedFinished();
    };

    @NonNull
    private final StateChangedCallback<SelectGameTypeUIHandler> multiplayerButtonClickedCallback = handler -> {
        handler.onMultiplayerButtonClicked(getParentFragmentManager());
        viewModel.onMultiplayerButtonClickedFinished();
    };

    @NonNull
    @Override
    public View onCreateView(
            final @NonNull LayoutInflater inflater,
            final @Nullable ViewGroup container,
            final @Nullable Bundle savedInstanceState
    ) {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_select_game_type,
                container,
                false
        );

        viewModel = new ViewModelProvider(this).get(SelectGameTypeViewModel.class);
        binding.setViewModel(viewModel);

        observeUIStateChanges();
        return binding.getRoot();
    }

    @Override
    public void observeUIStateChanges() {
        singleplayerButtonClickedCallback.observe(this, viewModel.getSingleplayerButtonClickedState(), viewModel.handler);
        multiplayerButtonClickedCallback.observe(this, viewModel.getMultiplayerButtonClickedState(), viewModel.handler);
    }
}
