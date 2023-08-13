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

    public volatile boolean isGameServiceConnected = false;

    public ServiceConnection gameServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final @NonNull ComponentName name, final @NonNull IBinder service) {
            Log.d("MainApplication", String.format("%s is connected", name.getShortClassName()));

            if (name.getShortClassName().equals(GAME_SERVICE_NAME))
                isGameServiceConnected = true;
        }

        @Override
        public void onServiceDisconnected(final @NonNull ComponentName name) {
            Log.d("MainApplication", String.format("%s is disconnected", name.getShortClassName()));

            if (name.getShortClassName().equals(GAME_SERVICE_NAME))
                isGameServiceConnected = false;
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
