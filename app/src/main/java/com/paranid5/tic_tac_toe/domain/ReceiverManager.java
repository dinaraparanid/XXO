package com.paranid5.tic_tac_toe.domain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import androidx.annotation.NonNull;

import com.paranid5.tic_tac_toe.domain.utils.extensions.ContextExt;

public interface ReceiverManager {
    @NonNull
    Context getReceiverContext();

    void registerReceivers();
    void unregisterReceivers();

    default void registerReceiverCompat(
            final @NonNull BroadcastReceiver receiver,
            final @NonNull IntentFilter filter
    ) {
        ContextExt.registerReceiverCompat(
                getReceiverContext(),
                receiver,
                filter
        );
    }

    default void registerReceiverCompat(
            final @NonNull BroadcastReceiver receiver,
            final @NonNull String action
    ) {
        ContextExt.registerReceiverCompat(
                getReceiverContext(),
                receiver,
                action
        );
    }

    default void stopReceiver(final @NonNull BroadcastReceiver receiver) {
        getReceiverContext().unregisterReceiver(receiver);
    }
}
