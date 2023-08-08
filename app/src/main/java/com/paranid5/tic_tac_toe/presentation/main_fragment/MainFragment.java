package com.paranid5.tic_tac_toe.presentation.main_fragment;

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
import com.paranid5.tic_tac_toe.databinding.FragmentMainBinding;
import com.paranid5.tic_tac_toe.presentation.StateChangedCallback;
import com.paranid5.tic_tac_toe.presentation.UIStateChangesObserver;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class MainFragment extends Fragment implements UIStateChangesObserver {

    @NonNull
    private FragmentMainBinding binding;

    @NonNull
    private MainFragmentViewModel viewModel;

    @NonNull
    private final StateChangedCallback<MainFragmentUIHandler, Void> playButtonClickedCallback = (handler, t) -> {
        handler.onPlayButtonClicked(getParentFragmentManager());
        viewModel.onPlayButtonClickedFinished();
    };

    @NonNull
    private final StateChangedCallback<MainFragmentUIHandler, Void> settingsButtonClickedCallback = (handler, t) -> {
        handler.onSettingsButtonClicked(requireContext());
        viewModel.onSettingsButtonClickedFinished();
    };

    @NonNull
    @Override
    public View onCreateView(
            final @NonNull LayoutInflater inflater,
            final @Nullable ViewGroup container,
            final @Nullable Bundle savedInstanceState
    ) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        viewModel = new ViewModelProvider(this).get(MainFragmentViewModel.class);
        binding.setViewModel(viewModel);

        observeUIStateChanges();
        return binding.getRoot();
    }

    @Override
    public void observeUIStateChanges() {
        playButtonClickedCallback.observe(this, viewModel.getPlayButtonClickedState(), viewModel.handler);
        settingsButtonClickedCallback.observe(this, viewModel.getSettingsButtonClickedState(), viewModel.handler);
    }
}
