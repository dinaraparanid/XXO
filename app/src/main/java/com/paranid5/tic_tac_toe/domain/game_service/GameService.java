package com.paranid5.tic_tac_toe.domain.game_service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.domain.ReceiverManager;
import com.paranid5.tic_tac_toe.domain.network.ServerLauncher;
import com.paranid5.tic_tac_toe.presentation.game_fragment.PlayerRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;

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
    public static final String Broadcast_FIRST_MOVED = buildBroadcast("FIRST_MOVED");

    @NonNull
    private final Binder binder = new Binder() {};

    @Nullable
    private Disposable serverTask;

    @NonNull
    private final MutableLiveData<PlayerRole[]> rolesState = new MutableLiveData<>(null);

    @NonNull
    private final BroadcastReceiver stopServerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            stopServer();
        }
    };

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
        registerReceiverCompat(stopServerReceiver, Broadcast_STOP_SERVER);
        registerReceiverCompat(firstMovedReceiver, Broadcast_FIRST_MOVED);
    }

    @Override
    public void unregisterReceivers() {
        unregisterReceiver(stopServerReceiver);
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
        serverTask = sendLocalHostAndStartServer();
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        stopServer();
        unregisterReceivers();
    }

    @NonNull
    private DisposableCompletableObserver sendLocalHostAndStartServer() {
        return ServerLauncher.getLocalHost(this)
                .map(host -> {
                    Objects.requireNonNull(host); // TODO: handle no wifi connection
                    ServerLauncher.sendHost(this, host);
                    return host;
                })
                .flatMapCompletable((host) -> {
                    rolesState.postValue(generateRoles());
                    return ServerLauncher.launch(this, host, rolesState);
                })
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

    private void stopServer() {
        if (serverTask != null)
            serverTask.dispose();
        serverTask = null;
    }

    @NonNull
    private PlayerRole[] generateRoles() {
        final List<PlayerRole> roles = new ArrayList<>(2);
        roles.add(PlayerRole.ZERO); roles.add(PlayerRole.CROSS);
        Collections.shuffle(roles);
        return roles.toArray(new PlayerRole[0]);
    }
}
