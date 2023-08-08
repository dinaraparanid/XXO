package com.paranid5.tic_tac_toe.domain.network;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.domain.utils.extensions.ListExt;
import com.paranid5.tic_tac_toe.presentation.game_fragment.PlayerRole;
import com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment.SelectGameRoomTypeFragment;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public final class ServerLauncher {
    @NonNull
    private static final String TAG = ServerLauncher.class.getSimpleName();

    public static final byte CLIENT_MOVED = 0;

    @NonNull
    private static final Map<Byte, Function<Pair<SocketChannel, byte[]>, Void>> requestHandlers = buildRequestHandlers();

    @NonNull
    private static Map<Byte, Function<Pair<SocketChannel, byte[]>, Void>> buildRequestHandlers() {
        final Map<Byte, Function<Pair<SocketChannel, byte[]>, Void>> rh = new HashMap<>();
        return rh;
    }

    @NonNull
    public static Single<String> getLocalHost(final @NonNull Context context) {
        return Single
                .fromCallable(() -> ServerLauncher.mGetWifiHost(context))
                .subscribeOn(Schedulers.io());
    }

    @NonNull
    public static Completable launch(
            final @NonNull Context ctx,
            final @NonNull String host,
            final @NonNull MutableLiveData<PlayerRole[]> rolesState
    ) {
        return Completable
                .fromAction(() -> launchServer(ctx, host, rolesState))
                .subscribeOn(Schedulers.io());
    }

    private static void launchServer(
            final @NonNull Context ctx,
            final @NonNull String host,
            final @NonNull MutableLiveData<PlayerRole[]> rolesState
    ) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(32);
        Log.d(TAG, "Launching server");

        try (
                final ServerSocketChannel server = gameServer(host);
                final Selector selector = serverSelector(server)
        ) {
            while (true) {
                selector.select();

                final Set<SelectionKey> keys = selector.selectedKeys();
                final Iterator<SelectionKey> it = keys.iterator();

                while (it.hasNext()) {
                    final SelectionKey key = it.next();

                    if (key.isAcceptable()) {
                        sendGameStart(ctx, rolesState, registerClient(server, selector), buffer);
                        buffer.flip();
                    } else if (key.isReadable()) {
                        onClientRequestReceived((SocketChannel) key.channel(), buffer);
                        buffer.flip();
                    }

                    it.remove();
                }
            }
        }
    }

    @Nullable
    private static String mGetWifiHost(final @NonNull Context context) {
        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        try {
            return InetAddress.getByAddress(
                    ByteBuffer
                            .allocate(4)
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .putInt(ipAddress)
                            .array()
            ).getHostAddress();
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

    @NonNull
    private static SocketChannel registerClient(
            final @NonNull ServerSocketChannel server,
            final @NonNull Selector selector
    ) throws IOException {
        final SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        Log.d(TAG, "Client is registered");
        return client;
    }

    private static void sendGameStart(
            final @NonNull Context context,
            final @NonNull MutableLiveData<PlayerRole[]> rolesState,
            final @NonNull SocketChannel client,
            final @NonNull ByteBuffer buffer
    ) throws IOException {
        Log.d(TAG, "Sending game start to host");

        final PlayerRole[] roles = generateRoles();
        rolesState.postValue(roles);

        context.sendBroadcast(
                new Intent(SelectGameRoomTypeFragment.Broadcast_GAME_START)
                        .putExtra(SelectGameRoomTypeFragment.PLAYER_ROLE_KEY, roles[0].ordinal())
        );

        sendRequest(
                client,
                buffer,
                ClientLauncher.GAME_START,
                new byte[] { (byte) roles[1].ordinal() }
        );

        Log.d(TAG, "Game start is sent to both players");
    }

    private static void onClientRequestReceived(
            final @NonNull SocketChannel client,
            final @NonNull ByteBuffer buffer
    ) throws IOException {
        if (client.read(buffer) < 0) {
            Log.d(TAG, "Connection with client is lost");
            client.close();
            return;
        }

        final byte[] msg = buffer.array();
        final byte request = msg[0];
        final byte[] body = parseBody(msg);
        buffer.flip();

        Log.d(TAG, String.format("Request %s is received", request));
        requestHandlers.get(request).apply(new Pair<>(client, body));
    }

    private static void sendRequest(
            final @NonNull SocketChannel client,
            final @NonNull ByteBuffer buffer,
            final byte request,
            final @NonNull byte[] body
    ) throws IOException {
        Log.d(TAG, String.format("Sending request %s: %s", request, Arrays.toString(body)));

        buffer.put(request);
        buffer.put(body);

        Log.d(TAG, "Prepare to write");
        final long bytes = client.write(buffer);
        Log.d(TAG, String.format("Sent request %s; bytes: %s", request, bytes));
    }

    @NonNull
    private static byte[] parseBody(final @NonNull byte[] msg) {
        final byte[] body = new byte[msg.length - 1];
        System.arraycopy(msg, 1, body, 0, body.length);
        return body;
    }

    @NonNull
    private static PlayerRole[] generateRoles() {
        final List<PlayerRole> roles = ListExt.mutableListOf(PlayerRole.CROSS, PlayerRole.ZERO);
        Collections.shuffle(roles);
        return roles.toArray(new PlayerRole[0]);
    }
}
