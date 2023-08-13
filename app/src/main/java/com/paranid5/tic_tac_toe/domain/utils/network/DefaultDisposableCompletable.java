package com.paranid5.tic_tac_toe.domain.utils.network;

import androidx.annotation.NonNull;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;

public class DefaultDisposableCompletable {
    @NonNull
    public static DisposableCompletableObserver disposableCompletableObserver() {
        return new DisposableCompletableObserver() {
            @Override
            public void onComplete() {}

            @Override
            public void onError(final @NonNull Throwable e) {}
        };
    }

    @NonNull
    public static io.reactivex.observers.DisposableCompletableObserver disposableCompletableObserverOld() {
        return new io.reactivex.observers.DisposableCompletableObserver() {
            @Override
            public void onComplete() {}

            @Override
            public void onError(final @NonNull Throwable e) {}
        };
    }
}
