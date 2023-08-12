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

import com.paranid5.tic_tac_toe.data.PlayerRole;
import com.paranid5.tic_tac_toe.data.PlayerType;
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
    private final PlayerRole[] cells = new PlayerRole[9];

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
        }
    };

    @NonNull
    private final BroadcastReceiver hostMovedReceiver = new BroadcastReceiver() {
        @SuppressLint("CheckResult")
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            final int cellPos = intent.getIntExtra(CELL_KEY, 0);
            cells[cellPos] = hostAndClientRoles.first;

            Completable
                    .fromRunnable(() -> {
                        try {
                            if (checkForVictory() == null)
                                ServerLauncher.sendHostMoved(GameService.this, (byte) cellPos);
                            else
                                ServerLauncher.sendHostWon(GameService.this);
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
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            final int cellPos = intent.getIntExtra(CELL_KEY, 0);
            cells[cellPos] = hostAndClientRoles.second;

            if (checkForVictory() == null) sendClientMoved(cellPos);
            else sendClientWon();
        }
    };

    @Override
    public void registerReceivers() {
        registerReceiverCompat(stopServerReceiver, Broadcast_STOP_SERVER);
        registerReceiverCompat(rolesGeneratedReceiver, Broadcast_ROLES_GENERATED);
        registerReceiverCompat(hostMovedReceiver, Broadcast_HOST_MOVED);
        registerReceiverCompat(clientMovedReceiver, Broadcast_CLIENT_MOVED);
    }

    @Override
    public void unregisterReceivers() {
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

    @Nullable
    private PlayerRole checkForVictory() {
        // TODO: Check for victory
        return null;
    }

    private void sendClientMoved(final int cellPos) {
        sendBroadcast(
                new Intent(GameFragment.Broadcast_PLAYER_MOVED)
                        .putExtra(GameFragment.PLAYER_TYPE, PlayerType.CLIENT.ordinal())
                        .putExtra(GameFragment.CELL_KEY, cellPos)
        );
    }

    private void sendClientWon() {
        sendBroadcast(
                new Intent(GameFragment.Broadcast_PLAYER_MOVED)
                        .putExtra(GameFragment.PLAYER_TYPE, PlayerType.CLIENT.ordinal())
        );
    }
}
