package com.paranid5.tic_tac_toe.domain.network;

import android.content.Context;

import androidx.annotation.NonNull;

public final class RequestCallbackArgs<C> {
    @NonNull
    public final C client;

    @NonNull
    public final byte[] body;

    @NonNull
    public Context context;

    public RequestCallbackArgs(
            final @NonNull C client,
            final @NonNull byte[] body,
            final @NonNull Context context
    ) {
        this.client = client;
        this.body = body;
        this.context = context;
    }
}
