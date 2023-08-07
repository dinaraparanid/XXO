package com.paranid5.tic_tac_toe.domain.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment.SelectGameRoomTypeFragment;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public final class ServerLauncher {
    private static final String TAG = ServerLauncher.class.getSimpleName();

    @NonNull
    public static Single<String> getLocalHost() {
        return Single
                .fromCallable(ServerLauncher::mGetLocalHost)
                .subscribeOn(Schedulers.io());
    }

    @NonNull
    public static Completable launch(final @NonNull String host) {
        return Completable
                .fromCallable(() -> {
                    launchService(host);
                    return true;
                })
                .subscribeOn(Schedulers.io());
    }

    private static void launchService(final @NonNull String host) throws IOException {
        Log.d(TAG, "Launching service");

        try (final ServerSocketChannel server = gameServer(host)) {
            try (final Selector selector = serverSelector(server)) {
                while (true) {
                    selector.select();

                    final Set<SelectionKey> keys = selector.selectedKeys();
                    final Iterator<SelectionKey> it = keys.iterator();

                    while (it.hasNext()) {
                        final SelectionKey key = it.next();

                        if (key.isAcceptable()) {
                            onClientConnected(server, selector);
                        } else if (key.isReadable()) {
                            onClientRequestReceived((SocketChannel) key.channel());
                        }

                        it.remove();
                    }
                }
            }
        }
    }

    @Nullable
    private static String mGetLocalHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (final UnknownHostException e) {
            return null;
        }
    }

    @NonNull
    private static ServerSocketChannel gameServer(final @NonNull String host) throws IOException {
        final ServerSocketChannel server = ServerSocketChannel.open();
        final InetSocketAddress addr = new InetSocketAddress(host, 8080);
        server.socket().bind(addr);
        server.configureBlocking(false);
        return server;
    }

    public static void sendHost(final @NonNull Context ctx, final @NonNull String host) {
        Log.d(TAG, String.format("Server is launched on %s", host));

        ctx.sendBroadcast(
                new Intent(SelectGameRoomTypeFragment.Broadcast_GAME_HOST)
                        .putExtra(SelectGameRoomTypeFragment.GAME_HOST_KEY, host)
        );
    }

    @NonNull
    private static Selector serverSelector(final @NonNull ServerSocketChannel server) throws IOException {
        final Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        return selector;
    }

    private static void onClientConnected(
            final @NonNull ServerSocketChannel server,
            final @NonNull Selector selector
    ) throws IOException {
        final SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    private static void onClientRequestReceived(final @NonNull SocketChannel client) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(1024);
        final int bytesRead = client.read(buffer);

        if (bytesRead < 0) {
            client.close();
        } else {
            // TODO: Handle request

            buffer.flip();
            client.write(buffer);
        }
    }
}
