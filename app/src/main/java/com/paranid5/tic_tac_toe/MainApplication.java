package com.paranid5.tic_tac_toe;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public final class MainApplication extends Application {
    private static final String GAME_SERVICE_NAME = ".domain.game_service.GameService";

    private volatile boolean gameServiceConnected = false;

    public boolean isGameServiceConnected() { return gameServiceConnected; }

    public ServiceConnection gameServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final @NonNull ComponentName name, final @NonNull IBinder service) {
            if (name.getShortClassName().equals(GAME_SERVICE_NAME))
                gameServiceConnected = true;
        }

        @Override
        public void onServiceDisconnected(final @NonNull ComponentName name) {
            if (name.getShortClassName().equals(GAME_SERVICE_NAME))
                gameServiceConnected = false;
        }
    };
}
