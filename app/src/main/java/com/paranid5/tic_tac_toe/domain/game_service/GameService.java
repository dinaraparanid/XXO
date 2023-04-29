package com.paranid5.tic_tac_toe.domain.game_service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class GameService extends Service {
    private static final String TAG = GameService.class.getSimpleName();

    private static final String SERVICE_LOCATION = "com.paranid5.tic_tac_toe.domain.game_service";

    @NonNull
    private static String buildBroadcast(final @NonNull String action) {
        return String.format("%s.%s", SERVICE_LOCATION, action);
    }

    public static final String Broadcast_SECOND_CONNECTED = buildBroadcast("SECOND_CONNECTED");
    public static final String Broadcast_SECOND_MOVED = buildBroadcast("SECOND_MOVED");

    private final Binder binder = new Binder() {};

    private final BroadcastReceiver secondConnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            // TODO: send broadcast to start game
        }
    };

    private final BroadcastReceiver secondMovedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final @NonNull Context context, final @NonNull Intent intent) {
            // TODO: check if game is finished, send broadcasts
        }
    };

    private void registerReceivers() {
        registerReceiver(secondConnectedReceiver, new IntentFilter(Broadcast_SECOND_CONNECTED));
        registerReceiver(secondMovedReceiver, new IntentFilter(Broadcast_SECOND_MOVED));
    }

    private void unregisterReceivers() {
        unregisterReceiver(secondConnectedReceiver);
        unregisterReceiver(secondMovedReceiver);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceivers();
        Log.d(TAG, "onCreate()");
    }

    @NonNull
    @Override
    public IBinder onBind(final @NonNull Intent intent) { return binder; }

    @Override
    public int onStartCommand(final @Nullable Intent intent, final int flags, final int startId) {
        // TODO: launch game server
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }
}
