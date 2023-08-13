package com.paranid5.tic_tac_toe.domain.game_service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.paranid5.tic_tac_toe.data.GameStatus;
import com.paranid5.tic_tac_toe.data.PlayerRole;
import com.paranid5.tic_tac_toe.data.PlayerType;
import com.paranid5.tic_tac_toe.data.utils.extensions.ArrayExt;
import com.paranid5.tic_tac_toe.domain.ReceiverManager;
import com.paranid5.tic_tac_toe.domain.network.ServerLauncher;
import com.paranid5.tic_tac_toe.domain.utils.network.DefaultDisposableCompletable;
import com.paranid5.tic_tac_toe.presentation.game_fragment.GameFragment;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
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
    public static final String Broadcast_WIFI_HOST = buildBroadcast("WIFI_HOST");

    @NonNull
    public static final String Broadcast_STOP_SERVER = buildBroadcast("STOP_SERVER");

    @NonNull
    public static final String Broadcast_ROLES_GENERATED = buildBroadcast("ROLES_GENERATED");

    @NonNull
    public static final String Broadcast_HOST_MOVED = buildBroadcast("HOST_MOVED");

    @NonNull
    public static final String Broadcast_CLIENT_MOVED = buildBroadcast("CLIENT_MOVED");

    @NonNull
    public static final String HOST_ROLE_KEY = "host_role";

    @NonNull
    public static final String CLIENT_ROLE_KEY = "client_role";

    @NonNull
    public static final String CELL_KEY = "cell";

    @NonNull
    private final Binder binder = new Binder() {};

    @NonNull
    private UUID serverTaskId;

    @NonNull
    @Override
    public Context getReceiverContext() { return this; }

    @Inject
    @NonNull
    MutableLiveData<String> hostState;

    @NonNull
    private Pair<PlayerRole, PlayerRole> hostAndClientRoles;

    @NonNull
    private PlayerRole[] cells = new PlayerRole[9];

    @NonNull
    private final BroadcastReceiver wifiHostReceiver = new BroadcastReceiver() {
        @SuppressLint("CheckResult")
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            ServerLauncher
                    .getWifiHost(getApplicationContext())
                    .flatMapCompletable(host -> io.reactivex.Completable.fromRunnable(() -> {
                        hostState.postValue(host);
                        ServerLauncher.sendHost(getApplicationContext(), host);
                    }))
                    .subscribeWith(DefaultDisposableCompletable.disposableCompletableObserverOld());
        }
    };

    @NonNull
    private final BroadcastReceiver stopServerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            stopServer();
        }
    };

    @NonNull
    private final BroadcastReceiver rolesGeneratedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            final PlayerRole hostRole = PlayerRole.values()[intent.getIntExtra(HOST_ROLE_KEY, 0)];
            final PlayerRole clientRole = PlayerRole.values()[intent.getIntExtra(CLIENT_ROLE_KEY, 0)];
            hostAndClientRoles = new Pair<>(hostRole, clientRole);
            cells = new PlayerRole[9];
        }
    };

    @NonNull
    private final BroadcastReceiver hostMovedReceiver = new BroadcastReceiver() {
        @SuppressLint("CheckResult")
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            final int cellPos = intent.getIntExtra(CELL_KEY, 0);
            cells[cellPos] = hostAndClientRoles.first;
            Log.d(TAG, String.format("Host moved to %d", cellPos));

            Completable
                    .fromRunnable(() -> {
                        try {
                            onHostMoved((byte) cellPos);
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .subscribeWith(DefaultDisposableCompletable.disposableCompletableObserver());
        }
    };

    @NonNull
    private final BroadcastReceiver clientMovedReceiver = new BroadcastReceiver() {
        @SuppressLint("CheckResult")
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            final int cellPos = intent.getIntExtra(CELL_KEY, 0);
            cells[cellPos] = hostAndClientRoles.second;
            Log.d(TAG, String.format("Client moved to %d", cellPos));

            Completable
                    .fromRunnable(() -> {
                        try {
                            onClientMoved(cellPos);
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .subscribeWith(DefaultDisposableCompletable.disposableCompletableObserver());
        }
    };

    @Override
    public void registerReceivers() {
        registerReceiverCompat(wifiHostReceiver, Broadcast_WIFI_HOST);
        registerReceiverCompat(stopServerReceiver, Broadcast_STOP_SERVER);
        registerReceiverCompat(rolesGeneratedReceiver, Broadcast_ROLES_GENERATED);
        registerReceiverCompat(hostMovedReceiver, Broadcast_HOST_MOVED);
        registerReceiverCompat(clientMovedReceiver, Broadcast_CLIENT_MOVED);
    }

    @Override
    public void unregisterReceivers() {
        unregisterReceiver(wifiHostReceiver);
        unregisterReceiver(stopServerReceiver);
        unregisterReceiver(rolesGeneratedReceiver);
        unregisterReceiver(hostMovedReceiver);
        unregisterReceiver(clientMovedReceiver);
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
        startServer();
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        stopServer();
        unregisterReceivers();
    }

    private void startServer() {
        final OneTimeWorkRequest serverTask = new OneTimeWorkRequest
                .Builder(ServerLauncher.class)
                .build();

        serverTaskId = serverTask.getId();
        WorkManager.getInstance(this).enqueue(serverTask);
    }

    private void stopServer() {
        WorkManager.getInstance(this).cancelWorkById(serverTaskId);
    }

    private void onHostMoved(final byte cellPos) throws IOException {
        final GameStatus status = getGameStatus();
        Log.d(TAG, String.format("Game status: %s", status));

        if (status instanceof GameStatus.Playing) {
            ServerLauncher.sendHostMoved(GameService.this, cellPos);
            return;
        }

        if (status instanceof GameStatus.Victor) {
            ServerLauncher.sendHostWon(GameService.this, cellPos);
            sendWon(PlayerType.HOST, cellPos);
            return;
        }

        ServerLauncher.sendDraw(GameService.this, PlayerType.HOST, cellPos);
        sendDraw(PlayerType.HOST, cellPos);
    }

    private void onClientMoved(final int cellPos) throws IOException {
        final GameStatus status = getGameStatus();

        if (status instanceof GameStatus.Playing) {
            sendMoved(PlayerType.CLIENT, cellPos);
            return;
        }

        if (status instanceof GameStatus.Victor) {
            ServerLauncher.sendClientWon(GameService.this, (byte) cellPos);
            sendWon(PlayerType.CLIENT, cellPos);
            return;
        }

        ServerLauncher.sendDraw(GameService.this, PlayerType.CLIENT, (byte) cellPos);
        sendDraw(PlayerType.CLIENT, cellPos);
    }

    @NonNull
    private GameStatus getGameStatus() {
        // | 0 | 1 | 2 |
        // | 3 | 4 | 5 |
        // | 6 | 7 | 8 |

        final PlayerType r1 = checkThree(0, 1, 2);
        if (r1 != null) return new GameStatus.Victor(r1);

        final PlayerType r2 = checkThree(3, 4, 5);
        if (r2 != null) return new GameStatus.Victor(r2);

        final PlayerType r3 = checkThree(6, 7, 8);
        if (r3 != null) return new GameStatus.Victor(r3);

        final PlayerType c1 = checkThree(0, 3, 6);
        if (c1 != null) return new GameStatus.Victor(c1);

        final PlayerType c2 = checkThree(1, 4, 7);
        if (c2 != null) return new GameStatus.Victor(c2);

        final PlayerType c3 = checkThree(2, 5, 8);
        if (c3 != null) return new GameStatus.Victor(c3);

        final PlayerType d1 = checkThree(0, 4, 8);
        if (d1 != null) return new GameStatus.Victor(d1);

        final PlayerType d2 = checkThree(2, 4, 6);
        if (d2 != null) return new GameStatus.Victor(d2);

        return ArrayExt.any(cells, (cell) -> cell == null)
                ? new GameStatus.Playing()
                : new GameStatus.Draw();
    }

    @Nullable
    private PlayerType checkThree(final int first, final int second, final int third) {
        if (cells[first] == null) return null;
        if (!cells[first].equals(cells[second])) return null;
        if (!cells[second].equals(cells[third])) return null;
        return getPlayerTypeFromRole(cells[first]);
    }

    @NonNull
    private PlayerType getPlayerTypeFromRole(final @NonNull PlayerRole role) {
        return role.equals(hostAndClientRoles.first) ? PlayerType.HOST : PlayerType.CLIENT;
    }

    private void sendMoved(final @NonNull PlayerType playerType, final int cellPos) {
        sendBroadcast(
                new Intent(GameFragment.Broadcast_PLAYER_MOVED)
                        .putExtra(GameFragment.PLAYER_TYPE_KEY, playerType.ordinal())
                        .putExtra(GameFragment.CELL_KEY, cellPos)
        );
    }

    private void sendWon(final @NonNull PlayerType playerType, final int cellPos) {
        sendBroadcast(
                new Intent(GameFragment.Broadcast_PLAYER_WON)
                        .putExtra(GameFragment.PLAYER_TYPE_KEY, playerType.ordinal())
                        .putExtra(GameFragment.CELL_KEY, cellPos)
        );
    }

    private void sendDraw(final @NonNull PlayerType playerType, final int cellPos) {
        sendBroadcast(
                new Intent(GameFragment.Broadcast_DRAW)
                        .putExtra(GameFragment.PLAYER_TYPE_KEY, playerType.ordinal())
                        .putExtra(GameFragment.CELL_KEY, cellPos)
        );
    }
}
