package com.paranid5.tic_tac_toe.presentation;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;

import com.paranid5.tic_tac_toe.presentation.main_fragment.MainFragment;
import com.paranid5.tic_tac_toe.presentation.select_game_type_fragment.SelectGameTypeFragment;

import javax.inject.Inject;

public final class GlobalFragmentFactory extends FragmentFactory {
    @Inject
    public GlobalFragmentFactory() {}

    @NonNull
    @Override
    public Fragment instantiate(
            final @NonNull ClassLoader classLoader,
            final @NonNull String className
    ) {
        if (className.equals(MainFragment.class.getName()))
            return new MainFragment();

        if (className.equals(SelectGameTypeFragment.class.getName()))
            return new SelectGameTypeFragment();

        return super.instantiate(classLoader, className);
    }
}
