package com.paranid5.tic_tac_toe.presentation;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

public interface StateChangedCallback<H extends UIHandler> {
    void onStateChanged(final @NonNull H handler);

    default void observe(
            final @NonNull LifecycleOwner owner,
            final @NonNull LiveData<Boolean> state,
            final @NonNull H handler
    ) {
        state.observe(owner, isChanged -> {
            if (isChanged) onStateChanged(handler);
        });
    }
}
