package com.paranid5.tic_tac_toe.domain.network;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;

import com.paranid5.tic_tac_toe.presentation.game_fragment.GameFragmentViewModel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public final class ClientLauncher {
    @NonNull
    private static final String TAG = ClientLauncher.class.getSimpleName();

    private static final class RequestCallbackArgs {
        @NonNull
        public final SocketChannel client;

        @NonNull
        public final byte[] body;

        @NonNull
        public final GameFragmentViewModel viewModel;

        public RequestCallbackArgs(
                final @NonNull SocketChannel client,
                final @NonNull byte[] body,
                final @NonNull GameFragmentViewModel viewModel
        ) {
            this.client = client;
            this.body = body;
            this.viewModel = viewModel;
        }
    }

    @NonNull
    private static final Map<Byte, Function<RequestCallbackArgs, Void>> requestHandlers = buildRequestHandlers();

    @NonNull
    private static Map<Byte, Function<RequestCallbackArgs, Void>> buildRequestHandlers() {
        final Map<Byte, Function<RequestCallbackArgs, Void>> rh = new HashMap<>();
        return rh;
    }

    @NonNull
    public static DisposableCompletableObserver launch(
            final @NonNull String host,
            final @NonNull GameFragmentViewModel viewModel
    ) {
        return Completable
                .fromAction(() -> launchClient(host, viewModel))
                .subscribeOn(Schedulers.io())
                .subscribeWith(disposableLaunchObserver());
    }

    @NonNull
    private static DisposableCompletableObserver disposableLaunchObserver() {
        return new DisposableCompletableObserver() {
            @Override
            public void onComplete() {}

            @Override
            public void onError(final @NonNull Throwable e) {}
        };
    }

    private static void launchClient(
            final @NonNull String host,
            final @NonNull GameFragmentViewModel viewModel
    ) throws IOException {
        final SocketChannel client = SocketChannel.open(new InetSocketAddress(host, 8080));
        final ByteBuffer requestBuf = ByteBuffer.allocate(1);
        final ByteBuffer bodyBuf = ByteBuffer.allocate(1);
        final ByteBuffer[] buffer = { requestBuf, bodyBuf };

        Log.d(TAG, String.format("Connected to the game at %s", host));

        while (true) {
            if (client.read(buffer) < 0) {
                client.close();
                return;
            }

            final byte request = requestBuf.array()[0];
            final byte[] body = bodyBuf.array();

            requestHandlers.get(request).apply(
                    new RequestCallbackArgs(client, body, viewModel)
            );
        }
    }
}
