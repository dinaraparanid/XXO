package com.paranid5.tic_tac_toe.presentation;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.presentation.GlobalFragmentFactory;
import com.paranid5.tic_tac_toe.presentation.main_fragment.MainFragment;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class MainActivity extends AppCompatActivity implements LifecycleOwner {

    @Inject
    GlobalFragmentFactory fragmentFactory;

    @Override
    protected void onCreate(final @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().setFragmentFactory(fragmentFactory);
        initFirstFragment();
    }

    private void initFirstFragment() {
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(
                            R.id.fragment_container,
                            MainFragment.class,
                            null
                    )
                    .commit();
    }
}