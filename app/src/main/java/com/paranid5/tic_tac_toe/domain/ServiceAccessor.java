package com.paranid5.tic_tac_toe.domain;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.paranid5.tic_tac_toe.MainApplication;

public abstract class ServiceAccessor {
    @NonNull
    protected final MainApplication application;

    protected ServiceAccessor(final @NonNull Application application) {
        this.application = (MainApplication) application;
    }

    protected void sendBroadcast(final @NonNull Intent intent) {
        application.sendBroadcast(intent);
    }

    protected void sendBroadcast(final @NonNull String action) { sendBroadcast(new Intent(action)); }
}