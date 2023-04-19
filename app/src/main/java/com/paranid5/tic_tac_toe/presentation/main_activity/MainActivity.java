package com.paranid5.tic_tac_toe.presentation.main_activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.databinding.ActivityMainBinding;
import com.paranid5.tic_tac_toe.presentation.StateChangedCallback;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class MainActivity extends AppCompatActivity implements LifecycleOwner {
    @NonNull
    private MainActivityViewModel viewModel;

    @NonNull
    private ActivityMainBinding binding;

    @NonNull
    private final StateChangedCallback<MainActivityUIHandler> playButtonClickedCallback = handler -> {
        handler.onPlayButtonClicked(this);
        viewModel.onPlayButtonClickedFinished();
    };

    @NonNull
    private final StateChangedCallback<MainActivityUIHandler> settingsButtonClickedCallback = handler -> {
        handler.onSettingsButtonClicked(this);
        viewModel.onSettingsButtonClickedFinished();
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        binding.setContext(this);
        binding.setUiHandler(viewModel.handler);
        handleUIStateChanges();
    }

    private void handleUIStateChanges() {
        playButtonClickedCallback.observe(this, viewModel.getPlayButtonClickedState(), viewModel.handler);
        settingsButtonClickedCallback.observe(this, viewModel.getSettingsButtonClickedState(), viewModel.handler);
    }
}