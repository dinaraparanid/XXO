package com.paranid5.tic_tac_toe.di;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.net.Socket;
import java.nio.channels.SocketChannel;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;

@Module
@InstallIn(SingletonComponent.class)
public final class NetworkModule {
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
    public MutableLiveData<DisposableCompletableObserver> provideClientTaskState() {
        return new MutableLiveData<>(null);
    }

    @NonNull
    @Singleton
    @Provides
    public MutableLiveData<String> provideHostState() {
        return new MutableLiveData<>(null);
    }
}
