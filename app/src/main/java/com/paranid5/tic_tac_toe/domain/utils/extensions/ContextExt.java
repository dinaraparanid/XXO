package com.paranid5.tic_tac_toe.domain.utils.extensions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;

import androidx.annotation.NonNull;

public final class ContextExt {
    public static void registerReceiverCompat(
            final @NonNull Context context,
            final @NonNull BroadcastReceiver receiver,
            final @NonNull IntentFilter filter
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        else
            context.registerReceiver(receiver, filter);
    }

    public static void registerReceiverCompat(
            final @NonNull Context context,
            final @NonNull BroadcastReceiver receiver,
            final @NonNull String action
    ) { registerReceiverCompat(context, receiver, new IntentFilter(action)); }
}
