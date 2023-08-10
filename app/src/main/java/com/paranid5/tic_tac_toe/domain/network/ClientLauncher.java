package com.paranid5.tic_tac_toe.domain.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.data.PlayerRole;
import com.paranid5.tic_tac_toe.data.PlayerType;
import com.paranid5.tic_tac_toe.di.NetworkModule;
import com.paranid5.tic_tac_toe.domain.utils.network.DefaultDisposableCompletable;
import com.paranid5.tic_tac_toe.presentation.game_fragment.GameFragment;
import com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment.SelectGameRoomTypeFragment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public final class ClientLauncher {
    @NonNull
    private static final String TAG = ClientLauncher.class.getSimpleName();

    public static final byte GAME_START = 0;

    public static final byte HOST_MOVED = 1;

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface ClientLauncherEntryPoint {
        MutableLiveData<Socket> clientState();
    }

    @NonNull
    private static final Map<Byte, Function<RequestCallbackArgs, Void>> requestHandlers = buildRequestHandlers();

    @NonNull
    private static Map<Byte, Function<RequestCallbackArgs, Void>> buildRequestHandlers() {
        final Map<Byte, Function<RequestCallbackArgs, Void>> rh = new HashMap<>();
        rh.put(GAME_START, ClientLauncher::onGameStartReceived);
        rh.put(HOST_MOVED, ClientLauncher::onHostMoved);
        return rh;
    }

    @NonNull
    public static DisposableCompletableObserver launch(
            final @NonNull String host,
            final @NonNull Context context
    ) {
        Log.d(TAG, "Prepare to launch client");

        return Completable
                .fromRunnable(() -> {
                    try {
                        launchClient(host, context);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribeWith(DefaultDisposableCompletable.disposableCompletableObserver());
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
            Log.d(TAG, "Prepare to read loop");

            while (true) {
                Log.d(TAG, String.format("Bytes read: %d", in.read(buffer)));

                final byte request = buffer[0];
                final byte[] body = parseBody(buffer);

                Log.d(TAG, String.format("Received %s", Arrays.toString(buffer)));

                requestHandlers.get(request).apply(
                        new RequestCallbackArgs(client, null, body, ctx)
                );
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

    @NonNull
    private static byte[] parseBody(final @NonNull byte[] msg) {
        final byte[] body = new byte[msg.length - 1];
        System.arraycopy(msg, 1, body, 0, body.length);
        return body;
    }

    public static void sendMoveToServer(
            final @NonNull Socket client,
            final byte cellPosition
    ) throws IOException {
        final BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
        out.write(new byte[] { ServerLauncher.CLIENT_MOVED, cellPosition });
        out.flush();
    }

    private static Void onGameStartReceived(final @NonNull RequestCallbackArgs args) {
        final PlayerRole role = PlayerRole.values()[args.body[0]];
        Log.d(TAG, String.format("Game is started as %s", role));

        args.context.sendBroadcast(
                new Intent(SelectGameRoomTypeFragment.Broadcast_GAME_START)
                        .putExtra(SelectGameRoomTypeFragment.PLAYER_TYPE_KEY, PlayerType.CLIENT.ordinal())
                        .putExtra(SelectGameRoomTypeFragment.PLAYER_ROLE_KEY, role.ordinal())
        );

        return null;
    }

    private static Void onHostMoved(final @NonNull RequestCallbackArgs args) {
        final byte cellPos = args.body[0];
        Log.d(TAG, String.format("Host moved at %d", cellPos));

        args.context.sendBroadcast(
                new Intent(GameFragment.Broadcast_PLAYER_MOVED)
                        .putExtra(GameFragment.PLAYER_TYPE, PlayerType.HOST.ordinal())
                        .putExtra(GameFragment.CELL_KEY, cellPos)
        );

        return null;
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
}
