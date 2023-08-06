package com.paranid5.tic_tac_toe.domain.game_service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.paranid5.tic_tac_toe.domain.ReceiverManager;
import com.paranid5.tic_tac_toe.presentation.game_fragment.GameFragment;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class GameService extends Service implements ReceiverManager {
    @NonNull
    private static final String TAG = GameService.class.getSimpleName();

    @NonNull
    private static final String SERVICE_LOCATION = "com.paranid5.tic_tac_toe.domain.game_service";

    @NonNull
    private static String buildBroadcast(final @NonNull String action) {
        return String.format("%s.%s", SERVICE_LOCATION, action);
    }

    @NonNull
    public static final String Broadcast_FIRST_MOVED = buildBroadcast("FIRST_MOVED");

    @NonNull
    private final Binder binder = new Binder() {};

    @NonNull
    private final Executor executor = Executors.newSingleThreadExecutor();

    @NonNull
    private final BroadcastReceiver firstMovedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            // TODO: check if game is finished, send broadcasts
        }
    };

    @NonNull
    @Override
    public Context getReceiverContext() { return this; }

    @Override
    public void registerReceivers() {
        registerReceiverCompat(firstMovedReceiver, Broadcast_FIRST_MOVED);
    }

    @Override
    public void unregisterReceivers() {
        unregisterReceiver(firstMovedReceiver);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceivers();
        Log.d(TAG, "onCreate()");
    }

    @NonNull
    @Override
    public IBinder onBind(final @NonNull Intent intent) {
        Log.d(TAG, "onBind()");

        executor.execute(() -> {
            try {
                launchService();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        });

        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }

    private void launchService() throws IOException {
        Log.d(TAG, "Launching service");

        try (final ServerSocketChannel server = gameServer()) {
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
    private String getWifiInetIp() {
        final WifiManager manager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        try {
            return InetAddress.getByAddress(
                    ByteBuffer.allocate(4)
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .putInt(manager.getConnectionInfo().getIpAddress())
                            .array()
            ).getHostAddress();
        } catch (final UnknownHostException e) {
            return null;
        }
    }

    @NonNull
    private ServerSocketChannel gameServer() throws IOException {
        final ServerSocketChannel server = ServerSocketChannel.open();
        final InetSocketAddress addr = new InetSocketAddress(getWifiInetIp(), 8080);
        server.socket().bind(addr);
        server.configureBlocking(false);
        sendHost(addr.getHostString());
        return server;
    }

    private void sendHost(final @NonNull String host) {
        Log.d(TAG, String.format("Server is launched on %s", host));

        sendBroadcast(
                new Intent(GameFragment.Broadcast_GAME_HOST)
                        .putExtra(GameFragment.GAME_HOST_KEY, host)
        );
    }

    @NonNull
    private Selector serverSelector(final @NonNull ServerSocketChannel server) throws IOException {
        final Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        return selector;
    }

    private void onClientConnected(
            final @NonNull ServerSocketChannel server,
            final @NonNull Selector selector
    ) throws IOException {
        final SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    private void onClientRequestReceived(final @NonNull SocketChannel client) throws IOException {
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
