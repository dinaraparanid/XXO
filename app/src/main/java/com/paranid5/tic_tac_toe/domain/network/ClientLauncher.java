package com.paranid5.tic_tac_toe.domain.network;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.presentation.StateChangedCallback;
import com.paranid5.tic_tac_toe.presentation.game_fragment.PlayerRole;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public final class ClientLauncher {
    @NonNull
    private static final String TAG = ClientLauncher.class.getSimpleName();

    public static final byte GAME_START = 0;

    public static final byte CLIENT_TIME_TO_MOVE = 1;

    public static final byte HOST_TIME_TO_MOVE = 2;

    private static final class RequestCallbackArgs<T> {
        @NonNull
        public final SocketChannel client;

        @NonNull
        public final byte[] body;

        @NonNull
        public final MutableLiveData<StateChangedCallback.State<T>> viewModelActionState;

        public RequestCallbackArgs(
                final @NonNull SocketChannel client,
                final @NonNull byte[] body,
                final @NonNull MutableLiveData<StateChangedCallback.State<T>> viewModelAction
        ) {
            this.client = client;
            this.body = body;
            this.viewModelActionState = viewModelAction;
        }
    }

    @NonNull
    private static final Map<Byte, Function<RequestCallbackArgs, Void>> requestHandlers = buildRequestHandlers();

    @NonNull
    private static Map<Byte, Function<RequestCallbackArgs, Void>> buildRequestHandlers() {
        final Map<Byte, Function<RequestCallbackArgs, Void>> rh = new HashMap<>();
        rh.put(GAME_START, ClientLauncher::onGameStartReceived);
        return rh;
    }

    @NonNull
    public static DisposableCompletableObserver launch(
            final @NonNull String host,
            final @NonNull MutableLiveData<StateChangedCallback.State<PlayerRole>> roleState
    ) {
        return Completable
                .fromAction(() -> launchClient(host, roleState))
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
            final @NonNull MutableLiveData<StateChangedCallback.State<PlayerRole>> roleToHostState
    ) throws IOException {
        final SocketChannel client = clientSocket(host);
        final Selector selector = clientSelector(client);

        Log.d(TAG, "Selector is configured");

        final ByteBuffer buffer = ByteBuffer.allocate(32);

        Log.d(TAG, String.format("Connected to the game at %s", host));

        while (true) {
            selector.select();

            final Set<SelectionKey> keys = selector.selectedKeys();
            final Iterator<SelectionKey> it = keys.iterator();

            while (it.hasNext()) {
                final SelectionKey key = it.next();

                if (key.isReadable()) {
                    Log.d(TAG, "Prepare to read");

                    if (client.read(buffer) < 0) {
                        Log.d(TAG, "Connection is lost");
                        client.close();
                        return;
                    }

                    final byte[] msg = buffer.array();
                    final byte request = msg[0];
                    final byte[] body = parseBody(msg);

                    requestHandlers.get(request).apply(
                            new RequestCallbackArgs<>(client, body, roleToHostState)
                    );
                }

                it.remove();
            }
        }
    }

    @NonNull
    private static SocketChannel clientSocket(final @NonNull String host) throws IOException {
        final SocketChannel client = SocketChannel.open(new InetSocketAddress(host, 8080));
        client.configureBlocking(false);
        return client;
    }

    private static Selector clientSelector(final @NonNull SocketChannel client) throws IOException {
        final Selector selector = Selector.open();
        client.register(selector, SelectionKey.OP_READ);
        return selector;
    }

    @NonNull
    private static byte[] parseBody(final @NonNull byte[] msg) {
        final byte[] body = new byte[msg.length - 1];
        System.arraycopy(msg, 1, body, 0, body.length);
        return body;
    }

    private static Void onGameStartReceived(final @NonNull RequestCallbackArgs<PlayerRole> args) {
        final PlayerRole role = PlayerRole.values()[args.body[0]];
        Log.d(TAG, String.format("Game is started as %s", role));
        args.viewModelActionState.postValue(new StateChangedCallback.State<>(true, role));
        return null;
    }
}
