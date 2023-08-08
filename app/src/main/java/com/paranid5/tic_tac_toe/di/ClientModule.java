package com.paranid5.tic_tac_toe.di;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;

@Module
@InstallIn(SingletonComponent.class)
public final class ClientModule {
    @NonNull
    @Singleton
    @Provides
    public MutableLiveData<DisposableCompletableObserver> provideClientTask() {
        return new MutableLiveData<>(null);
    }
}
