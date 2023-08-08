package com.paranid5.tic_tac_toe.presentation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

public interface StateChangedCallback<H extends UIHandler, T> {
    class State<T> {
        public boolean isChanged;

        @Nullable
        public T value;

        public State(final boolean isChanged, final @Nullable T value) {
            this.isChanged = isChanged;
            this.value = value;
        }

        public State(final @Nullable T value) {
            this.isChanged = false;
            this.value = value;
        }

        public State(final boolean isChanged) {
            this.isChanged = isChanged;
            this.value = null;
        }

        public State() {
            this.isChanged = false;
            this.value = null;
        }
    }

    void onStateChanged(final @NonNull H handler, final @Nullable T data);

    default void observe(
            final @NonNull LifecycleOwner owner,
            final @NonNull LiveData<State<T>> state,
            final @NonNull H handler
    ) {
        state.observe(owner, isChangedToData -> {
            if (isChangedToData.isChanged)
                onStateChanged(handler, isChangedToData.value);
        });
    }
}
