package com.paranid5.tic_tac_toe.domain.network;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.hilt.work.HiltWorker;
import androidx.lifecycle.MutableLiveData;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;

import com.paranid5.tic_tac_toe.data.PlayerRole;
import com.paranid5.tic_tac_toe.data.PlayerType;
import com.paranid5.tic_tac_toe.domain.game_service.GameService;
import com.paranid5.tic_tac_toe.domain.utils.extensions.ListExt;
import com.paranid5.tic_tac_toe.domain.utils.network.NetUtils;
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
import java.util.Objects;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@HiltWorker
public final class ServerLauncher extends RxWorker {
    @NonNull
    private static final String TAG = ServerLauncher.class.getSimpleName();

    public static final byte CLIENT_MOVED = 0;

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface ServerLauncherEntryPoint {
        MutableLiveData<SocketChannel> serverClientState();
        MutableLiveData<String> hostState();
    }

    @NonNull
    private static final Map<Byte, Function<RequestCallbackArgs<SocketChannel>, Void>> requestHandlers = buildRequestHandlers();

    @NonNull
    private static Map<Byte, Function<RequestCallbackArgs<SocketChannel>, Void>> buildRequestHandlers() {
        final Map<Byte, Function<RequestCallbackArgs<SocketChannel>, Void>> rh = new HashMap<>();
        rh.put(CLIENT_MOVED, ServerLauncher::onClientMoved);
        return rh;
    }

    @AssistedInject
    ServerLauncher(
            final @Assisted @NonNull Context context,
            final @Assisted @NonNull WorkerParameters params
    ) { super(context, params); }

    @NonNull
    @Override
    public Single<Result> createWork() {
        return ServerLauncher.getWifiHost(getApplicationContext()).map(host -> {
            postHost(getApplicationContext(), Objects.requireNonNull(host));
            ServerLauncher.sendHost(getApplicationContext(), host);
            ServerLauncher.launchServer(getApplicationContext(), host);
            return Result.success();
        });
    }

    @NonNull
    private static Single<String> getWifiHost(final @NonNull Context context) {
        return Single
                .fromCallable(() -> ServerLauncher.mGetWifiHost(context))
                .subscribeOn(Schedulers.io());
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

    private static void launchServer(
            final @NonNull Context ctx,
            final @NonNull String host
    ) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(8);
        Log.d(TAG, "Launching server");

        final ServerSocketChannel server = gameServer(host);
        final Selector selector = serverSelector(server);
        Log.d(TAG, String.format("Server is launched on %s", host));

        while (true) {
            selector.select();

            final Iterator<SelectionKey> it = selector.selectedKeys().iterator();

            while (it.hasNext()) {
                final SelectionKey key = it.next();

                if (key.isAcceptable()) {
                    final SocketChannel client = registerClient(server, selector);
                    postClientSocket(ctx, client);
                    sendGameStart(ctx, client, buffer);
                    buffer.flip();
                } else if (key.isReadable()) {
                    final SocketChannel client = (SocketChannel) key.channel();
                    postClientSocket(ctx, client);
                    onClientRequestReceived(client, buffer, ctx);
                    buffer.flip();
                }

                it.remove();
            }
        }
    }

    @NonNull
    private static ServerSocketChannel gameServer(final @NonNull String host) throws IOException {
        final ServerSocketChannel server = ServerSocketChannel.open();
        final InetSocketAddress addr = new InetSocketAddress(host, 8080);
        server.socket().bind(addr);
        server.configureBlocking(false);
        Log.d(TAG, String.format("Server is binded to %s", addr));
        return server;
    }

    private static void sendHost(final @NonNull Context ctx, final @NonNull String host) {
        Log.d(TAG, String.format("Sending host %s to the UI", host));

        ctx.sendBroadcast(
                new Intent(SelectGameRoomTypeFragment.Broadcast_GAME_HOST)
                        .putExtra(SelectGameRoomTypeFragment.GAME_HOST_KEY, host)
        );
    }

