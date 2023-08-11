package com.paranid5.tic_tac_toe.di;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.startup.Initializer;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class NetworkModule implements Initializer<WorkManager> {
    @NonNull
    @Singleton
    @Provides
    public MutableLiveData<SocketChannel> provideServerClientState() {
        return new MutableLiveData<>(null);
    }

    @NonNull
    @Singleton
    @Provides
    public MutableLiveData<Socket> provideClientState() {
        return new MutableLiveData<>(null);
    }

    @NonNull
    @Singleton
    @Provides
    public AtomicReference<UUID> provideClientTaskIdState() {
        return new AtomicReference<>();
    }

    @NonNull
    @Singleton
    @Provides
    public MutableLiveData<String> provideHostState() {
        return new MutableLiveData<>(null);
    }

    @NonNull
    @Override
    public WorkManager create(final @ApplicationContext @NonNull Context context) {
        final Configuration configuration = new Configuration.Builder().build();
        WorkManager.initialize(context, configuration);
        return WorkManager.getInstance(context);
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() { return new ArrayList<>(); }
}
