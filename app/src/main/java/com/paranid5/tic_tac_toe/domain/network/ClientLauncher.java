package com.paranid5.tic_tac_toe.domain.network;

import android.content.Context;
import android.content.Intent;
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
import com.paranid5.tic_tac_toe.domain.utils.network.NetUtils;
import com.paranid5.tic_tac_toe.presentation.game_fragment.GameFragment;
import com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment.SelectGameRoomTypeFragment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
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
public final class ClientLauncher extends RxWorker {
    @NonNull
    private static final String TAG = ClientLauncher.class.getSimpleName();

    public static final byte GAME_START = 0;

    public static final byte HOST_MOVED = 1;

    public static final byte CLIENT_MOVED = 2;

    public static final byte HOST_LEFT = 3;

    public static final byte HOST_WON = 4;

    public static final byte CLIENT_WON = 5;

    public static final byte DRAW = 6;

    public static final String HOST_KEY = "host";

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface ClientLauncherEntryPoint {
        MutableLiveData<Socket> clientState();
    }

    @NonNull
    private static final Map<Byte, Function<RequestCallbackArgs<Socket>, Void>> requestHandlers =
            buildRequestHandlers();

    @NonNull
    private static Map<Byte, Function<RequestCallbackArgs<Socket>, Void>> buildRequestHandlers() {
        final Map<Byte, Function<RequestCallbackArgs<Socket>, Void>> rh = new HashMap<>();
        rh.put(GAME_START, ClientLauncher::onGameStartReceived);
        rh.put(HOST_MOVED, ClientLauncher::onHostMoved);
        rh.put(CLIENT_MOVED, ClientLauncher::onClientMoved);
        rh.put(HOST_LEFT, ClientLauncher::onHostLeft);
        rh.put(HOST_WON, ClientLauncher::onHostWon);
        rh.put(CLIENT_WON, ClientLauncher::onClientWon);
        rh.put(DRAW, ClientLauncher::onDraw);
        return rh;
    }

    @AssistedInject
    ClientLauncher(
            final @Assisted @NonNull Context context,
            final @Assisted @NonNull WorkerParameters params
    ) { super(context, params); }