    @NonNull
    private static Selector serverSelector(final @NonNull ServerSocketChannel server) throws IOException {
        final Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        Log.d(TAG, "Server selector is configured");
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

    private static void postHost(
            final @NonNull Context ctx,
            final @NonNull String host
    ) {
        EntryPointAccessors
                .fromApplication(
                        ctx.getApplicationContext(),
                        ServerLauncherEntryPoint.class
                )
                .hostState()
                .postValue(host);
    }

    private static void sendGameStart(
            final @NonNull Context context,
            final @NonNull SocketChannel client,
            final @NonNull ByteBuffer buffer
    ) throws IOException {
        Log.d(TAG, "Sending game start to both players");

        final PlayerRole[] roles = generateRoles();
        Log.d(TAG, String.format("Generated roles: %s", Arrays.toString(roles)));

        context.sendBroadcast(
                new Intent(GameService.Broadcast_ROLES_GENERATED)
                        .putExtra(GameService.HOST_ROLE_KEY, roles[0].ordinal())
                        .putExtra(GameService.CLIENT_ROLE_KEY, roles[1].ordinal())
        );

        context.sendBroadcast(
                new Intent(SelectGameRoomTypeFragment.Broadcast_GAME_START)
                        .putExtra(SelectGameRoomTypeFragment.PLAYER_TYPE_KEY, PlayerType.HOST.ordinal())
                        .putExtra(SelectGameRoomTypeFragment.PLAYER_ROLE_KEY, roles[0].ordinal())
        );

        sendClientRequest(
                client,
                buffer,
                ClientLauncher.GAME_START,
                new byte[] { (byte) roles[1].ordinal() }
        );

        Log.d(TAG, "Game start is sent to both players");
    }

    private static void onClientRequestReceived(
            final @NonNull SocketChannel client,
            final @NonNull ByteBuffer buffer,
            final @NonNull Context context
    ) throws IOException {
        buffer.clear();

        if (client.read(buffer) < 0) {
            Log.d(TAG, "Connection with client is lost");
            client.close();
            return;
        }

        final byte[] msg = buffer.array();
        final byte request = msg[0];
        final byte[] body = NetUtils.parseBody(msg);

        Log.d(TAG, String.format("Request %s is received with body %s", request, Arrays.toString(body)));

        Objects.requireNonNull(requestHandlers.get(request))
                .apply(new RequestCallbackArgs<>(client, body, context));
    }

    private static void sendClientRequest(
            final @NonNull SocketChannel client,
            final @NonNull ByteBuffer buffer,
            final byte request,
            final @NonNull byte[] body
    ) throws IOException {
        Log.d(TAG, String.format("Sending client request %s: %s", request, Arrays.toString(body)));

        buffer.clear();
        buffer.put(request);
        buffer.put(body);
        buffer.flip();

        Log.d(TAG, String.format("Prepare to write %d bytes as %s", buffer.remaining(), Arrays.toString(buffer.array())));

        while (buffer.hasRemaining()) {
            final long bytes = client.write(buffer);
            Log.d(TAG, String.format("Sent request %s; bytes: %s", request, bytes));
        }
    }

    @NonNull
    private static PlayerRole[] generateRoles() {
        final List<PlayerRole> roles = ListExt.mutableListOf(PlayerRole.CROSS, PlayerRole.ZERO);
        Collections.shuffle(roles);
        return roles.toArray(new PlayerRole[0]);
    }

    private static Void onClientMoved(final @NonNull RequestCallbackArgs<SocketChannel> args) {
        final int cellPos = args.body[0];

        args.context.sendBroadcast(
                new Intent(GameService.Broadcast_CLIENT_MOVED)
                        .putExtra(GameService.CELL_KEY, cellPos)
        );

        return null;
    }

    public static void sendHostMoved(
            final @NonNull Context context,
            final byte cellPosition
    ) throws IOException {
        sendMovedRequest(context, ClientLauncher.HOST_MOVED, cellPosition);
    }

    public static void sendHostWon(
            final @NonNull Context context,
            final byte cellPosition
    ) throws IOException {
        sendMovedRequest(context, ClientLauncher.HOST_WON, cellPosition);
    }

    public static void sendClientWon(
            final @NonNull Context context,
            final byte cellPosition
    ) throws IOException {
        sendMovedRequest(context, ClientLauncher.CLIENT_WON, cellPosition);
    }

    public static void sendDraw(
            final @NonNull Context context,
            final @NonNull PlayerType movedPlayer,
            final byte cellPosition
    ) throws IOException {
        sendClientRequest(
                Objects.requireNonNull(serverClientSocket(context)),
                ByteBuffer.allocate(8),
                ClientLauncher.DRAW,
                new byte[] { (byte) movedPlayer.ordinal(), cellPosition }
        );
    }

    public static void sendMovedRequest(
            final @NonNull Context context,
            final byte request,
            final byte cellPosition
    ) throws IOException {
        sendClientRequest(
                Objects.requireNonNull(serverClientSocket(context)),
                ByteBuffer.allocate(8),
                request,
                new byte[] { cellPosition }
        );
    }

    @Nullable
    private static SocketChannel serverClientSocket(final @NonNull Context ctx) {
        return EntryPointAccessors
                .fromApplication(
                        ctx,
                        ServerLauncherEntryPoint.class
                )
                .serverClientState()
                .getValue();
    }

    private static void postClientSocket(
            final @NonNull Context ctx,
            final @Nullable SocketChannel client
    ) {
        EntryPointAccessors
                .fromApplication(
                        ctx,
                        ServerLauncherEntryPoint.class
                )
                .serverClientState()
                .postValue(client);
    }
}
