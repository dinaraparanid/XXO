package com.paranid5.tic_tac_toe;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public final class MainApplication extends Application implements Configuration.Provider {
    private static final String GAME_SERVICE_NAME = ".domain.game_service.GameService";

    @Inject
    @NonNull
    HiltWorkerFactory workerFactory;

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

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .setMinimumLoggingLevel(Log.DEBUG)
                .build();
    }
}