    @NonNull
    @Override
    public Single<Result> createWork() {
        Log.d(TAG, "Prepare to launch client");

        final String host = Objects.requireNonNull(getInputData().getString(HOST_KEY));

        return Single
                .just(Result.success())
                .map(r -> {
                    try {
                        launchClient(host, getApplicationContext());
                        return Result.success();
                    } catch (final IOException e) {
                        e.printStackTrace();
                        return Result.failure();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    private static void launchClient(
            final @NonNull String host,
            final @NonNull Context ctx
    ) throws IOException {
        Log.d(TAG, "Launching client");

        final Socket client = clientSocket(host);
        Log.d(TAG, "Client is created");

        postClient(ctx, client);
        Log.d(TAG, "Client is posted to live data");

        final byte[] buffer = new byte[8];

        try (final BufferedInputStream in = new BufferedInputStream(client.getInputStream())) {
            Log.d(TAG, "Prepare for a request loop");

            while (true) {
                if (!isRequestHandled(ctx, client, in, buffer)) {
                    stopClient(ctx, client);
                    break;
                }
            }
        }
    }

    @NonNull
    private static Socket clientSocket(final @NonNull String host) throws IOException {
        Log.d(TAG, "Creating client socket");
        final Socket client = new Socket(host, 8080);
        Log.d(TAG, "Client socket is created");
        return client;
    }

    private static void postClient(final @NonNull Context ctx, final @Nullable Socket client) {
        EntryPointAccessors
                .fromApplication(
                        ctx.getApplicationContext(),
                        ClientLauncherEntryPoint.class
                )
                .clientState()
                .postValue(client);
    }

    private static boolean isRequestHandled(
            final @NonNull Context ctx,
            final @NonNull Socket client,
            final @NonNull InputStream in,
            final @NonNull byte[] buffer
    ) throws IOException {
        final int bytesRead = in.read(buffer);
        Log.d(TAG, String.format("Bytes read: %d", bytesRead));

        if (bytesRead < 0)
            return false;

        final byte request = buffer[0];
        final byte[] body = NetUtils.parseBody(buffer);

        Log.d(TAG, String.format("Received request %s with body %s", request, Arrays.toString(buffer)));

        Objects.requireNonNull(requestHandlers.get(request))
                .apply(new RequestCallbackArgs<>(client, body, ctx));

        return true;
    }

    private static void stopClient(
            final @NonNull Context ctx,
            final @NonNull Socket client
    ) throws IOException {
        postClient(ctx, null);
        client.close();
    }

    public static void sendMoveToServer(
            final @NonNull Socket client,
            final byte cellPosition
    ) throws IOException {
        final BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
        out.write(new byte[] { ServerLauncher.CLIENT_MOVED, cellPosition });
        out.flush();
    }

    public static void sendLeftToServer(final @NonNull Socket client) throws IOException {
        final BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
        out.write(new byte[] { ServerLauncher.CLIENT_LEFT });
        out.flush();
    }

    private static Void onGameStartReceived(final @NonNull RequestCallbackArgs<Socket> args) {
        final PlayerRole role = PlayerRole.values()[args.body[0]];
        Log.d(TAG, String.format("Game is started as %s", role));

        args.context.sendBroadcast(
                new Intent(SelectGameRoomTypeFragment.Broadcast_GAME_START)
                        .putExtra(SelectGameRoomTypeFragment.PLAYER_TYPE_KEY, PlayerType.CLIENT.ordinal())
                        .putExtra(SelectGameRoomTypeFragment.PLAYER_ROLE_KEY, role.ordinal())
        );

        return null;
    }

    private static Void onHostMoved(final @NonNull RequestCallbackArgs<Socket> args) {
        return onPlayerMoved(args, PlayerType.HOST);
    }

    private static Void onClientMoved(final @NonNull RequestCallbackArgs<Socket> args) {
        return onPlayerMoved(args, PlayerType.CLIENT);
    }

    private static Void onPlayerMoved(
            final @NonNull RequestCallbackArgs<Socket> args,
            final @NonNull PlayerType playerType
    ) {
        final int cellPos = args.body[0];
        Log.d(TAG, String.format("%s moved at %d", playerType, cellPos));

        args.context.sendBroadcast(
                new Intent(GameFragment.Broadcast_PLAYER_MOVED)
                        .putExtra(GameFragment.PLAYER_TYPE_KEY, playerType.ordinal())
                        .putExtra(GameFragment.CELL_KEY, cellPos)
        );

        return null;
    }

    private static Void onHostLeft(final @NonNull RequestCallbackArgs<Socket> args) {
        args.context.sendBroadcast(
                new Intent(GameFragment.Broadcast_PLAYER_LEFT)
        );

        return null;
    }

    private static Void onHostWon(final @NonNull RequestCallbackArgs<Socket> args) {
        return onPlayerWon(args, PlayerType.HOST);
    }

    private static Void onClientWon(final @NonNull RequestCallbackArgs<Socket> args) {
        return onPlayerWon(args, PlayerType.CLIENT);
    }

    private static Void onPlayerWon(
            final @NonNull RequestCallbackArgs<Socket> args,
            final @NonNull PlayerType victor
    ) {
        final int cellPos = args.body[0];
        Log.d(TAG, String.format("%s moved at %d and won", victor, cellPos));

        args.context.sendBroadcast(
                new Intent(GameFragment.Broadcast_PLAYER_WON)
                        .putExtra(GameFragment.PLAYER_TYPE_KEY, victor.ordinal())
                        .putExtra(GameFragment.CELL_KEY, cellPos)
        );

        return null;
    }

    private static Void onDraw(final @NonNull RequestCallbackArgs<Socket> args) {
        final int cellPos = args.body[0];
        final int playerTypeOrd = args.body[1];
        Log.d(TAG, String.format("Draw after move at %d", cellPos));

        args.context.sendBroadcast(
                new Intent(GameFragment.Broadcast_DRAW)
                        .putExtra(GameFragment.PLAYER_TYPE_KEY, playerTypeOrd)
                        .putExtra(GameFragment.CELL_KEY, cellPos)
        );

        return null;
    }
}
