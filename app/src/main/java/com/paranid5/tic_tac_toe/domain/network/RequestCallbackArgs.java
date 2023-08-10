package com.paranid5.tic_tac_toe.domain.network;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.Socket;
import java.nio.channels.SocketChannel;

public final class RequestCallbackArgs {
    @Nullable
    public final Socket client;

    @Nullable
    public final SocketChannel clientChannel;

    @NonNull
    public final byte[] body;

    @NonNull
    public Context context;

    public RequestCallbackArgs(
            final @Nullable Socket client,
            final @Nullable SocketChannel clientChannel,
            final @NonNull byte[] body,
            final @NonNull Context context
    ) {
        this.client = client;
        this.clientChannel = clientChannel;
        this.body = body;
        this.context = context;
    }
}
