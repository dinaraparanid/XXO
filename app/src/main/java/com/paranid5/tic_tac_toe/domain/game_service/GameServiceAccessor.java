package com.paranid5.tic_tac_toe.domain.game_service;

import android.app.Application;
import android.app.Service;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.paranid5.tic_tac_toe.domain.ServiceAccessor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class GameServiceAccessor extends ServiceAccessor {

    @Inject
    public GameServiceAccessor(final @NonNull Application application) {
        super(application);
    }

    public void bindServiceOrSendWifiHostRequest() {
        if (application.isGameServiceConnected)
            sendBroadcast(GameService.Broadcast_WIFI_HOST);
        else
            bindService();
    }

    private void bindService() {
        final Intent serviceIntent = new Intent(application, GameService.class);

        application.bindService(
                serviceIntent,
                application.gameServiceConnection,
                Service.BIND_AUTO_CREATE
        );
    }

    public void stopServiceIfLaunched() {
        if (application.isGameServiceConnected) {
            application.isGameServiceConnected = false;
            application.unbindService(application.gameServiceConnection);
        }
    }
}
