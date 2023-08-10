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
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.di.NetworkModule;
import com.paranid5.tic_tac_toe.domain.ReceiverManager;
import com.paranid5.tic_tac_toe.domain.network.ServerLauncher;
import com.paranid5.tic_tac_toe.domain.utils.network.DefaultDisposableCompletable;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
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
    public static final String Broadcast_HOST_MOVED = buildBroadcast("FIRST_MOVED");

    @NonNull
    public static final String CELL_KEY = "cell";

    @NonNull
    private final Binder binder = new Binder() {};

    @Nullable
    private Disposable serverTask;

    @NonNull
    @Override
    public Context getReceiverContext() { return this; }

    @Inject
    @NonNull
    MutableLiveData<String> hostState;

    @NonNull
    private final BroadcastReceiver stopServerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            stopServer();
        }
    };

    @NonNull
    private final BroadcastReceiver hostMovedReceiver = new BroadcastReceiver() {
        @SuppressLint("CheckResult")
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            final byte cellPos = (byte) intent.getIntExtra(CELL_KEY, 0);

            Completable
                    .fromRunnable(() -> {
                        try {
                            ServerLauncher.sendHostMoved(GameService.this, cellPos);
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
        registerReceiverCompat(stopServerReceiver, Broadcast_STOP_SERVER);
        registerReceiverCompat(hostMovedReceiver, Broadcast_HOST_MOVED);
    }

    @Override
    public void unregisterReceivers() {
        unregisterReceiver(stopServerReceiver);
        unregisterReceiver(hostMovedReceiver);
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
                    hostState.postValue(host);
                    ServerLauncher.sendHost(this, host);
                    return host;
                })
                .flatMapCompletable((host) -> ServerLauncher.launch(this, host))
                .subscribeWith(DefaultDisposableCompletable.disposableCompletableObserver());
    }

    private void stopServer() {
        if (serverTask != null)
            serverTask.dispose();
        serverTask = null;
    }
}
